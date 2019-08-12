package com.mazhangjing.wsh

import scala.beans.BeanProperty

/**
  * 定义一个便捷的用于确定实验编号的类
  */
object Config {
  @BeanProperty
  var isPreExperiment: Boolean = _
  @BeanProperty
  var experimentNumber: Int = _
}
