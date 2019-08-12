package com.mazhangjing.zsw.data

import java.io._
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.mazhangjing.wsh.data.DataUtils

import scala.collection.mutable
import scala.io.Source

/**
  * 本脚本用于执行 .csv 结果的数据剔除、标准差过滤、分组和输出
  * 主要操作定义在 mainOps 中，每个条件的每个被试 trial 数据格式为 Data，一个被试数据格式为 Subject
  * 数据在后期添加了适配到 SPSS 的选项，即新建了数据结构 Table 和 Line，
  * 其中 Line 用于保存平均化的 Subject 在一个条件下的所有被试的平均数据，Line 的数据被保存在 Table 中。
  */
object DataBatch {

  var lookForSize = false

  /**
    * 遍历文件夹，根据过滤器和操作，对文件分别执行，并行执行。
    * @param filter 过滤器，判断一个文件是否满足条件
    * @param op 对满足过滤器的文件执行操作
    * @return 遍历的文件路径
    */
  def walkFolderAndRunFunction(path: Path)(filter: Path => Boolean, op: File => Unit): Path = {
    Files.walkFileTree(path, new FileVisitor[Path] {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = FileVisitResult.CONTINUE
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        file match {
          case csvFile if filter(csvFile) =>
            System.out.println(s"\n\n===================================== ${if (lookForSize) "寻找尺寸" else "寻找数量"} " +
              s"- ${file.getFileName} =============================================\n")
              op(file.toFile)
            try {
            } catch {
              case ex: Exception => System.out.println(s"Error with exception when handling File ${file.getFileName}: ${ex.getMessage}")
            }
          case _ =>
        }
        FileVisitResult.CONTINUE
      }
      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = FileVisitResult.CONTINUE
      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        try { Table.outputTableData() } catch { case e: Throwable => println(e) }
        FileVisitResult.CONTINUE
      }
    })
  }

  /**
    * 主操作，在其中将每个 CSV 文件转换为对应的 Buffer 集合， --> changeFileToDataCollect
    * 然后为每个被试的每一行计算得到 Area、Init、MT、RT 四个因变量的值， --> Data Object
    * 将其按照是否异步区分组，
    * 每组根据三个标准差删除不合适的行，最后计算每个被试 8 个因变量的平均数
    * @param file 文件
    */
  def mainOps(file: File): Unit = {
    var buffer = changeFileToDataCollect(file)
    System.out.println("Wrong Count is: " + buffer.count(data => !data.answer_check))
    buffer = buffer.filter(data => data.answer_check)
    val subject = Subject(buffer.toArray)
    if (lookForSize) {
      val d0c1fb0 = subject.printFilteredDataWithIn3SD(subject.conNumber0DataD0, "d2 - 一致 -  NUMBER_FIRST_BIG = 0")
      val d0c1fb1 = subject.printFilteredDataWithIn3SD(subject.conNumber1DataD0,"d2 - 一致 -  NUMBER_FIRST_BIG = 1")
      val d0c1fb2 = subject.printFilteredDataWithIn3SD(subject.conNumber2DataD0,"d2 - 一致 -  NUMBER_FIRST_BIG = 2")
      val d0c0fb0 = subject.printFilteredDataWithIn3SD(subject.noConNumber0DataD0,"d2 - 不一致 -  NUMBER_FIRST_BIG = 0")
      val d0c0fb1 = subject.printFilteredDataWithIn3SD(subject.noConNumber1DataD0,"d2 - 不一致 -  NUMBER_FIRST_BIG = 1")
      val d0c0fb2 = subject.printFilteredDataWithIn3SD(subject.noConNumber2DataD0,"d2 - 不一致 -  NUMBER_FIRST_BIG = 2")

      val d1c1fb0 = subject.printFilteredDataWithIn3SD(subject.conNumber0DataD1, "d5 - 一致 -  NUMBER_FIRST_BIG = 0")
      val d1c1fb1 = subject.printFilteredDataWithIn3SD(subject.conNumber1DataD1,"d5 - 一致 -  NUMBER_FIRST_BIG = 1")
      val d1c1fb2 = subject.printFilteredDataWithIn3SD(subject.conNumber2DataD1,"d5 - 一致 -  NUMBER_FIRST_BIG = 2")
      val d1c0fb0 = subject.printFilteredDataWithIn3SD(subject.noConNumber0DataD1,"d5 - 不一致 -  NUMBER_FIRST_BIG = 0")
      val d1c0fb1 = subject.printFilteredDataWithIn3SD(subject.noConNumber1DataD1,"d5 - 不一致 -  NUMBER_FIRST_BIG = 1")
      val d1c0fb2 = subject.printFilteredDataWithIn3SD(subject.noConNumber2DataD1,"d5 - 不一致 -  NUMBER_FIRST_BIG = 2")

      val c1 = subject.printFilteredDataWithIn3SD(subject.conAllData,"一致 - 总体")
      val c0 = subject.printFilteredDataWithIn3SD(subject.noConAllData,"不一致 - 总体")

      val c1fb0 = subject.printFilteredDataWithIn3SD(subject.conNumber0DataD0 ++ subject.conNumber0DataD1 , "一致 -  NUMBER_FIRST_BIG = 0")
      val c1fb1 = subject.printFilteredDataWithIn3SD(subject.conNumber1DataD0 ++ subject.conNumber1DataD1,"一致 -  NUMBER_FIRST_BIG = 1")
      val c1fb2 = subject.printFilteredDataWithIn3SD(subject.conNumber2DataD0 ++ subject.conNumber2DataD1,"一致 -  NUMBER_FIRST_BIG = 2")
      val c0fb0 = subject.printFilteredDataWithIn3SD(subject.noConNumber0DataD0 ++ subject.noConNumber0DataD1,"不一致 -  NUMBER_FIRST_BIG = 0")
      val c0fb1 = subject.printFilteredDataWithIn3SD(subject.noConNumber1DataD0 ++ subject.noConNumber1DataD1,"不一致 -  NUMBER_FIRST_BIG = 1")
      val c0fb2 = subject.printFilteredDataWithIn3SD(subject.noConNumber2DataD0 ++ subject.noConNumber2DataD1,"不一致 -  NUMBER_FIRST_BIG = 2")

      val c0d0 = subject.printFilteredDataWithIn3SD(subject.noConDistance0,"C0D0")
      val c1d0 = subject.printFilteredDataWithIn3SD(subject.conDistance0,"C1D0")
      val c0d1 = subject.printFilteredDataWithIn3SD(subject.noConDistance1,"C0D1")
      val c1d1 = subject.printFilteredDataWithIn3SD(subject.conDistance1,"C1D1")

      Table.AREA_Line += Line(d0c0fb0._1, d0c0fb1._1, d0c0fb2._1, d0c1fb0._1, d0c1fb1._1, d0c1fb2._1,
                              d1c0fb0._1, d1c0fb1._1, d1c0fb2._1, d1c1fb0._1, d1c1fb1._1, d1c1fb2._1, c0._1, c1._1,
                              c0fb0._1, c0fb1._1, c0fb2._1, c1fb0._1, c1fb1._1, c1fb2._1, c0d0._1, c1d0._1, c0d1._1, c1d1._1)
      Table.INIT_Line += Line(d0c0fb0._2, d0c0fb1._2, d0c0fb2._2, d0c1fb0._2, d0c1fb1._2, d0c1fb2._2,
                              d1c0fb0._2, d1c0fb1._2, d1c0fb2._2, d1c1fb0._2, d1c1fb1._2, d1c1fb2._2, c0._2, c1._2,
                              c0fb0._2, c0fb1._2, c0fb2._2, c1fb0._2, c1fb1._2, c1fb2._2, c0d0._2, c1d0._2, c0d1._2, c1d1._2)
      Table.MT_Line += Line(d0c0fb0._3, d0c0fb1._3, d0c0fb2._3, d0c1fb0._3, d0c1fb1._3, d0c1fb2._3,
                            d1c0fb0._3, d1c0fb1._3, d1c0fb2._3, d1c1fb0._3, d1c1fb1._3, d1c1fb2._3, c0._3, c1._3,
                            c0fb0._3, c0fb1._3, c0fb2._3, c1fb0._3, c1fb1._3, c1fb2._3, c0d0._3, c1d0._3, c0d1._3, c1d1._3)
      Table.RT_Line += Line(d0c0fb0._4, d0c0fb1._4, d0c0fb2._4, d0c1fb0._4, d0c1fb1._4, d0c1fb2._4,
                            d1c0fb0._4, d1c0fb1._4, d1c0fb2._4, d1c1fb0._4, d1c1fb1._4, d1c1fb2._4, c0._4, c1._4,
                            c0fb0._4, c0fb1._4, c0fb2._4, c1fb0._4, c1fb1._4, c1fb2._4, c0d0._4, c1d0._4, c0d1._4, c1d1._4)
    } else {
      val d0c1fb0 = subject.printFilteredDataWithIn3SD(subject.conSize0DataD0, "d2 - 一致 -  SIZE_FIRST_BIG = 0")
      val d0c1fb1 = subject.printFilteredDataWithIn3SD(subject.conSize1DataD0,"d2 - 一致 -  SIZE_FIRST_BIG = 1")
      val d0c1fb2 = subject.printFilteredDataWithIn3SD(subject.conSize2DataD0,"d2 - 一致 -  SIZE_FIRST_BIG = 2")
      val d0c0fb0 = subject.printFilteredDataWithIn3SD(subject.noConSize0DataD0,"d2 - 不一致 -  SIZE_FIRST_BIG = 0")
      val d0c0fb1 = subject.printFilteredDataWithIn3SD(subject.noConSize1DataD0,"d2 - 不一致 -  SIZE_FIRST_BIG = 1")
      val d0c0fb2 = subject.printFilteredDataWithIn3SD(subject.noConSize2DataD0,"d2 - 不一致 -  SIZE_FIRST_BIG = 2")

      val d1c1fb0 = subject.printFilteredDataWithIn3SD(subject.conSize0DataD1, "d5 - 一致 -  SIZE_FIRST_BIG = 0")
      val d1c1fb1 = subject.printFilteredDataWithIn3SD(subject.conSize1DataD1,"d5 - 一致 -  SIZE_FIRST_BIG = 1")
      val d1c1fb2 = subject.printFilteredDataWithIn3SD(subject.conSize2DataD1,"d5 - 一致 -  SIZE_FIRST_BIG = 2")
      val d1c0fb0 = subject.printFilteredDataWithIn3SD(subject.noConSize0DataD1,"d5 - 不一致 -  SIZE_FIRST_BIG = 0")
      val d1c0fb1 = subject.printFilteredDataWithIn3SD(subject.noConSize1DataD1,"d5 - 不一致 -  SIZE_FIRST_BIG = 1")
      val d1c0fb2 = subject.printFilteredDataWithIn3SD(subject.noConSize2DataD1,"d5 - 不一致 -  SIZE_FIRST_BIG = 2")

      val c1 = subject.printFilteredDataWithIn3SD(subject.conAllData,"一致 - 总体")
      val c0 = subject.printFilteredDataWithIn3SD(subject.noConAllData,"不一致 - 总体")

      val c1fb0 = subject.printFilteredDataWithIn3SD(subject.conSize0DataD0 ++ subject.conSize0DataD1 , "一致 -  SIZE_FIRST_BIG = 0")
      val c1fb1 = subject.printFilteredDataWithIn3SD(subject.conSize1DataD0 ++ subject.conSize1DataD1,"一致 -  SIZE_FIRST_BIG = 1")
      val c1fb2 = subject.printFilteredDataWithIn3SD(subject.conSize2DataD0 ++ subject.conSize2DataD1,"一致 -  SIZE_FIRST_BIG = 2")
      val c0fb0 = subject.printFilteredDataWithIn3SD(subject.noConSize0DataD0 ++ subject.noConSize0DataD1,"不一致 -  SIZE_FIRST_BIG = 0")
      val c0fb1 = subject.printFilteredDataWithIn3SD(subject.noConSize1DataD0 ++ subject.noConSize1DataD1,"不一致 -  SIZE_FIRST_BIG = 1")
      val c0fb2 = subject.printFilteredDataWithIn3SD(subject.noConSize2DataD0 ++ subject.noConSize2DataD1,"不一致 -  SIZE_FIRST_BIG = 2")


      val c0d0 = subject.printFilteredDataWithIn3SD(subject.noConDistance0,"C0D0")
      val c1d0 = subject.printFilteredDataWithIn3SD(subject.conDistance0,"C1D0")
      val c0d1 = subject.printFilteredDataWithIn3SD(subject.noConDistance1,"C0D1")
      val c1d1 = subject.printFilteredDataWithIn3SD(subject.conDistance1,"C1D1")

      Table.AREA_Line += Line(d0c0fb0._1, d0c0fb1._1, d0c0fb2._1, d0c1fb0._1, d0c1fb1._1, d0c1fb2._1,
        d1c0fb0._1, d1c0fb1._1, d1c0fb2._1, d1c1fb0._1, d1c1fb1._1, d1c1fb2._1, c0._1, c1._1,
        c0fb0._1, c0fb1._1, c0fb2._1, c1fb0._1, c1fb1._1, c1fb2._1, c0d0._1, c1d0._1, c0d1._1, c1d1._1)
      Table.INIT_Line += Line(d0c0fb0._2, d0c0fb1._2, d0c0fb2._2, d0c1fb0._2, d0c1fb1._2, d0c1fb2._2,
        d1c0fb0._2, d1c0fb1._2, d1c0fb2._2, d1c1fb0._2, d1c1fb1._2, d1c1fb2._2, c0._2, c1._2,
        c0fb0._2, c0fb1._2, c0fb2._2, c1fb0._2, c1fb1._2, c1fb2._2, c0d0._2, c1d0._2, c0d1._2, c1d1._2)
      Table.MT_Line += Line(d0c0fb0._3, d0c0fb1._3, d0c0fb2._3, d0c1fb0._3, d0c1fb1._3, d0c1fb2._3,
        d1c0fb0._3, d1c0fb1._3, d1c0fb2._3, d1c1fb0._3, d1c1fb1._3, d1c1fb2._3, c0._3, c1._3,
        c0fb0._3, c0fb1._3, c0fb2._3, c1fb0._3, c1fb1._3, c1fb2._3, c0d0._3, c1d0._3, c0d1._3, c1d1._3)
      Table.RT_Line += Line(d0c0fb0._4, d0c0fb1._4, d0c0fb2._4, d0c1fb0._4, d0c1fb1._4, d0c1fb2._4,
        d1c0fb0._4, d1c0fb1._4, d1c0fb2._4, d1c1fb0._4, d1c1fb1._4, d1c1fb2._4, c0._4, c1._4,
        c0fb0._4, c0fb1._4, c0fb2._4, c1fb0._4, c1fb1._4, c1fb2._4, c0d0._4, c1d0._4, c0d1._4, c1d1._4)
    }
  }

  /**
    * 将每个文件中的数据收集为数据对象
    * @param file CSV 格式文件
    * @return 每个 CSV 文件的数据格式对象集合
    */
  def changeFileToDataCollect(file: File): mutable.Buffer[Data] = {
    System.out.println(s"Parse Buffer from File ${file.getName}")
    val buffer = Source.fromFile(file,"UTF-8").getLines().toBuffer
    val result = buffer.filterNot(_.isEmpty).map(lineToObject).filterNot(op => op.isEmpty).map(op => op.get)
    result
  }

  /**
    * 将每行的数据转换为数据结构，并返回
    * @param line 数据行
    * @return 数据结构
    */
  def lineToObject(line: String): Option[Data] = {
    var strings:Array[String] = null
    if (!line.contains("\t")) {
      strings = line.split(",").filterNot(_.isEmpty).map(_.trim)
      if (strings.length != 15) throw new IllegalAccessException(s"传入行的数据长度不正确： ${line.length}")
      //如果是首行，则跳过
      if (strings(0) == "TIME" && strings(1) == "ANSWER") return None
    } else {
      strings = line.split("\t").filterNot(_.isEmpty).map(_.trim)
      if (strings.length != 13) throw new IllegalAccessException(s"传入行的数据长度不正确： ${line.length}")
      //如果是首行，则跳过
      if (strings(0) == "TIME" && strings(1) == "ANSWER") return None
    }

    var data: Data = null
    if (line.contains("\t")) {
      data = Data(
        strings(0).toLong, //TIME
        strings(1), //ANSWER
        strings(2).trim.toUpperCase == "RIGHT", //ANSWER_CHECK
        strings(3).toInt, //LEFT_NUMBER
        strings(4).toInt, //LEFT_SIZE
        strings(5).toInt, //RIGHT_NUMBER
        strings(6).toInt, //RIGHT_SIZE
        Int.MinValue,
        Int.MinValue,
        strings(9-2).toInt == 1, //IS_CONGRUENCE
        strings(10-2).toLong, //CLICK_BTN
        strings(11-2).toLong, //MOUSE_MOVE
        strings(12-2).toLong, //NEAR_ACTION_TIME
        strings(13-2).toLong, //NEAR_MOUSE_MOVE
        strings(14-2).toLong) //AREA
    } else {
      data = Data(
        strings(0).toLong, //TIME
        strings(1), //ANSWER
        strings(2).trim.toUpperCase == "RIGHT", //ANSWER_CHECK
        strings(3).toInt, //LEFT_NUMBER
        strings(4).toInt, //LEFT_SIZE
        strings(5).toInt, //RIGHT_NUMBER
        strings(6).toInt, //RIGHT_SIZE
        strings(7).toInt, //NUMBER_FIRST_BIG
        strings(8).toInt, //SIZE_FIRST_BIG
        strings(9).toInt == 1, //IS_CONGRUENCE
        strings(10).toLong, //CLICK_BTN
        strings(11).toLong, //MOUSE_MOVE
        strings(12).toLong, //NEAR_ACTION_TIME
        strings(13).toLong, //NEAR_MOUSE_MOVE
        strings(14).toLong //AREA
      )
    }

    Option(data)
  }

  def main(args: Array[String]): Unit = {
    System.out.println("ZSW Experiment DataCollect, Version 0.1.0, Author: Corkine Ma @CCNU, Powered By Scala")
    walkFolderAndRunFunction(Paths.get("/Users/corkine/工作文件夹/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example"))(_.toFile.toString.endsWith(".csv"), mainOps)
  }

  def runInJava(path: Path, lookForSize: Boolean): Unit = {
    System.out.println("ZSW Experiment DataCollect, Version 0.1.0, Author: Corkine Ma @CCNU, Powered By Scala")
    this.lookForSize = lookForSize
    val out = System.out
    val fileOut = new PrintStream("data_collect.log")
    System.setOut(fileOut)
    walkFolderAndRunFunction(path)(_.toFile.toString.endsWith(".csv"), mainOps)
    fileOut.flush()
    fileOut.close()
    System.setOut(out)
  }
}

