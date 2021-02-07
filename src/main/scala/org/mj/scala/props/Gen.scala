package org.mj.scala
package props

import org.mj.scala.Rand.Rand
import org.mj.scala.props.Gen.Size

case class GenEnv(size: Int, iteration: Int)

trait Gen[+A] extends Function[GenEnv, Rand[A]] {
  def map[B](f: A => B): Gen[B] = this(_).map(f)

  def flatMap[B](f: A => Gen[B]): Gen[B] = size => this(size).flatMap(a => f(a)(size))

  def unfold[B](zero: B)(reduce: (A, B) => Option[B]): Gen[B] = this(_).unfold(zero)(reduce)

  def unfoldTimes[B](times: Int, zero: B)(reduce: (A, B) => B): Gen[B] = this(_).unfoldTimes(times, zero)(reduce)

  def maybe: Gen[Option[A]] = Gen.boolean.flatMap {
    case true => this.map(Some(_))
    case false => Gen.unit(None)
  }

  def get(size: Size = 4, seed: Long = 0): A = this(GenEnv(size, 0)).exec(SimpleRng(seed))
}

object Gen {
  type Size = Int

  def unit[A](a: A): Gen[A] = _ => Rand.unit(a)

  def roundRobin[A](elems: IndexedSeq[A]): Gen[A] = env => Rand.unit(elems(env.iteration % elems.size))

  def union[A](a: Gen[A], b: Gen[A]): Gen[A] = boolean.flatMap {
    case true => a
    case false => b
  }

  def weighted[A](a: Gen[A], b: Gen[A], weight: Double): Gen[A] = double.flatMap(r => if (r < weight) a else b)

  def boolean: Gen[Boolean] = _ => Rand.int.map(i => (i & 1) == 1)

  val smallInt: Gen[Int] = env => Rand.map2(Rand.between(0, env.size + 1), Rand.sign)(_ * _)

  val int: Gen[Int] = env => {
    if (env.size < 8) {
      val bound = 1 << (env.size << 2)
      Rand.between(-bound, bound)
    } else Rand.int
  }

  def double: Gen[Double] = _ => Rand.double

  def between(a: Int, b: Int): Gen[Int] = _ => Rand.between(a, b)

  def alphanum: Gen[Char] = {
    val set = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    between(0, set.length).map(set.charAt)
  }

  def alphanumStrLen(len: Int): Gen[String] =
    alphanum.unfoldTimes(len, new StringBuilder()) { case (c, sb) => sb.append(c) }.map(_.toString())

  def alphanumStr: Gen[String] = env => alphanumStrLen(env.size)(env)

  def listOf[A](gen: Gen[A]): Gen[Seq[A]] = env => listOfN(gen, env.size)(env)

  def nonEmptyListOf[A](gen: Gen[A]): Gen[Seq[A]] = env => listOfN(gen, Math.max(1, env.size))(env)

  def listOfN[A](gen: Gen[A], n: Int): Gen[Seq[A]] = env => gen(env).elems(n)
}