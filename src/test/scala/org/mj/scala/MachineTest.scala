package org.mj.scala

class MachineTest extends BasicTest {


  it should "wurks" in {
    val input = Machine(locked = true, 3, 3)

    val transition = Machine.simulateMachine(Coin :: Coin :: Turn :: Nil)

    assert(transition(input)._1 == List(CandyReady, Ignored, CandyDispensed))
  }
}