/**
  * 数据结构
  * @param time 反应时的时间
  * @param answer LEFT or RIGHT
  * @param answer_check 反应是否正确
  * @param left_number 左边数字
  * @param left_size 左边数字大小
  * @param right_number 右边数字
  * @param right_size 右边数字大小
  * @param number_first_big 首先呈现的数字数值是否大
  * @param size_first_big 首先呈现的数字尺寸是否大
  * @param is_congruence 是否异步呈现
  * @param click_btn 按键时的时间
  * @param mouse_move 鼠标移动时的时间
  * @param near_action_time 估算的反应时的时间
  * @param near_mouse_move 估算的鼠标移动时的时间
  * @param area 鼠标移动的面积
  */
case class Data(
                 time: Long,
                 answer: String,
                 answer_check: Boolean,
                 left_number: Int,
                 left_size: Int,
                 right_number: Int,
                 right_size: Int,
                 number_first_big: Int,
                 size_first_big: Int,
                 is_congruence: Boolean,
                 click_btn: Long,
                 mouse_move: Long,
                 near_action_time: Long,
                 near_mouse_move: Long,
                 area: Long
               ) {
  def getArea: Long = area
  def getInit: Long = mouse_move - click_btn
  def getMT: Long = time - mouse_move
  def getRT: Long = time - click_btn
  def distanceIsLong: Boolean = {
    val res = math.abs(left_number - right_number)
    if (res == 2) false
    else if (res == 5) true
    else throw new RuntimeException("不可能的数值")
  }
}

