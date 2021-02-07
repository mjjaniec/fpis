package org.mj.scala.props

trait TestResult[+A] {
  def falsified: Boolean
  def hold: Boolean = !falsified
}

object TestResult {
  final case object Hold extends TestResult[Nothing] {
    override def falsified: Boolean = false
  }
  final case class Counterexample[A](example: A, after: Int, reason: Throwable) extends TestResult[A] {
    override def falsified: Boolean = true
  }
}
