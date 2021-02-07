package org.mj.scala.props

case class ExecEnv(testsCount: Int, seed: Long, inputSize: Int => Int = n => Math.sqrt(n).toInt)