case class Line(D0C0FB0: Double,
                D0C0FB1: Double,
                D0C0FB2: Double,
                D0C1FB0: Double,
                D0C1FB1: Double,
                D0C1FB2: Double,
                D1C0FB0: Double,
                D1C0FB1: Double,
                D1C0FB2: Double,
                D1C1FB0: Double,
                D1C1FB1: Double,
                D1C1FB2: Double,
                C0: Double,
                C1: Double,
                C0FB0: Double,
                C0FB1: Double,
                C0FB2: Double,
                C1FB0: Double,
                C1FB1: Double,
                C1FB2: Double,
                D0C0: Double,
                D0C1: Double,
                D1C0: Double,
                D1C1: Double
               ) {
  val D0C0FB0_D0C1FB0: Double = (D0C0FB0 - D0C1FB0)/D0C1FB0
  val D0C0FB1_D0C1FB1: Double = (D0C0FB1 - D0C1FB1)/D0C1FB1
  val D0C0FB2_D0C1FB2: Double = (D0C0FB2 - D0C1FB2)/D0C1FB2
  val D1C0FB0_D1C1FB0: Double = (D1C0FB0 - D1C1FB0)/D1C1FB0
  val D1C0FB1_D1C1FB1: Double = (D1C0FB1 - D1C1FB1)/D1C1FB1
  val D1C0FB2_D1C1FB2: Double = (D1C0FB2 - D1C1FB2)/D1C1FB2
  val C0FB0_C1FB0_With_Divide: Double = (C0FB0 - C1FB0)/C1FB0
  val C0FB1_C1FB1_With_Divide: Double = (C0FB1 - C1FB1)/C1FB1
  val C0FB2_C1FB2_With_Divide: Double = (C0FB2 - C1FB2)/C1FB2
  val C0FB0_C1FB0: Double = C0FB0 - C1FB0
  val C0FB1_C1FB1: Double = C0FB1 - C1FB1
  val C0FB2_C1FB2: Double = C0FB2 - C1FB2
  val D0C0_D0C1: Double = D0C0 - D0C1
  val D1C0_D1C1: Double = D1C0 - D1C1

  /**
    * 获取没有 D 的
    * @return
    */
  def getLine: String = {
    val a = new mutable.StringBuilder()
    a.append(D0C0FB0).append(", ")
    a.append(D0C0FB1).append(", ")
    a.append(D0C0FB2).append(", ")
    a.append(D0C1FB0).append(", ")
    a.append(D0C1FB1).append(", ")
    a.append(D0C1FB2).append(", ")
    a.append(D1C0FB0).append(", ")
    a.append(D1C0FB1).append(", ")
    a.append(D1C0FB2).append(", ")
    a.append(D1C1FB0).append(", ")
    a.append(D1C1FB1).append(", ")
    a.append(D1C1FB2).append(", ")
    a.append(C0).append(", ")
    a.append(C1).append(", ")
    a.append(D0C0FB0_D0C1FB0).append(", ")
    a.append(D0C0FB1_D0C1FB1).append(", ")
    a.append(D0C0FB2_D0C1FB2).append(", ")
    a.append(D1C0FB0_D1C1FB0).append(", ")
    a.append(D1C0FB1_D1C1FB1).append(", ")
    a.append(D1C0FB2_D1C1FB2)
    a.toString()
  }

  def getLineWithOutDistance: String = {
    val a = new mutable.StringBuilder()
    a.append(C0FB0).append(", ")
    a.append(C0FB1).append(", ")
    a.append(C0FB2).append(", ")
    a.append(C1FB0).append(", ")
    a.append(C1FB1).append(", ")
    a.append(C1FB2).append(", ")
    a.append(C0).append(", ")
    a.append(C1).append(", ")
    a.append(C0FB0_C1FB0_With_Divide).append(", ")
    a.append(C0FB1_C1FB1_With_Divide).append(", ")
    a.append(C0FB2_C1FB2_With_Divide).append(", ")
    a.append(C0FB0_C1FB0).append(", ")
    a.append(C0FB1_C1FB1).append(", ")
    a.append(C0FB2_C1FB2)
    a.toString()
  }

  def getLineWithDistanceAndCongruity:String = {
    val a = new mutable.StringBuilder()
    a.append(D0C0).append(", ")
    a.append(D0C1).append(", ")
    a.append(D1C0).append(", ")
    a.append(D1C1).append(", ")
    a.append(D0C0_D0C1).append(", ")
    a.append(D1C0_D1C1)
    a.toString()
  }
}

