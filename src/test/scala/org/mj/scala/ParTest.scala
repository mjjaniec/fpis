package org.mj.scala


import java.util.concurrent.{ExecutorService, Executors, Semaphore, TimeoutException}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}


class ParTest extends BasicTest {

  private val es: ExecutorService = Executors.newFixedThreadPool(5)

  it should "unit" in {
    assert(Par.unit(4).run(es) == Success(4))
    assert(Par.unit("ala ma kota").run(es) == Success("ala ma kota"))
  }

  it should "fromTry" in {
    assert(Par.fromTry(Success(true)).run(es) == Success(true))
    val ex = new RuntimeException("blah blah")
    assert(Par.fromTry(Failure(ex)).run(es) == Failure(ex))
  }

  it should "run in parallel indeed" in {
    val a = new Semaphore(0)
    val b = new Semaphore(0)

    // execution of these pars must be run in parallel, or they'll be blocked
    val p1 = Par.lazyUnit {
      a.release()
      b.acquire()
    }
    val p2 = Par.lazyUnit {
      a.acquire()
      b.release()
    }

    assert(Par.map2(p1, p2)((_, _) => ()).run(es).isSuccess)
    assert(p1.flatMap(_ => p2).run(es, Duration("1s")).isFailure)
  }

  it should "flatMap" in {
    val p1 = Par.lazyUnit(3)
    assert(p1.flatMap(i => Par.lazyUnit(i * 3)).run(es) == Success(9))
  }


  it should "recover" in {
    val ex = new RuntimeException("makak")
    val p1 = Par.lazyUnit(throw ex)
    assert(p1.run(es) == Failure(ex))

    val p2 = Par.unit("x").flatMap(_ => throw  ex)
    assert(p2.run(es) == Failure(ex))

    val p3 = Par.map2(Par.lazyUnit(2), Par.lazyUnit[Int](throw  ex))(_ + _)
    assert(p3.run(es) == Failure(ex))

    val p4 = Par.map2(Par.lazyUnit(2), Par.unit(5))((_, _) => throw ex)
    assert(p4.run(es) == Failure(ex))

    val p5 = Par.unit(3).map(_ => throw ex)
    assert(p5.run(es) == Failure(ex))
  }
}
