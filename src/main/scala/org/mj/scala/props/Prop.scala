package org.mj.scala.props

import org.mj.scala.{Creek, Rng, SimpleRng}

import scala.util.{Success, Try}


trait Prop[A] {
  def check(env: ExecEnv): TestResult[A]


  def &&(other: Prop[A]): Prop[A] = {
    val self: Prop[A] = this
    (env: ExecEnv) => {
      self.check(env) match {
        case TestResult.Hold => other.check(env)
        case fail => fail
      }
    }
  }

  def ||(other: Prop[A]): Prop[A] = {
    val self: Prop[A] = this
    (env: ExecEnv) => {
      self.check(env) match {
        case TestResult.Hold => TestResult.Hold
        case _ => other.check(env)
      }
    }
  }

  def exec(env: ExecEnv): Unit = {
    println(check(env))
  }
}

object Prop {


  def forAll[A](gen: SGen[A])(pred: A => Boolean): Prop[A] = (env: ExecEnv) => {
    val creek: Creek[(A, Int, Try[Boolean])] = Creek.infinite((SimpleRng(env.seed): Rng) -> 0) { case (rng, count) =>
      val (a, s): (A, Rng) = gen(env.inputSize(count)).t(rng)
      (a, count, Try(pred(a))) -> (s, count + 1)
    }

    creek
      .takeN(env.testsCount)
      .dropWhile { case (_, _, p) => p == Success(true) }
      .headOption match {
      case None => TestResult.Hold
      case Some((a, c, p)) =>
        TestResult.Counterexample(a, c, p.failed.getOrElse(new RuntimeException("assertion failed")))
    }
  }
}
