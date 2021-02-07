package org.mj.scala

import scala.collection.mutable

object TransitionTest {
  trait Action[+A] extends Transition[A, String]

  object Action extends TransitionCompanion[String] {
    val fresh: Action[Int] = input => {
      val output = input.length
      val newState = s"fresh: -> $output"
      output -> newState
    }
    val acc: Action[Int] = input => {
      val output = input.length
      val newState = s"acc: ($input) -> $output"
      output -> newState
    }
  }

  object Seqs extends TransitionCompanion[Int] {
    private val letters = "abcdefghijklmnopqrstuvwxyz"

    val square: Transition[Int, Int] = in => (in * in) -> (in + 1)
    val letter: Transition[Char, Int] = in => letters.charAt(in % letters.length) -> (in + 1)
  }
}

class TransitionTest extends BasicTest {
  import TransitionTest._

  it should "have some primitive action" in {
    assert(Action.fresh.exec("makak") == 5)
    val compond = for {
      first <- Action.fresh
      second <- Action.fresh
    } yield second + first

    assert(compond.exec("makak") == 6)
  }

  it should "unit correctly" in {
    assert(Action.unit(3).exec("") == 3)
    assert(Action.unit("dupka").exec("") == "dupka")
  }

  it should "getState correctly" in {
    val compound = for {
      _ <- Action.unit(3)
      state <- Action.getState
    } yield state
    assert(compound.exec("makak") == "makak")
  }

  it should "setState correctly" in {
    val compound = for {
      _ <- Action.unit(3)
      y <- Action.setState("makak")
      z <- Action.getState
    } yield y + z

    assert(compound.exec("dupataka") == "dupatakamakak")
  }

  it should "map2 correctly" in {
    def check(left: Action[Int], right: Action[Int], expectedRes: String, expectedState: String): Unit = {
      val (res, state) = Action.map2(left, right)((a, b) => s"$a,$b")("ala")
      assert(res == expectedRes)
      assert(state == expectedState)
    }

    check(Action.fresh, Action.acc, "3,11", "acc: (fresh: -> 3) -> 11")
    check(Action.acc, Action.fresh, "3,15", "fresh: -> 15")
  }

  it should "sequence correctly" in {

    val fm1 = for {
      a <- Action.acc
      b <- Action.acc
      c <- Action.fresh
    } yield List(a, b, c)

    val sq1 = Action.sequence(Action.acc :: Action.acc :: Action.fresh :: Nil)

    assert(fm1.finalState("") == sq1.finalState(""))

  }

  it should "unfold times correctly" in {
    val squares = Seqs.square.unfoldTimes(3, mutable.Buffer.empty[Int]){ case (a, b) => b.append(a) }
    assert(squares.exec(4).toList == List(16, 25, 36))
  }

  it should "unfold correctly" in {
    val letters = Seqs.letter.unfold(new StringBuilder()){ case (c, sb) => if (c.toInt < 'g'.toInt) Some(sb.append(c)) else None }
    assert(letters.exec(3).toString() == "def")
  }

  it should "generate elems" in {
    val squares = Seqs.square.elems(5)
    assert(squares.exec(-2) == Seq(4, 1, 0, 1, 4))
  }
}
