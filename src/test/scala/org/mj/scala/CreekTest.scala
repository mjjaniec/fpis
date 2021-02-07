package org.mj.scala

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers


class CreekTest extends AnyFlatSpec with Matchers {
  private val numbersList = List(1, 4, 9, 16, 25, 0)
  private val numbers = Creek.apply(numbersList)


  it should "create valid stream" in {
    assert(numbers.dump() == "1,4,9,16,25,0,")
  }

  it should "create valid list" in {
    assert(numbers.toList == numbersList)
  }

  it should "takeN correctly" in {
    assert(numbers.takeN(100).toList == numbersList)
    assert(numbers.takeN(0).toList == List())
    assert(numbers.takeN(1).toList == List(1))
    assert(numbers.takeN(3).toList == List(1, 4, 9))
  }

  it should "dropN correctly" in {
    assert(numbers.dropN(100).toList == List.empty)
    assert(numbers.dropN(0).toList == List(1, 4, 9, 16, 25, 0))
    assert(numbers.dropN(1).toList == List(4, 9, 16, 25, 0))
    assert(numbers.dropN(3).toList == List(16, 25, 0))
  }

  it should "foldRight correctly" in {
    assert(numbers.foldRight(0)(_ + _) == 55)
    assert(numbers.foldRight(false)((e, b) => e > 3 || b))
    assert(!numbers.foldRight(false)((e, b) => e > 30 || b))
  }

  it should "forAll correctly" in {
    assert(numbers.forAll(_ >= 0))
    assert(!numbers.forAll(_ >= 5))
    assert(!numbers.forAll(_ <= 5))
  }

  it should "takeWhile correctly" in {
    assert(numbers.takeWhile(_ < 8).toList == List(1, 4))
  }

  it should "headOption correctly" in {
    assert(numbers.headOption.contains(1))
    assert(Creek.empty().headOption.isEmpty)
  }

  it should "filter correctly" in {
    assert(numbers.filter(e => (e & 1) == 1).toList == List(1, 9, 25))
  }

  it should "map correctly" in {
    assert(numbers.map(e => Math.sqrt(e).toInt).toList == List(1, 2, 3, 4, 5, 0))
    assert(numbers.map2(e => Math.sqrt(e).toInt).toList == List(1, 2, 3, 4, 5, 0))
  }

  it should "take from infinite stream" in {
    assert(Creek.const(5).takeN(3).toList == List(5, 5, 5))
  }

  it should "from blah blah" in {
    assert(Creek.from(2).takeWhile(_ <= 5).toList == List(2, 3, 4, 5))
  }

  it should "fibs" in {
    assert(Creek.fibs.takeN(7).toList == List(0, 1, 1, 2, 3, 5, 8))
    assert(Creek.fibs2.takeN(7).toList == List(0, 1, 1, 2, 3, 5, 8))
  }

  it should "startsWithR correctly" in {
    startsWithTest((a, b) => a.startsWithR(b))
  }

  it should "startsWith correctly" in {
    startsWithTest((a, b) => a.startsWith(b))
  }

  it should "tails correctly" in {
    assert(numbers.tails.map(s => s"[${s.dump()}]").dump() == "[1,4,9,16,25,0,],[4,9,16,25,0,],[9,16,25,0,],[16,25,0,],[25,0,],[0,],")
  }

  it should "scanRight" in {
    assert(numbers.scanRight(0)(_ + _).toList == List(55, 54, 50, 41, 25, 0))
  }

  it should "scanLeft" in {
    assert(numbers.scanLeft(0)(_ + _).toList == List(1, 5, 14, 30, 55, 55))
  }

  it should "contains correctly" in {
    assert(numbers.contains(numbers))
    assert(numbers.contains(Creek.empty()))
    assert(numbers.contains(Creek(List(1, 4, 9))))
    assert(numbers.contains(Creek(List(25, 0))))

    assert(!numbers.dropN(1).contains(numbers))
    assert(!Creek.empty().contains(numbers))
    assert(!numbers.contains(Creek(List(1, 3, 9))))

    assert(Creek.fibs.contains(Creek(List(0, 1, 1, 2, 3))))
    assert(Creek.fibs.contains(Creek(List(2, 3))))
    assert(Creek.fibs.contains(Creek(List(5, 8, 13, 21))))
  }

  private def startsWithTest(f: (Creek[Int], Creek[Int]) => Boolean): Unit = {
    assert(f(numbers, numbers))
    assert(f(numbers, Creek(List(1, 4, 9))))
    assert(f(numbers, Creek.empty()))

    assert(!f(numbers.dropN(1), numbers))
    assert(!f(numbers, Creek(List(1, 3, 9))))
    assert(!f(Creek.empty(), numbers))

    assert(f(Creek.fibs2, Creek(List(0, 1, 1, 2, 3, 5, 8, 13))))
    assert(!f(Creek(List(0, 1, 4)), Creek.fibs))
  }

  it should "unfold" in {
    println(Creek.unfold(100.0)(s => Option(Math.sqrt(s - 2)).filterNot(_.isNaN).map(s => s -> s)).toList)
  }

  it should "dropWhile" in {
    val creek = Creek(List(3, 5, 8, 2, 1, 7)).dropWhile(_ < 6)
    assert(creek.toList == List(8, 2, 1, 7))
  }

}
