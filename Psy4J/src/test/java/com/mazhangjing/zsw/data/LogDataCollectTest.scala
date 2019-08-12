package com.mazhangjing.zsw.data

import java.nio.file.Paths

import com.mazhangjing.zsw.data.LogDataCollect.runInJava
import org.scalatest.{FlatSpec, Matchers}

class LogDataCollectTest extends FlatSpec with Matchers {

  "Batch Process" should "work well" in {
    runInJava("1",produceCoordinateCsvFile = false, "/Users/corkine/工作文件夹_旧/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example")
  }

  "checkIfLogFile" should "work well" in {
    val bool = LogDataCollect.checkIfIsLogFile(Paths.get("/Users/corkine/工作文件夹_旧/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example/.DS_Store"))
    assert(!bool)
    val bool2 = LogDataCollect.checkIfIsLogFile(Paths.get("/Users/corkine/工作文件夹_旧/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example/1-1.log"))
    assert(!bool2)
    val bool3 = LogDataCollect.checkIfIsLogFile(Paths.get("/Users/corkine/工作文件夹_旧/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example/1-1_毕东娇_女.log"))
    assert(bool3)
  }

}
