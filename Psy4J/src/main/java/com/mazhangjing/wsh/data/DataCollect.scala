package com.mazhangjing.wsh.data
import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes

/**
  * 本代码用来进行批量的、一个文件夹里的 .log 的快速批处理，结果生成 .csv 格式文件
  * 代码调用了 DataProcess 定义的从 .log 提取数据的方法
  */
object DataCollect {
  import java.nio.file._
  //C:/工作文件夹/cmPsyLab/Lab/Lab/src/main/resources/wsh_data

  def getFileInfo(file:Path): Option[(String, String, String, String)] = {
    import scala.io._
    assert(file.toFile.exists())
    Source.fromFile(file.toFile, "GBK").getLines().find(line => line.contains("Get result and go now")) match {
      case Some(line) =>
        val nanoTime = line.split(":::").last.trim
        val realTime = line.split(" ").head
        Option((file.getParent.toString, file.getFileName.toString, realTime, nanoTime))
      case _ => None
    }
  }

  def runInJava(): Unit = {
    DataUtils.printToFile(Paths.get("batch.log")) {
      System.out.println("Data Collect Application - Powered by Scala - @corkine")
      Files.walkFileTree(Paths.get("."), new FileVisitor[Path] {
        override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
          FileVisitResult.CONTINUE

        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = file match {
          case _ if file.getFileName.toString.endsWith(".log") =>
            println(s"Find Files $file")
            try {
              getFileInfo(file) match {
                case None => println(s"没有在文件 $file 中找到对应信息")
                case Some(tup) =>
                  println(s"正在处理文件 $file, 结果将保存到对应名称的 csv 中")
                  new DataProcess(tup._1 ,tup._2, tup._3, tup._4).doAction()
              }
            } catch {
              case ex: Exception => println(s"在处理文件 $file 的过程中发生错误, ${ex.getMessage}")
            }
            FileVisitResult.CONTINUE
          case _ => FileVisitResult.CONTINUE
        }

        override def visitFileFailed(file: Path, exc: IOException): FileVisitResult =
          FileVisitResult.CONTINUE

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult =
          FileVisitResult.CONTINUE
      })
    }
  }

  def main(args: Array[String]): Unit = {
    runInJava()
  }

}
