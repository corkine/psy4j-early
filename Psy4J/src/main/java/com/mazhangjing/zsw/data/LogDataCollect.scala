package com.mazhangjing.zsw.data

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes

import com.mazhangjing.wsh.data.DataUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * 用于进行曲下线面积拆分的类，调用 DataCollect 进行批处理。
  * @note 2019-03-26 编写此类
  */
object LogDataCollect {

  import java.nio.file._

  val runTask: ArrayBuffer[DataCollect] = mutable.ArrayBuffer[DataCollect]()

  /**
    * 从程序日志中提取时间信息
    * @param file 程序日志文件
    * @return
    */
  private def getFileInfo(file:Path): Option[(String, String, String, String, String)] = {
    import scala.io._
    assert(file.toFile.exists())
    Source.fromFile(file.toFile, "GBK").getLines().find(line => line.contains("Get Left clicked")) match {
      case Some(line) =>
        val nanoTime = line.split("::").last.trim
        val realTime = line.split(" ").head
        Option((file.getParent.toString, file.getFileName.toString, file.getFileName.toString.split("_").head + ".log", realTime, nanoTime))
      case _ => None
    }
  }

  /**
    * 检查是否是程序日志文件
    */
  val checkIfIsLogFile: Path => Boolean = file => {
    val name = file.getFileName.toString
    //日志文件逻辑如下，要区分程序日志和鼠标日志，且不能选择一些临时文件
    name.split("_").length > 1 && !name.startsWith(".")
  }

  def runInJava(computePart: String, produceCoordinateCsvFile: Boolean, fromFolder: String = "."): Unit = {
    DataUtils.printToFile(Paths.get("batch.log")) {

      System.out.println("Data Collect Application - Powered by Scala - @corkine")

      Files.walkFileTree(Paths.get(fromFolder), new FileVisitor[Path] {
        override def preVisitDirectory(dir: Path,
                                       attrs: BasicFileAttributes): FileVisitResult =
          FileVisitResult.CONTINUE

        override def visitFile(file: Path,
                               attrs: BasicFileAttributes): FileVisitResult = file match {
          case _ if checkIfIsLogFile(file) =>
            println(s"Find Files $file")
            try {
              getFileInfo(file) match {
                case None => println(s"没有在文件 $file 中找到对应信息")
                case Some(tup) =>
                  println(s"正在处理文件 $file, 结果将保存到对应名称的 csv 中")
                  val collect = new DataCollect(tup._1, tup._2, tup._3, tup._4, tup._5)
                  collect.produceCoordinateCsvFile = produceCoordinateCsvFile
                  if (computePart.contains("1")) {
                    collect.computeFullArea = false
                    collect.computeCutArea = 1
                  } else if (computePart.contains("2")) {
                    collect.computeFullArea = false
                    collect.computeCutArea = 2
                  } else if (computePart.contains("3")) {
                    collect.computeFullArea = false
                    collect.computeCutArea = 3
                  } else {
                    collect.computeFullArea = true
                    collect.computeCutArea = -1
                  }
                  collectToQueen(collect)
              }
            } catch {
              case ex: Exception => println(s"在处理文件 $file 的过程中发生错误, ${ex.getMessage}")
            }
            FileVisitResult.CONTINUE
          case _ => FileVisitResult.CONTINUE
        }

        override def visitFileFailed(file: Path, exc: IOException): FileVisitResult =
          FileVisitResult.CONTINUE

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          runAllTask()
          FileVisitResult.SKIP_SUBTREE
        }
      })
    }
  }

  private def collectToQueen(collect: DataCollect): Unit = {
    runTask += collect
  }

  private def runAllTask(): Unit = {
    runTask.par.foreach(collect => {
      collect.doAction()
    })
  }
}
