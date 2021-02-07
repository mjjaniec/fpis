package org.mj.scala.props

import org.mj.scala.{BasicTest, Rng, SimpleRng, UniversalExt}

class GenTest extends BasicTest {

  private val rng: Rng = SimpleRng(3)

  it should "genUnit" in {
    assert(Gen.unit(3).get(rng) == 3)
  }

  it should "genInts" in {
    val list = Gen.listOfN(Gen.int, 3).get(rng)

    assert(list.length == 3)
    assert(list.sliding(2, 1).forall { case List(a, b) => a != b })
    list |> println
  }

}
