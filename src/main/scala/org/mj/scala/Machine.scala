package org.mj.scala

import org.mj.scala.Action.Action

sealed trait Input
case object Coin extends Input
case object Turn extends Input

sealed trait Output
case object CandyReady extends Output
case object CandyDispensed extends Output
case object Ignored extends Output

case class Machine(locked: Boolean, candies: Int, coins: Int)


object Action extends TransitionCompanion[Machine] {
  type Action[+A] = Transition[A, Machine]
  case class AnnotatedAction[+A](description: String, action: Action[A]) extends Action[A] {
    override def apply(v1: Machine): (A, Machine) = action.apply(v1)

    override def toString(): String = description
  }

  def forInput(input: Input): Action[Output] = {
    AnnotatedAction(s"action for $input", machine => {
      println(s"Input: $input, machine: $machine")
      val (output, newSate) = (input, machine) match {
        case (Coin, Machine(true, candies, coins)) if candies > 0 =>
          CandyReady -> Machine(locked = false, candies - 1, coins + 1)
        case (Turn, Machine(false, candies, coins)) =>
          CandyDispensed -> Machine(locked = true, candies, coins)
        case (_, machine) =>
          Ignored -> machine
      }
      println(s"Output: $output, machine: $machine")
      output -> newSate
    })
  }
}

object Machine {
  def simulateMachine(input: List[Input]): Action[List[Output]] = {
    Action.sequence(input.map(Action.forInput))
  }
}