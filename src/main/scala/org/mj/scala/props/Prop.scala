package org.mj.scala.props

import org.mj.scala.{Creek, Rng, SimpleRng}

import scala.util.{Success, Try}


trait Prop[A] {
  def check(env: ExecEnv): TestResult[A]

  def exec(env: ExecEnv): Unit = {
    println(check(env))
  }
}

object Prop {
  def forAll[A](gen: Gen[A])(pred: A => Boolean): Prop[A] = (env: ExecEnv) => {
    val creek: Creek[(A, Int, Try[Boolean])] = Creek.infinite((SimpleRng(env.seed): Rng) -> 0) { case (rng, count) =>
      val (a, s): (A, Rng) = gen(GenEnv(env.inputSize(count), count))(rng)
      (a, count, Try(pred(a))) -> (s, count + 1)
    }

    creek
      .takeN(env.testsCount)
      .dropWhile { case (_, _, p) => p == Success(true) }
      .headOption match {
      case None => TestResult.Hold
      case Some((a, c, p)) =>
        TestResult.Falsified(a, c, p.failed.getOrElse(new RuntimeException("assertion failed")))
    }
  }
}
