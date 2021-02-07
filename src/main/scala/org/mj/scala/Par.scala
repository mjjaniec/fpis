package org.mj.scala


import org.mj.scala.Par.eval

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{Callable, ExecutorService, Semaphore, TimeUnit}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class TimeoutException extends Exception

private[scala] trait Cont[A] {
  def apply(cb: Try[A] => Unit): Unit
}

trait Par[A] extends Function[ExecutorService, Cont[A]] {
  def run(executorService: ExecutorService, timeout: Duration = Duration.apply("10s")): Try[A] = {
    val sem = new Semaphore(0)
    val ref = new AtomicReference[Try[A]]()
    this (executorService).apply { res =>
      ref.set(res)
      sem.release()
    }
    if (sem.tryAcquire(timeout.toMillis, TimeUnit.MILLISECONDS)) {
      ref.get()
    } else {
      Failure(new TimeoutException)
    }
  }


  def map[B](f: A => B): Par[B] = flatMap(a => Par.unit(f(a)))

  def flatMap[B](f: A => Par[B]): Par[B] = { es =>
    val thisCont = this(es)
    new Cont[B] {
      override def apply(cb: Try[B] => Unit): Unit = {
        thisCont.apply {
          case Success(a) => Try(f(a)) match {
            case Success(fa) => Par.eval(es)(fa(es).apply(cb))
            case f: Failure[Par[B]] => eval(es)(cb(f.asInstanceOf[Failure[B]]))
          }
          case f: Failure[A] => eval(es)(cb(f.asInstanceOf[Failure[B]]))
        }
      }
    }
  }

  def whenReady(f: A => Unit): Unit = map(f)

}

object Par {


  def unit[A](a: A): Par[A] = _ => (cb: Try[A] => Unit) => cb(Success(a))

  def fromTry[A](a: Try[A]): Par[A] = _ => (cb: Try[A] => Unit) => cb(a)

  def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

  def asyncF[A, B](f: A => B): A => Par[B] = a => lazyUnit(f(a))

  def map2[A, B, C](a: Par[A], b: Par[B])(f: (A, B) => C): Par[C] = (es: ExecutorService) => {
    (cb: Try[C] => Unit) => {
      var avo: Option[A] = None
      var bvo: Option[B] = None
      val combiner = Actor[Either[Try[A], Try[B]]](es) {
        case Left(Success(value)) =>
          bvo match {
            case None => avo = Some(value)
            case Some(bv) => eval(es)(cb(Try(f(value, bv))))
          }
        case Right(Success(value)) =>
          avo match {
            case None => bvo = Some(value)
            case Some(av) => eval(es)(cb(Try(f(av, value))))
          }
        case Left(f: Failure[A]) => eval(es)(cb(f.asInstanceOf[Failure[C]]))
        case Right(f: Failure[B]) => eval(es)(cb(f.asInstanceOf[Failure[C]]))
      }
      a.apply(es).apply(at => combiner ! Left(at))
      b.apply(es).apply(bt => combiner ! Right(bt))
    }
  }

  private def fork[A](a: => Par[A]): Par[A] = es => (cb: Try[A] => Unit) => eval(es) {
    fromTry(Try(a(es)))(es) {
      case Success(cont) => cont.apply(cb)
      case Failure(ex) => cb.apply(Failure(ex))
    }
  }

  private def eval(es: ExecutorService)(r: => Unit): Unit = es.submit(new Callable[Unit] {
    override def call(): Unit = r
  })

  def sequence[A](list: List[Par[A]]): Par[List[A]] = list.foldRight(unit(List.empty[A]))(map2(_, _)(_ :: _))

  def parMap[A, B](list: List[A])(f: A => B): Par[List[B]] = fork {
    list.map(asyncF(f)) |> sequence
  }

  def parFilter[A](as: List[A])(f: A => Boolean): Par[List[A]] = parMap(as)(a => Some(a).filter(f)).map(_.flatten)

  def parFold[A, B](as: List[Par[A]], zero: B)(c: (A, B) => B): Par[B] = {
    as.foldRight(unit(zero)) { case (par, acc) =>
      map2(par, acc)(c)
    }
  }

  def parMapFold[A, B, C](as: List[A], zero: C)(f: A => B)(c: (B, C) => C): Par[C] = fork {
    val mapped = as.map(asyncF(f))
    parFold(mapped, zero)(c)
  }

  def words(chapters: List[String]): Par[Int] = parMapFold(chapters, 0)(_.count(_.isWhitespace) + 1)(_ + _)
}