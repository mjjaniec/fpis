package org.mj.scala

import scala.collection.mutable

trait Transition[+A, S] extends Function[S, (A, S)] {
  type TT[+X] = Transition[X, S]

  def map[B](f: A => B): TT[B] = in => {
    val (a, s) = this.apply(in)
    (f(a), s)
  }

  def flatMap[B](f: A => TT[B]): TT[B] = in => {
    val (a, s) = this.apply(in)
    f(a).apply(s)
  }

  def unfold[B](zero: B)(reduce: (A, B) => Option[B]): TT[B] = in => {
    var state = in
    var b = zero
    var continue = true
    while(continue) {
      val (a, s) = apply(state)
      state = s
      val opt = reduce(a, b)
      continue = opt.isDefined
      opt.foreach(b = _)
    }
    b -> state
  }

  def unfoldTimes[B](times: Int, zero: B)(reduce: (A, B) => B): TT[B] =
    unfold(zero -> times) {
      case (_, (_, 0)) => None
      case (a, (b, n)) => Some(reduce(a, b) -> (n - 1))
    }.map(_._1)

  def elems(n: Int): TT[Seq[A]] =
    unfoldTimes(n, mutable.Buffer.empty[A]) { case (a, buffer) => buffer.append(a) }.map(_.toSeq)

  def exec(state: S): A = this (state)._1

  def finalState(state: S): S = this (state)._2
}

class TransitionCompanion[S] {
  type TT[+A] = Transition[A, S]

  def unit[A](a: A): TT[A] = s => a -> s

  def map2[A, B, C](ra: TT[A], rb: TT[B])(f: (A, B) => C): TT[C] =
    ra.flatMap(a => rb.map(f(a, _)))

  def setState(newState: S): TT[S] = current => current -> newState

  def getState: TT[S] = in => in -> in

  def both[A, B](ta: TT[A], tb: TT[B]): TT[(A, B)] = map2(ta, tb)(_ -> _)

  def sequence[A](input: List[TT[A]]): TT[List[A]] = sequence2(input)

  def sequence1[A](input: List[TT[A]]): TT[List[A]] = input match {
    case Nil => unit(Nil)
    case h :: t => map2(h, sequence1(t))(_ :: _)
  }

  def sequence2[A](input: List[TT[A]]): TT[List[A]] = {
    var res = unit(List.empty[A])
    for (action <- input)
      res = res.flatMap(acc => action.flatMap(a => unit(a :: acc)))
    res.map(_.reverse)
  }
}
