package org.mj.scala
package props

case class Gen[+A](t: Transition[A, Rng]) {
  def map[B](f: A => B): Gen[B] = Gen(t.map(f))

  def flatMap[B](f: A => Gen[B]): Gen[B] = Gen(t.flatMap(a => f(a).t))

  def unfold[B](zero: B)(reduce: (A, B) => Option[B]): Gen[B] = Gen(t.unfold(zero)(reduce))

  def unfoldTimes[B](times: Int, zero: B)(reduce: (A, B) => B): Gen[B] = Gen(t.unfoldTimes(times, zero)(reduce))

  def get(rng: Rng): A = t.exec(rng)

  def unsized: SGen[A] = SGen.unit(this)

  def maybe: Gen[Option[A]] = Gen.boolean.flatMap {
    case true => this.map(Some(_))
    case false => Gen.unit(None)
  }
}

trait SGen[+A] extends Function[Int, Gen[A]] {

  def map[B](f: A => B): SGen[B] = n => apply(n).map(f)

  def flatMap[B](f: A => SGen[B]): SGen[B] = n => apply(n).flatMap(a => f(a)(n))

  def get(n: Int, rng: Rng): A = apply(n).get(rng)
}

object SGen {
  def unit[A](gen: Gen[A]): SGen[A] = _ => gen

  def listOf[A](gen: Gen[A]): SGen[Seq[A]] = n => Gen.listOfN(gen, n)

  def alphanumStr: SGen[String] = n => Gen.alphanumStr(n)

  def nonEmptyList[A](gen: Gen[A]): SGen[Seq[A]] = n => Gen.listOfN(gen, Math.max(n, 1))

  def int: SGen[Int] = n => Gen(Rand.map2(Rand.between(0, Math.min(1, n)), Rand.sign)(_ * _))

}

object Gen {
  def unit[A](a: A): Gen[A] = Gen(Rand.unit(a))

  def union[A](a: Gen[A], b: Gen[A]): Gen[A] = boolean.flatMap {
    case true => a
    case false => b
  }

  def weighted[A](a: Gen[A], b: Gen[A], weight: Double): Gen[A] = double.flatMap(r => if (r < weight) a else b)

  def boolean: Gen[Boolean] = Gen(Rand.int.map(i => (i & 1) == 1))

  def int: Gen[Int] = Gen(Rand.int)

  def double: Gen[Double] = Gen(Rand.double)

  def between(a: Int, b: Int): Gen[Int] = Gen(Rand.between(a, b))

  def alphanum: Gen[Char] = {
    val set = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    between(0, set.length).map(set.charAt)
  }

  def alphanumStr(len: Int): Gen[String] =
    alphanum.unfoldTimes(len, new StringBuilder()) { case (c, sb) => sb.append(c) }.map(_.toString())

  def aphpanumStr: Gen[String] = between(0, 40).flatMap(alphanumStr)

  def listOfN[A](gen: Gen[A], size: Gen[Int]): Gen[Seq[A]] = size.flatMap(listOfN(gen, _))

  def listOf[A](gen: Gen[A]): Gen[Seq[A]] = between(0, 40).flatMap(listOfN(gen, _))

  def listOfN[A](gen: Gen[A], n: Int): Gen[Seq[A]] = Gen(gen.t.elems(n))
}