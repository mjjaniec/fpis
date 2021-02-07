package org.mj.scala.props

import org.mj.scala.{BasicTest, Par}

class PropTest extends BasicTest {
  private final val env = ExecEnv(100, 4)

  it should "work" in {
    val nums = Gen.between(-10, 10)
    val list = SGen.nonEmptyList(nums)
    val prop = Prop.forAll(list) { list =>
      val max = list.max
      list.forall(max >= _)
    }

    assert(prop.check(env) == TestResult.Hold)
  }

  it should "recover" in {
    val nums = Gen.between(-10, 10)
    val list = SGen.listOf(nums)
    val prop = Prop.forAll(list) { list =>
      val max = list.max
      list.forall(max >= _)
    }

    val result = prop.check(env)
    println(result)
    assert(result.falsified)
  }

  it should "falsify" in {
    val nums = Gen.between(-10, 10)
    val list = SGen.nonEmptyList(nums)
    val prop = Prop.forAll(list) { list =>
      val max = list.max
      list.forall(max > _)
    }

    println(prop.check(env))
    assert(prop.check(env).falsified)
  }

  it should "pass example" in {
    val nums = Gen.between(-10, 10)
    val list = SGen.listOf(nums)
    val prop = Prop.forAll(list) { list =>
      list.sorted.sliding(2, 1).forall {
        case Seq(a, b) => a <= b
        case _ => true
      }
    }

    prop.exec(env)
    assert(prop.check(env).hold)
  }

  it should "work with par" in {

    Par.unit(1).map(_ + 1) == Par.unit(2)
  }

}
