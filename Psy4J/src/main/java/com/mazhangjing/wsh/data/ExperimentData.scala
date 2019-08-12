package com.mazhangjing.wsh.data

import java.time.Instant
import java.util

import com.mazhangjing.wsh.SET

/**
  * 本类定义了 wsh 包的程序收集到的一个被试的 Experiment 以及其若干个 Trial 的数据。
  * 本类尚未真正使用，目前使用的是 .log 文件中提取数据的方法。
  */
class ExperimentData extends Serializable {
  var userId = ""
  var userName = ""
  var userInfo = ""
  val trialsData: util.ArrayList[TrialData] = new util.ArrayList[TrialData]()
}

class TrialData extends Serializable {
  //ID, SHOW_TIME, ACTION_TIME, SHOW_TIME_MS, DURATION_TIME_MS,
  //STAND_SIZE, ACTION_SIZE, SIZE_IS_BIG, ACTION_ORDER, STI_IS_LEFT, CHECK_BY
  var isNormal: Boolean = _
  var checkBySizeOrOrder: String = _
  var showByLeftOrRight: String = _
  var orderIndex: Int = _
  var stimulateRadius: Int = _
  var showStiTimeInstant: Instant = _
  var showStiNanoTime: Long = _

  var showTimeMSFake: Int = 0
  var standSize:Int = SET.PIXEL_270_SIZE.getValue
  def sizeIsBig: Boolean = {
    if (stimulateRadius > standSize) true else false
  }

  var actionTimeInstant: Instant = _
  var actionTimeNanoTime: Long = _
  def durationNanoMS: Int = {
    if (actionTimeNanoTime == 0) 0
    else (actionTimeNanoTime - showStiNanoTime).toInt
  }
}
