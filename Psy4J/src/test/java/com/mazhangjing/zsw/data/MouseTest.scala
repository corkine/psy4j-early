package com.mazhangjing.zsw.data;

import java.nio.file.{Path, Paths}

import com.mazhangjing.wsh.data.DataUtils

import scala.collection.mutable
import scala.io.Source

/**
  * 鼠标信息测试类
  */
object MouseTest extends App {
        private val file: Path = Paths.get("/Users/corkine/工作文件夹/cmPsyLab/Lab/Lab/src/test/1-4.log")
        def pathLogAndPrint(file: Path): Unit = {
                val array = Source.fromFile(file.toFile).getLines()
                  .filter(_.contains("Mouse Moved:")).map(_.split(" ")(0))
                  .map(_.toDouble).toArray
                val diff = {
                        val list = mutable.ListBuffer[Double]()
                        for (i <- array.indices) {
                                var diffNumber = 0.0
                                if (i + 1 < array.length) {
                                        val currentNumber = array(i)
                                        val nextNumber = array(i + 1)
                                        diffNumber = nextNumber - currentNumber
                                } else diffNumber = 0.0
                                list.append(diffNumber)
                        }
                        list
                }
                println((diff.sum/diff.length).toInt)
        }

        DataUtils.walkAndProcess(Paths.get("/Users/corkine/工作文件夹/cmPsyLab/Lab/Lab/src/test"),
                _.getFileName.toString.contains(".log"))(file => {
                pathLogAndPrint(file)
        })
}