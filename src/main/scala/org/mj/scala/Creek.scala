package org.mj.scala


sealed trait Creek[+A] {

  import Creek._

  def dump(): String = this match {
    case SNil => ""
    case SNode(h, t) => s"${h()},${t().dump()}"
  }

  def toList: List[A] = this match {
    case SNil => Nil
    case SNode(h, t) => h() :: t().toList
  }

  def takeN(n: Int): Creek[A] = this match {
    case SNode(h, t) if n > 0 => SNode(h, () => t().takeN(n - 1))
    case _ => SNil
  }

  def dropN(n: Int): Creek[A] = this match {
    case SNode(_, t) if n > 0 => t().dropN(n - 1)
    case _else => _else
  }

  def dropWhile(pred: A => Boolean): Creek[A] = this match {
    case SNode(elem, tail) if pred(elem()) => tail().dropWhile(pred)
    case _else => _else
  }

  def takeWhile(pred: A => Boolean): Creek[A] = foldRight(empty[A]())((e, s) => if (pred(e)) cons(e, s) else SNil)

  def filter(pred: A => Boolean): Creek[A] = foldRight(empty[A]())((e, s) => if (pred(e)) cons(e, s) else s)

  def filterNot(pred: A => Boolean): Creek[A] = filter(a => !pred(a))

  def map[B](f: A => B): Creek[B] = foldRight(empty[B]())((e: A, s) => cons(f(e), s))

  def map2[B](f: A => B): Creek[B] = Creek.unfold(this) {
    case SNil => None
    case SNode(h, t) => Some(f(h()) -> t())
  }

  def foldRight[B](zero: B)(combine: (A, => B) => B): B = this match {
    case SNode(h, t) => combine(h(), t().foldRight(zero)(combine))
    case SNil => zero
  }

  def exists(pred: A => Boolean): Boolean = foldRight(false)((elem, rest) => pred(elem) || rest)

  def startsWithR[B >: A](searched: Creek[B]): Boolean = (this, searched) match {
    case (_, SNil) => true
    case (SNil, _) => false
    case (SNode(th, tt), SNode(sh, st)) => th() == sh() && tt().startsWithR(st())
  }

  def startsWith[B >: A](searched: Creek[B]): Boolean = {
    case class State(tRemaining: Creek[A], sRemaining: Creek[B])
    unfold(State(this, searched)) {
      case State(_, SNil) => Some(true, State(SNil, SNil))
      case State(SNode(th, tt), SNode(sh, st)) if th() == sh() => Some(false, State(tt(), st()))
      case _ => None
    }.exists(identity)
  }

  def tails: Creek[Creek[A]] =
    unfold(this) {
      case SNil => None
      case s@SNode(_, t) => Some(s, t())
    }

  def scanRight[B](zero: B)(f: (A, => B) => B): Creek[B] = {
    foldRight(zero -> empty[B]())((elem, state) => {
      val next = f(elem, state._1)
      next -> cons(next, state._2)
    })._2
  }

  def scanLeft[B](zero: B)(f: (A, => B) => B): Creek[B] = {
    unfold((zero, this, false)){
      case (_, _, true) => None
      case (sum, SNil, _) => Some((sum, (sum, SNil, true)))
      case (sum, SNode(h, t), _) => Some((sum, (f(h(), sum), t(), false)))
    }.dropN(1)
  }

  def contains[B >: A](searched: Creek[B]): Boolean = tails.exists(_.startsWith(searched))


  def forAll(pred: A => Boolean): Boolean = foldRight(true)((a, b) => pred(a) && b)

  def headOption: Option[A] = foldRight(Option.empty[A])((a, _) => Some(a))

  override def toString: String = {
    val list = takeN(4).toList
    if (list.length == 4) list.take(3).mkString("[", ", ", ",...]")
    else list.mkString("[", ", ", "]")
  }
}

object Creek {

  case object SNil extends Creek[Nothing]

  case class SNode[+A](h: () => A, t: () => Creek[A]) extends Creek[A]

  def empty[A](): Creek[A] = SNil

  def cons[A](h: => A, t: => Creek[A]): Creek[A] = {
    lazy val lh = h
    lazy val lt = t
    SNode(() => lh, () => lt)
  }

  def zip[A, B](a: Creek[A], b: Creek[B]): Creek[(A, B)] = (a, b) match {
    case (SNode(ah, at), SNode(bh, bt)) => Creek.cons(ah() -> bh(), zip(at(), bt()))
    case _ => SNil
  }

  def const[A](a: A): Creek[A] = cons(a, const(a))

  def from(start: Int): Creek[Int] = cons(start, from(start + 1))

  def fibs: Creek[Int] = {
    def fibs(a: Int, b: Int): Creek[Int] = cons(b, fibs(b, a + b))

    cons(0, fibs(0, 1))
  }

  def fibs2: Creek[Int] = unfold((0, 1))(s => Some(s._1 -> (s._2, s._1 + s._2)))

  def unfold[A, S](z: S)(f: S => Option[(A, S)]): Creek[A] = f(z) match {
    case Some((a, s)) => cons(a, unfold(s)(f))
    case None => empty()
  }

  def infinite[A, S](z: S)(f: S => (A, S)): Creek[A] = unfold(z)(s => Some(f(s)))

  def apply[A](seq: Seq[A]): Creek[A] = seq.headOption.map(h => cons(h, apply(seq.tail))).getOrElse(empty())

}
