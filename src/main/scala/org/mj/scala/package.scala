package org.mj

package object scala {

  implicit class UniversalExt[A](val v: A) extends AnyVal {
    def setup(f: A => Unit): A = { f(v); v }

    def |>[B] (f: A => B): B = f(v)
  }

  implicit class PairExt[A, B](val v: (A, B)) extends AnyVal {
    def mapLeft[C](f: A => C): (C, B) = f(v._1) -> v._2
    def mapRight[C](f: B => C): (A, C) = v._1 -> f(v._2)
  }

}