/**
  * 将所有的被试数据输出为八张表，包含 C、FB、D 三种条件
  */
object Table {
  val AREA_Line: mutable.Buffer[Line] = mutable.Buffer[Line]()
  val INIT_Line: mutable.Buffer[Line] = mutable.Buffer[Line]()
  val MT_Line: mutable.Buffer[Line] = mutable.Buffer[Line]()
  val RT_Line: mutable.Buffer[Line] = mutable.Buffer[Line]()
  val header: String = "D0C0FB0, D0C0FB1, D0C0FB2, D0C1FB0, D0C1FB1, D0C1FB2, D1C0FB0, D1C0FB1, D1C0FB2, D1C1FB0, D1C1FB1, D1C1FB2, C0, C1, " +
    "D0C0FB0-D0C1FB0, D0C0FB1-D0C1FB1, D0C0FB2-D0C1FB2, D1C0FB0-D1C1FB0, D1C0FB1-D1C1FB1, D1C0FB2-D1C1FB2\n"
  val headerWithOutDistance: String = "C0FB0, C0FB1, C0FB2, C1FB0, C1FB1, C1FB2, C0, C1, " +
    "C0FB0-C1FB0-Divide, C0FB1-C1FB1-Divide, C0FB2-C1FB2-Divide, C0FB0-C1FB0, C0FB1-C1FB1, C0FB2-C1FB2\n"
  val headerWithDistanceAndCongruity: String = "D0C0, D0C1, D1C0, D1C1, D0C0-D0C1, D1C0-D1C1\n"
  def outputTableData(): Unit = {
    //with_distance
    DataUtils.saveTo(Paths.get("result_area_with_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(header)
      AREA_Line.foreach(i => sb.append(i.getLine).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_init_with_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(header)
      INIT_Line.foreach(i => sb.append(i.getLine).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_mt_with_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(header)
      MT_Line.foreach(i => sb.append(i.getLine).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_rt_with_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(header)
      RT_Line.foreach(i => sb.append(i.getLine).append("\n"))
      sb
    }

    //with_out_distance
    DataUtils.saveTo(Paths.get("result_area_with_out_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithOutDistance)
      AREA_Line.foreach(i => sb.append(i.getLineWithOutDistance).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_init_with_out_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithOutDistance)
      INIT_Line.foreach(i => sb.append(i.getLineWithOutDistance).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_mt_with_out_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithOutDistance)
      MT_Line.foreach(i => sb.append(i.getLineWithOutDistance).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_rt_with_out_distance.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithOutDistance)
      RT_Line.foreach(i => sb.append(i.getLineWithOutDistance).append("\n"))
      sb
    }

    //with_distance_and_congruity
    DataUtils.saveTo(Paths.get("result_area_with_distance_and_congruity.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithDistanceAndCongruity)
      AREA_Line.foreach(i => sb.append(i.getLineWithDistanceAndCongruity).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_init_with_distance_and_congruity.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithDistanceAndCongruity)
      INIT_Line.foreach(i => sb.append(i.getLineWithDistanceAndCongruity).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_mt_with_distance_and_congruity.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithDistanceAndCongruity)
      MT_Line.foreach(i => sb.append(i.getLineWithDistanceAndCongruity).append("\n"))
      sb
    }
    DataUtils.saveTo(Paths.get("result_rt_with_distance_and_congruity.csv")) {
      val sb = new mutable.StringBuilder()
      sb.append(headerWithDistanceAndCongruity)
      RT_Line.foreach(i => sb.append(i.getLineWithDistanceAndCongruity).append("\n"))
      sb
    }
  }
}

case class Subject(dataList: Array[Data]) {
  private val gData = dataList.groupBy(d => d.is_congruence)

  val conDistance0: Array[Data] = gData(true).filterNot(_.distanceIsLong)
  val conDistance1: Array[Data] = gData(true).filter(_.distanceIsLong)
  val noConDistance0: Array[Data] = gData(false).filterNot(_.distanceIsLong)
  val noConDistance1: Array[Data] = gData(false).filter(_.distanceIsLong)

  val conSize0DataD0: Array[Data] = gData(true).filter(_.size_first_big == 0).filterNot(_.distanceIsLong)
  val conSize0DataD1: Array[Data] = gData(true).filter(_.size_first_big == 0).filter(_.distanceIsLong)

  val conSize1DataD0: Array[Data] = gData(true).filter(_.size_first_big == 1).filterNot(_.distanceIsLong)
  val conSize1DataD1: Array[Data] = gData(true).filter(_.size_first_big == 1).filter(_.distanceIsLong)

  val conSize2DataD0: Array[Data] = gData(true).filter(_.size_first_big == 2).filterNot(_.distanceIsLong)
  val conSize2DataD1: Array[Data] = gData(true).filter(_.size_first_big == 2).filter(_.distanceIsLong)

  val noConSize0DataD0: Array[Data] = gData(false).filter(_.size_first_big == 0).filterNot(_.distanceIsLong)
  val noConSize0DataD1: Array[Data] = gData(false).filter(_.size_first_big == 0).filter(_.distanceIsLong)

  val noConSize1DataD0: Array[Data] = gData(false).filter(_.size_first_big == 1).filterNot(_.distanceIsLong)
  val noConSize1DataD1: Array[Data] = gData(false).filter(_.size_first_big == 1).filter(_.distanceIsLong)

  val noConSize2DataD0: Array[Data] = gData(false).filter(_.size_first_big == 2).filterNot(_.distanceIsLong)
  val noConSize2DataD1: Array[Data] = gData(false).filter(_.size_first_big == 2).filter(_.distanceIsLong)


  val conAllData: Array[Data] = gData(true)
  val noConAllData: Array[Data] = gData(false)

  val conNumber0DataD0: Array[Data] = gData(true).filter(_.number_first_big == 0).filterNot(_.distanceIsLong)
  val conNumber0DataD1: Array[Data] = gData(true).filter(_.number_first_big == 0).filter(_.distanceIsLong)

  val conNumber1DataD0: Array[Data] = gData(true).filter(_.number_first_big == 1).filterNot(_.distanceIsLong)
  val conNumber1DataD1: Array[Data] = gData(true).filter(_.number_first_big == 1).filter(_.distanceIsLong)

  val conNumber2DataD0: Array[Data] = gData(true).filter(_.number_first_big == 2).filterNot(_.distanceIsLong)
  val conNumber2DataD1: Array[Data] = gData(true).filter(_.number_first_big == 2).filter(_.distanceIsLong)

  val noConNumber0DataD0: Array[Data] = gData(false).filter(_.number_first_big == 0).filterNot(_.distanceIsLong)
  val noConNumber0DataD1: Array[Data] = gData(false).filter(_.number_first_big == 0).filter(_.distanceIsLong)

  val noConNumber1DataD0: Array[Data] = gData(false).filter(_.number_first_big == 1).filterNot(_.distanceIsLong)
  val noConNumber1DataD1: Array[Data] = gData(false).filter(_.number_first_big == 1).filter(_.distanceIsLong)

  val noConNumber2DataD0: Array[Data] = gData(false).filter(_.number_first_big == 2).filterNot(_.distanceIsLong)
  val noConNumber2DataD1: Array[Data] = gData(false).filter(_.number_first_big == 2).filter(_.distanceIsLong)

  /**
    * 算法，得到一组数据的 SD - 标准差值
    */
  val dataSD: Array[Double] => Double = data => {
    val length = data.length
    val sum = data.sum
    val avg = sum/length
    val sd = //标准差等于每个数减去平均数，之后求平方，累加，除以 N，开平方
      math.sqrt(data.map(each => {
        val abs = math.abs(each - avg)
        abs * abs
      }).sum/(length - 1))
    sd
  }

  /**
    * 算法，数据处于2.5个标准差之中
    */
  val in3D: (Double, Double, Double) => Boolean =
    (in, sd, avg) => in >= (-3 * sd + avg) && in <= (3 * sd + avg)


  /**
    * 直接打印输出当前传入数据满足三个标准差过滤后的数据平均值
    * @param datas 输入数据
    * @param msg 输入数据标记
    */
  def printFilteredDataWithIn3SD(datas: Array[Data], msg:String): (Long, Long, Long, Long) = {
    //首先，求得每个因变量的总值和平均值
    val areaSum = datas.foldLeft(0L)((sum ,newData) => sum + newData.getArea)
    val initSum = datas.foldLeft(0L)((sum, newData) => sum + newData.getInit)
    val MTSum = datas.foldLeft(0L)((sum, newData) => sum + newData.getMT)
    val RTSum = datas.foldLeft(0L)((sum, newData) => sum + newData.getRT)

    val sds = (dataSD(datas.map(_.getArea.toDouble)), dataSD(datas.map(_.getInit.toDouble)),
      dataSD(datas.map(_.getMT.toDouble)), dataSD(datas.map(_.getRT.toDouble)))
    System.out.println(s"SD(Before) is ${(sds._1.toDouble, sds._2.toDouble, sds._3.toDouble, sds._4.toDouble)}")

    val count = datas.length
    //过滤掉大于小于 3 SD + AVG 的值

    val areaData = datas.filter(line => in3D(line.getArea, sds._1, areaSum/count))
    System.out.println(s"对于 AREA，一共有: ${datas.length}, 去除了：${datas.length - areaData.length}")
    val initData = datas.filter(line => in3D(line.getInit, sds._2, initSum/count))
    System.out.println(s"对于 INIT，一共有: ${datas.length}, 去除了：${datas.length - initData.length}")
    val mtData = datas.filter(line => in3D(line.getMT, sds._3, MTSum/count))
    System.out.println(s"对于 MT，一共有: ${datas.length}, 去除了：${datas.length - mtData.length}")
    val rtData = datas.filter(line => in3D(line.getRT, sds._4, RTSum/count))
    System.out.println(s"对于 RT，一共有: ${datas.length}, 去除了：${datas.length - rtData.length}")

    val sds2 = (dataSD(areaData.map(_.getArea.toDouble)), dataSD(initData.map(_.getInit.toDouble)),
      dataSD(mtData.map(_.getMT.toDouble)), dataSD(rtData.map(_.getRT.toDouble)))
    System.out.println(s"SD(After) is ${(sds2._1.toDouble, sds2._2.toDouble, sds2._3.toDouble, sds2._4.toDouble)}")

    val res = (
      areaData.map(_.getArea).sum/areaData.length,
      initData.map(_.getInit).sum/initData.length,
      mtData.map(_.getMT).sum/mtData.length,
      rtData.map(_.getRT).sum/rtData.length)
    System.out.println(s"\nWith $msg, Result is ===================\n")
    System.out.println(s"For Area: Data Length: ${areaData.length}, Data Avg is ${res._1}")
    System.out.println(s"For Init: Data Length: ${initData.length}, Data Avg is ${res._2}")
    System.out.println(s"For MT: Data Length: ${mtData.length}, Data Avg is ${res._3}")
    System.out.println(s"For RT: Data Length: ${rtData.length}, Data Avg is ${res._4}")
    System.out.println("\n====================\n")

    res
  }
}
