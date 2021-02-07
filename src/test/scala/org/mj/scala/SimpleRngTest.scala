package org.mj.scala

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class SimpleRngTest extends AnyFlatSpec with Matchers {

  it should "blah blah" in {
    val rng = SimpleRng(42)
    println(Rand.ints(20)(rng)._1)
    assert(Rand.ints(10)(rng)._1.length == 10)
  }

  it should "x" in {
    val rng = SimpleRng(42)

    println(Rand.map2(Rand.int, Rand.double)(_ -> _)(rng)._1)
    assert(Rand.map2(Rand.int, Rand.double)(_ -> _)(rng)._1 == Rand.map2(Rand.int, Rand.double)(_ -> _)(rng)._1)
  }

  it should "sequence correctly" in {
    val rng = SimpleRng(42)

    val x = List.fill(3)(Rand.int)
    val y = Rand.sequence(x)
    val z = Rand.sequence(x)
    assert(y(rng)._1 == z(rng)._1)
  }

  it should "forcomprehend" in {
    val combined = for {
      i <- Rand.int
      d <- Rand.double
      i2 <- Rand.int
    } yield (i, d, i2)

    println(combined(SimpleRng(42))._1)
  }

}
