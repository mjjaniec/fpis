package org.mj.scala.props

import org.mj.scala.BasicTest

class PropTest extends BasicTest {
  private final val env = ExecEnv()

  it should "work" in {
    val nums = Gen.between(-10, 10)
    val list = Gen.nonEmptyListOf(nums)
    val prop = Prop.forAll(list) { list =>
      val max = list.max
      list.forall(max >= _)
    }

    assert(prop.check(env) == TestResult.Hold)
  }

  it should "recover" in {
    val nums = Gen.between(-10, 10)
    val list = Gen.listOf(nums)
    val prop = Prop.forAll(list) { list =>
      val max = list.max
      list.forall(max >= _)
    }

    val result = prop.check(env)
    assert(result.falsified)
  }

  it should "falsify" in {
    val list = Gen.nonEmptyListOf(Gen.int)
    val prop = Prop.forAll(list) { list =>
      val max = list.max
      println(list)
      list.forall(max <= _)
    }

    val result = prop.check(env)
    println(result)
    assert(result.falsified)
  }

  it should "pass example" in {
    val nums = Gen.between(-10, 10)
    val list = Gen.listOf(nums)
    val prop = Prop.forAll(list) { list =>
      list.sorted.sliding(2, 1).forall {
        case Seq(a, b) => a <= b
        case _ => true
      }
    }

    prop.exec(env)
    assert(prop.check(env).hold)
  }
}
