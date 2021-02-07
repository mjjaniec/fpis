package org.mj.scala.props

case class ExecEnv(testsCount: Int = 100, seed: Long = System.currentTimeMillis(), inputSize: Int => Int = n => Math.sqrt(n).toInt)