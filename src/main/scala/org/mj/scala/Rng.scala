package org.mj.scala


trait Rng {
  def nextInt: (Int, Rng)
}

case class SimpleRng(seed: Long) extends Rng {
  override def nextInt: (Int, Rng) = {
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFFL
    val nextInt = (newSeed >>> 16).toInt
    nextInt -> SimpleRng(newSeed)
  }
}


object Rand extends TransitionCompanion[Rng] {

  type Rand[+A] = Transition[A, Rng]

  val int: Rand[Int] = _.nextInt

  val long: Rand[Long] = for {
    l <- int
    r <- int
  } yield l.toLong << 32 | r

  val nonNegativeInt: Rand[Int] = int.flatMap {
    case Int.MinValue => nonNegativeInt
    case other => unit(Math.abs(other))
  }

  val nonNegativeLong: Rand[Long] = long.flatMap {
    case Long.MinValue => nonNegativeLong
    case other => unit(Math.abs(other))
  }

  val double: Rand[Double] = nonNegativeInt.map(_.toDouble / Int.MaxValue)

  def sign: Rand[Int] = int.map(i => (i & 1) * 2 - 1)

  def upToLong(max: Long): Rand[Long] = nonNegativeLong.flatMap { value =>
    val mod = value % max
    if (value + (max - mod) > 0) unit(mod)
    else upToLong(max)
  }

  def between(min: Int, maxEx: Int): Rand[Int] = {
    val diff = maxEx.toLong - min
    upToLong(diff).map(l => (l + min).toInt)
  }

  def ints(n: Int): Rand[Seq[Int]] = int.elems(n)
}


