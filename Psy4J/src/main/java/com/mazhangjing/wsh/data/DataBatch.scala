package com.mazhangjing.wsh.data

import java.nio.file._

import com.mazhangjing.wsh.data.DataUtils._
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * 本 scala 脚本用来进行 .csv 数据的整理、分组和分析。所有操作在 DataBatch 中进行，首先，.csv 文件被读入为 Data 格式的数据集合，其核心
  * 算法定义在 lineToData 方法中，接下来，在 invoke 方法中进行了数据的分类、分组、数据过滤、三个标准差剔除，然后将最后的结果保存成为 final_csv 文件。
  *
  * trait SubjectExperimentData 定义了 SPSS MANOVA 所需格式的结构，Subject 实现了这个特质，并且将上述分组后的数据（每个被试每个条件 n 条记录）
  * 计算平均值为每个被试的每种条件 1 条记录，最后输出 SPSS 可用的进行数据分析的 csv 文件，保存为 stat_info.csv
  */
trait SubjectExperimentData {
  def getGroup: Int //实验编号
  def getId: String //被试编号
  def getMin: Double
  def getMax: Double
  def getAcc: Double
  def getMean: Double
  def getStd: Double
  def getSizeSmallLeft: Double
  def getSizeSmallRight: Double
  def getSizeBigLeft: Double
  def getSizeBigRight: Double
  def getOrderEarlyLeft: Double
  def getOrderEarlyRight: Double
  def getOrderLateLeft: Double
  def getOrderLateRight: Double
  def getSizeCong: Double
  def getSizeUnCong: Double
  def getOrderCong: Double
  def getOrderUnCong: Double

  var size10Left: (Double, Double)
  var size14Left: (Double, Double)
  var size28Left: (Double, Double)
  var size40Left: (Double, Double)
  var size10Right: (Double, Double)
  var size14Right: (Double, Double)
  var size28Right: (Double, Double)
  var size40Right: (Double, Double)
  var order0Left: (Double, Double)
  var order1Left: (Double, Double)
  var order3Left: (Double, Double)
  var order4Left: (Double, Double)
  var order0Right: (Double, Double)
  var order1Right: (Double, Double)
  var order3Right: (Double, Double)
  var order4Right: (Double, Double)

  def size10DRT: (Double, Double)
  def size14DRT: (Double, Double)
  def size28DRT: (Double, Double)
  def size40DRT: (Double, Double)
  def order0DRT: (Double, Double)
  def order1DRT: (Double, Double)
  def order3DRT: (Double, Double)
  def order4DRT: (Double, Double)
}

/**
  * 本程序依赖于 log 文件生成的初步 CSV 文件中定义的数据格式：包括有/无题头，一般为 11 列 + 主试列，如果数据不可用，则小于 11 列。
  * 传入填入正确语音反应的 CSV 文件，去除空数据，之后根据四种条件分组，对于每组去除两个标准差之外的数据，过滤错误数据，
  * 然后求每组的平均值，打印输出。
  * @version 1.0.0 2019年3月9日 添加了预实验的处理逻辑
  *           1.0.1 2019年3月10日 修正了计算标准差的问题
  *           1.0.2 2019年3月11日 修正了描述上的一个问题，现在进行分析的数据使用使用的是回答正确的数据，而不是所有数据
  *           1.0.3 2019年03月25日 修正了空行判断方法，添加了 ACC
  */
object DataBatch {

  val title: String = "GROUP, ID, MIN, MAX, ACC, MEAN, STD, SIZE_SMALL_LFFT," +
    " SIZE_SMALL_RIGHT, SIZE_BIG_LFFT," +
    " SIZE_BIG_RIGHT, ORDER_EARLY_LEFT, ORDER_EARLY_RIGHT, ORDER_LATE_LEFT," +
    " ORDER_LATE_RIGHT, SIZE_CONGRUNCY, SIZE_UNCONGRUNCY, ORDER_CONGRUNCY," +
    " ORDER_UNCONGRUNCY, " +
    "SIZE10LEFT, SIZE10LEFTSD, SIZE14LEFT, SIZE14LEFTSD, SIZE28LEFT, SIZE28LEFTSD, SIZE40LEFT, SIZE40LEFTSD, " +
    "SIZE10RIGHT, SIZE10RIGHTSD, SIZE14RIGHT, SIZE14RIGHTSD, SIZE28RIGHT, SIZE28RIGHTSD, SIZE40RIGHT, SIZE40RIGHTSD, " +
    "ORDER0LEFT, ORDER0LEFTSD, ORDER1LEFT, ORDER1LEFTSD, ORDER3LEFT, ORDER3LEFTSD, ORDER4LEFT, ORDER4LEFTSD, " +
    "ORDER0RIGHT, ORDER0RIGHTSD, ORDER1RIGHT, ORDER1RIGHTSD, ORDER3RIGHT, ORDER3RIGHTSD, ORDER4RIGHT, ORDER4RIGHTSD"

  val title2: String = "GROUP, ID, MIN, MAX, ACC, MEAN, STD, SIZE10DRT_MEAN, SIZE10DRT_STD, SIZE14DRT_MEAN, SIZE14DRT_STD, SIZE28DRT_MEAN, SIZE28DRT_STD, SIZE40DRT_MEAN, SIZE40DRT_STD, " +
    "ORDER0DRT_MEAN, ORDER0DRT_STD, ORDER1DRT_MEAN, ORDER1DRT_STD, ORDER3DRT_MEAN, ORDER3DRT_STD, ORDER4DRT_MEAN, ORDER4DRT_STD"

  var subjects: ArrayBuffer[Subject] = ArrayBuffer()

  /**
    * 将每行转换成对应的数据结构 - 必须为 12 行数据，如果为 11 行，则伪造最后一行为全部正确
    *
    * @param line 如果每行不齐，则返回伪造数据，用于最后的对齐，反之，则装载真正数据
    * @return 伪造或者真实的数据，通过 real 属性辨别
    */
  def lineToData(line: String): Option[Data] = {
    //如果是题头，则不解析
    if (line.contains("ID, SHOW_TIME,")) return None
    //如果数据不齐，那么返回伪造数据
    //反之，构造数据结构
    var s = line.split(",").filterNot(_.isEmpty).map(_.trim).map(_.replace("$",""))
    if (s.length != 12) {
      //println("Get Wrong Line => " + line)
      if (s.length == 11) s = fakeLine(s)
      else return Option(fakeData)
    }
    val data = Data(
      s(0),
      s(1).toLong,
      s(2).toLong,
      s(3).toLong,
      s(4).toLong,
      s(5).toLong,
      s(6).toLong,
      if (s(7).toInt == 0) false else true,
      s(8).toLong,
      if (s(9).toInt == 0) false else true,
      s(10),
      if (s(11).toInt == 0) false else true,
      real = true,
      -2
    )
    handleEmptyLine(data)
    //最终根据极端数据做筛查
    val res = Option(finalCheck(data))
    res
  }

  var empty_line = 0

  def handleEmptyLine(in: Data): Unit = {
    if (in.check_by.equalsIgnoreCase("NONE")) {
      empty_line += 1
    }
  }

  //每个 Subject 对应一个被试的一个实验
  case class Subject(var lines: Array[Data]) extends SubjectExperimentData {

    def tryWith[T](op: => T)(_else: T): T = {
      try {
        op
      } catch {
        case _: Throwable => _else
      }
    }

    var fileName:String = ""
    var exp: Int = -2

    override def getGroup: Int = exp

    override def getId: String = tryWith(fileName.split("_").head)("NONE")

    var acc: Double = 0

    override def getAcc: Double = acc

    var min = 0.0
    var max = 0.0
    var mean = 0.0
    var std = 0.0

    override def getMin: Double = min

    override def getMax: Double = max

    override def getMean: Double = mean

    override def getStd: Double = std

    var smallLeft = 0.0
    var smallRight = 0.0
    var bigLeft = 0.0
    var bigRight = 0.0

    override def getSizeSmallLeft: Double = smallLeft

    override def getSizeSmallRight: Double = smallRight

    override def getSizeBigLeft: Double = bigLeft

    override def getSizeBigRight: Double = bigRight

    var leftLate = 0.0
    var leftEarly = 0.0
    var rightLate = 0.0
    var rightEarly = 0.0

    override def getOrderEarlyLeft: Double = leftEarly

    override def getOrderEarlyRight: Double = rightEarly

    override def getOrderLateLeft: Double = leftLate

    override def getOrderLateRight: Double = rightLate

    override def getSizeCong: Double = (getSizeSmallLeft + getSizeBigRight)/2

    override def getSizeUnCong: Double = (getSizeSmallRight + getSizeBigLeft)/2

    override def getOrderCong: Double = (getOrderEarlyLeft + getOrderLateRight)/2

    override def getOrderUnCong: Double = (getOrderEarlyRight + getOrderLateLeft)/2

    def toStringAgain: String = {
      val sb = new StringBuilder
      sb.append(getGroup).append(", ")
      sb.append(getId).append(", ")
      sb.append(getMin).append(", ")
      sb.append(getMax).append(", ")
      sb.append(getAcc).append(", ")
      sb.append(getMean).append(", ")
      sb.append(getStd).append(", ")
      sb.append(size10DRT.print).append(", ")
      sb.append(size14DRT.print).append(", ")
      sb.append(size28DRT.print).append(", ")
      sb.append(size40DRT.print).append(", ")
      sb.append(order0DRT.print).append(", ")
      sb.append(order1DRT.print).append(", ")
      sb.append(order3DRT.print).append(", ")
      sb.append(order4DRT.print)
      sb.toString()
    }

    override def toString: String = {
      val sb = new StringBuilder
      sb.append(getGroup).append(", ")
      sb.append(getId).append(", ")
      sb.append(getMin).append(", ")
      sb.append(getMax).append(", ")
      sb.append(getAcc).append(", ")
      sb.append(getMean).append(", ")
      sb.append(getStd).append(", ")
      sb.append(getSizeSmallLeft).append(", ")
      sb.append(getSizeSmallRight).append(", ")
      sb.append(getSizeBigLeft).append(", ")
      sb.append(getSizeBigRight).append(", ")
      sb.append(getOrderEarlyLeft).append(", ")
      sb.append(getOrderEarlyRight).append(", ")
      sb.append(getOrderLateLeft).append(", ")
      sb.append(getOrderLateRight).append(", ")
      sb.append(getSizeCong).append(", ")
      sb.append(getSizeUnCong).append(", ")
      sb.append(getOrderCong).append(", ")
      sb.append(getOrderUnCong).append(", ")
      sb.append(size10Left.print).append(", ")
      sb.append(size14Left.print).append(", ")
      sb.append(size28Left.print).append(", ")
      sb.append(size40Left.print).append(", ")
      sb.append(size10Right.print).append(", ")
      sb.append(size14Right.print).append(", ")
      sb.append(size28Right.print).append(", ")
      sb.append(size40Right.print).append(", ")
      sb.append(order0Left.print).append(", ")
      sb.append(order1Left.print).append(", ")
      sb.append(order3Left.print).append(", ")
      sb.append(order4Left.print).append(", ")
      sb.append(order0Right.print).append(", ")
      sb.append(order1Right.print).append(", ")
      sb.append(order3Right.print).append(", ")
      sb.append(order4Right.print)
      sb.toString()
    }

    implicit class DoublePrint(val in: (Double, Double)) {
      def print: String = {
        if (in == null) ", "
        else in._1 + ", " + in._2
      }
    }

    override var size10Left: (Double, Double) = _
    override var size14Left: (Double, Double) = _
    override var size28Left: (Double, Double) = _
    override var size40Left: (Double, Double) = _
    override var size10Right: (Double, Double) = _
    override var size14Right: (Double, Double) = _
    override var size28Right: (Double, Double) = _
    override var size40Right: (Double, Double) = _
    override var order0Left: (Double, Double) = _
    override var order1Left: (Double, Double) = _
    override var order3Left: (Double, Double) = _
    override var order4Left: (Double, Double) = _
    override var order0Right: (Double, Double) = _
    override var order1Right: (Double, Double) = _
    override var order3Right: (Double, Double) = _
    override var order4Right: (Double, Double) = _

    override def size10DRT: (Double, Double) = diff(size10Right, size10Left)
    override def size14DRT: (Double, Double) = diff(size14Right, size14Left)
    override def size28DRT: (Double, Double) = diff(size28Right, size28Left)
    override def size40DRT: (Double, Double) = diff(size40Right, size40Left)
    override def order0DRT: (Double, Double) = diff(order0Right, order0Left)
    override def order1DRT: (Double, Double) = diff(order1Right, order1Left)
    override def order3DRT: (Double, Double) = diff(order3Right, order3Left)
    override def order4DRT: (Double, Double) = diff(order4Right, order4Left)

    private def diff(a:(Double, Double), b:(Double,Double)): (Double, Double) = {
      if (a == null || b == null) (0,0)
      else (a._1 - b._1, a._2 - b._2)
    }
  }

  object Subject {

  }

  def dataToSubject(in: Stream[Data]): Subject = Subject(in.toArray)

  var duration_time_bad_line = 0

  /**
    * 去除反应时小于 250ms 的试次
    * @param in 构造好的粗略数据
    * @return 筛查后的精细数据
    */
  def finalCheck(in:Data):Data = {
    if (in.duration_time_ms < 250) {
      duration_time_bad_line += 1
      in
    }
    else in
  }

  def fakeLine(s: Array[String]): Array[String] = {
    //println(s"Fake Line to ${s.mkString(",") + ",1"}")
    s ++ Array(if (Random.nextBoolean()) "1" else "0")
  }

  def fakeData: Data = {
    Data("0",0L,0L,0L,0L,0L,0L,size_is_big = false,0L, sti_is_left = false,"", answer = false, real = false, isExp = -2)
  }

  def doSomethingIfIsExp4(subject: Subject): Unit = {
    val lines = subject.lines
    lines.foreach(
      data => {
        data.action_size match {
          case 40 => data.action_size = 10
          case 28 => data.action_size = 14
          case 14 => data.action_size = 28
          case 10 => data.action_size = 40
          case _ =>
        }
        data.size_is_big = !data.size_is_big
      }
    )
    subject.lines = lines
  }

  def invoke(path: Path, isExp: Int): Path =
    walkAndProcessAround(path, _.toFile.toString.endsWith(".csv"))(
      f => {
        implicit val sb: StringBuilder = new StringBuilder
        doWithTable(f,
          check = c => {
            println(s"Total Line is ${c.filterNot(_.isEmpty).size}")
            sb.append(s"Total Line is ${c.filterNot(_.isEmpty).size}\n")
          },
          convert = lineToData,
          collect = dataToSubject)(
          s => {
            if (isExp == 4) doSomethingIfIsExp4(s)
            val i = s.lines // i = 160
            i.foreach(_.isExp = isExp)

            val avgAndSD: Array[Double] => (Double, Double) = array => {
              val sd = getSD(array)
              val res = filterInSD(2)(array, sd)
              (res.sum/res.length, sd)
            }

            s.exp = isExp
            s.fileName = f.getFileName.toString.replace(".csv","")

            val empty_line_count = i.count(_.check_by == "NONE")
            val smaller_than_250ms = i.count(_.duration_time_ms < 250)
            val un_correct_count = i.count(!_.correct)
            //将空行、小于 250ms、非正确的数据标记为 real-false
            i.foreach(line => {
              if (line.check_by == "NONE" || line.duration_time_ms < 250 || !line.correct) line.real = false
              else line.real = true
            })
            val a = i.filter(_.real) // a = real - 去除了空行、反应时小于 250ms、错误回答的所有可用数据

            //如果是预实验，则根据 sti_is_left 和 order_is_late 判断
            //如果 CHECK_BY 为 ORDER，则根据 sti_is_left 和 stand_answer/order_is_late 判断
            //反之，如果 CHECK_BY 为 SIZE，则根据 size_is_big 和 sti_is_left 判断
            if (a.forall(_.isExp == -1)) {
              //预实验
              val left_late = a.filter(d => d.sti_is_left && d.size_is_big).map(_.duration_time_ms.toDouble)
              val left_early = a.filter(d => d.sti_is_left && !d.size_is_big).map(_.duration_time_ms.toDouble)
              val right_late = a.filter(d => !d.sti_is_left && d.size_is_big).map(_.duration_time_ms.toDouble)
              val right_early = a.filter(d => !d.sti_is_left && !d.size_is_big).map(_.duration_time_ms.toDouble)

              val ll_res = filterInSD(2)(left_late, getSD(left_late))
              val le_res = filterInSD(2)(left_early, getSD(left_early))
              val rl_res = filterInSD(2)(right_late, getSD(right_late))
              val re_res = filterInSD(2)(right_early, getSD(right_early))

              val filtered_length = a.length - ll_res.length - le_res.length - rl_res.length - re_res.length
              val acc = i.count(_.correct) * 1.0/i.length
              val leftLateAvg = ll_res.sum / ll_res.length
              val leftEarlyAvg = le_res.sum / le_res.length
              val rightLateAvg = rl_res.sum / rl_res.length
              val rightEarlyAvg = re_res.sum / re_res.length

              //解析每个被试为单行数据
              s.acc = acc
              s.leftLate = leftLateAvg
              s.leftEarly = leftEarlyAvg
              s.rightLate = rightLateAvg
              s.rightEarly = rightEarlyAvg
              val all = ll_res ++ le_res ++ rl_res ++ re_res
              s.min = all.min
              s.max = all.max
              s.mean = all.sum / all.length
              s.std = getSD(all)

              s.order0Left = avgAndSD(a.filter(d => d.action_order == 0 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order1Left = avgAndSD(a.filter(d => d.action_order == 1 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order3Left = avgAndSD(a.filter(d => d.action_order == 3 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order4Left = avgAndSD(a.filter(d => d.action_order == 4 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order0Right = avgAndSD(a.filter(d => d.action_order == 0 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order1Right = avgAndSD(a.filter(d => d.action_order == 1 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order3Right = avgAndSD(a.filter(d => d.action_order == 3 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order4Right = avgAndSD(a.filter(d => d.action_order == 4 && !d.sti_is_left).map(_.duration_time_ms.toDouble))

              //

              println(s"Result with File ${f.getFileName} ==================================")
              println(s"空行数据： " + empty_line_count + "\n")
              println(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              println(s"错误回答数据：" + un_correct_count + "\n")
              println(s"使用的数据： ${a.length}\n")
              println(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  filtered_length + "\n")
              println(s"ACC： $acc\n")
              println("检测到本实验为 ORDER CHECK_BY\n")
              println(s"For LEFT_LATE: $leftLateAvg ms, SD is ${getSD(ll_res)} ms")
              println(s"For LEFT_EARLY: $leftEarlyAvg ms, SD is ${getSD(le_res)} ms")
              println(s"For RIGHT_LATE: $rightLateAvg ms, SD is ${getSD(rl_res)} ms")
              println(s"For RIGHT_EARLY: $rightEarlyAvg ms, SD is ${getSD(re_res)} ms")
              println("==============================================================")

              sb.append(s"空行数据： " + empty_line_count + "\n")
              sb.append(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              sb.append(s"错误回答数据：" + un_correct_count + "\n")
              sb.append(s"使用的数据： ${a.length}\n")
              sb.append(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  filtered_length + "\n")
              sb.append(s"ACC： $acc\n")
              sb.append("检测到本实验为 ORDER CHECK_BY\n")
              sb.append(s"For LEFT_LATE: ${ll_res.sum / ll_res.length} ms, SD is ${getSD(ll_res)} ms\n")
              sb.append(s"For LEFT_EARLY: ${le_res.sum / le_res.length} ms, SD is ${getSD(le_res)} ms\n")
              sb.append(s"For RIGHT_LATE: ${rl_res.sum / rl_res.length} ms, SD is ${getSD(rl_res)} ms\n")
              sb.append(s"For RIGHT_EARLY: ${re_res.sum / re_res.length} ms, SD is ${getSD(re_res)} ms\n")
              sb.append("\n\n\n")

            } else if (a.forall(_.check_by == "ORDER")) {
              //实验 B
              val left_late = a.filter(d => d.sti_is_left && d.order_is_late).map(_.duration_time_ms.toDouble)
              val left_early = a.filter(d => d.sti_is_left && !d.order_is_late).map(_.duration_time_ms.toDouble)
              val right_late = a.filter(d => !d.sti_is_left && d.order_is_late).map(_.duration_time_ms.toDouble)
              val right_early = a.filter(d => !d.sti_is_left && !d.order_is_late).map(_.duration_time_ms.toDouble)

              val ll_res = filterInSD(2)(left_late, getSD(left_late))
              val le_res = filterInSD(2)(left_early, getSD(left_early))
              val rl_res = filterInSD(2)(right_late, getSD(right_late))
              val re_res = filterInSD(2)(right_early, getSD(right_early))

              val filtered_length = a.length - ll_res.length - le_res.length - rl_res.length - re_res.length
              val leftLateAvg = ll_res.sum / ll_res.length
              val leftEarlyAvg = le_res.sum / le_res.length
              val rightLateAvg = rl_res.sum / rl_res.length
              val rightEarlyAvg = re_res.sum / re_res.length
              val acc = i.count(_.correct) * 1.0 / i.length

              //解析每个被试为单行数据
              s.acc = acc
              s.leftLate = leftLateAvg
              s.leftEarly = leftEarlyAvg
              s.rightLate = rightLateAvg
              s.rightEarly = rightEarlyAvg
              val all = ll_res ++ le_res ++ rl_res ++ re_res
              s.min = all.min
              s.max = all.max
              s.mean = all.sum / all.length
              s.std = getSD(all)

              s.order0Left = avgAndSD(a.filter(d => d.action_order == 0 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order1Left = avgAndSD(a.filter(d => d.action_order == 1 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order3Left = avgAndSD(a.filter(d => d.action_order == 3 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order4Left = avgAndSD(a.filter(d => d.action_order == 4 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order0Right = avgAndSD(a.filter(d => d.action_order == 0 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order1Right = avgAndSD(a.filter(d => d.action_order == 1 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order3Right = avgAndSD(a.filter(d => d.action_order == 3 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order4Right = avgAndSD(a.filter(d => d.action_order == 4 && !d.sti_is_left).map(_.duration_time_ms.toDouble))

              println(s"Result with File ${f.getFileName} ==================================")
              println(s"空行数据： " + empty_line_count + "\n")
              println(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              println(s"错误回答数据：" + un_correct_count + "\n")
              println(s"使用的数据： ${a.length}\n")
              println(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  filtered_length + "\n")
              println(s"ACC： $acc\n")
              println("检测到本实验为 ORDER CHECK_BY")
              println(s"For LEFT_LATE: $leftLateAvg ms, SD is ${getSD(ll_res)} ms")
              println(s"For LEFT_EARLY: $leftEarlyAvg ms, SD is ${getSD(le_res)} ms")
              println(s"For RIGHT_LATE: $rightLateAvg ms, SD is ${getSD(rl_res)} ms")
              println(s"For RIGHT_EARLY: $rightEarlyAvg ms, SD is ${getSD(re_res)} ms")
              println("==============================================================")

              sb.append(s"空行数据： " + empty_line_count + "\n")
              sb.append(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              sb.append(s"错误回答数据：" + un_correct_count + "\n")
              sb.append(s"使用的数据： ${a.length}\n")
              sb.append(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  filtered_length + "\n")
              sb.append(s"ACC： $acc\n")
              sb.append(s"CHECK_BY = ORDER\n")
              sb.append(s"For LEFT_LATE: $leftLateAvg ms, SD is ${getSD(ll_res)} ms\n")
              sb.append(s"For LEFT_EARLY: $leftEarlyAvg ms, SD is ${getSD(le_res)} ms\n")
              sb.append(s"For RIGHT_LATE: $rightLateAvg ms, SD is ${getSD(rl_res)} ms\n")
              sb.append(s"For RIGHT_EARLY: $rightEarlyAvg ms, SD is ${getSD(re_res)} ms\n")
              sb.append("\n\n\n")
            } else if (a.forall(_.check_by == "SIZE")) {
              //实验 A
              val big_left = a.groupBy(d => d.sti_is_left && d.size_is_big)(true).map(_.duration_time_ms.toDouble)
              val big_right = a.groupBy(d => !d.sti_is_left && d.size_is_big)(true).map(_.duration_time_ms.toDouble)
              val small_left = a.groupBy(d => d.sti_is_left && !d.size_is_big)(true).map(_.duration_time_ms.toDouble)
              val small_right = a.groupBy(d => !d.sti_is_left && !d.size_is_big)(true).map(_.duration_time_ms.toDouble)

              val bl_res = filterInSD(2)(big_left, getSD(big_left))
              val br_res = filterInSD(2)(big_right, getSD(big_right))
              val sl_res = filterInSD(2)(small_left, getSD(small_left))
              val sr_res = filterInSD(2)(small_right, getSD(small_right))

              val filtered_length = a.length - bl_res.length - br_res.length - sl_res.length - sr_res.length
              val acc = i.count(_.correct) * 1.0/i.length
              val bigLeftAvg = bl_res.sum / bl_res.length
              val bigRightAvg = br_res.sum / br_res.length
              val smallLeftAvg = sl_res.sum / sl_res.length
              val smallRightAvg = sr_res.sum / sr_res.length

              //解析每个被试为单行数据
              s.acc = acc
              s.smallLeft = smallLeftAvg
              s.smallRight = smallRightAvg
              s.bigLeft = bigLeftAvg
              s.bigRight = bigRightAvg
              val all = bl_res ++ br_res ++ sl_res ++ sr_res
              s.min = all.min
              s.max = all.max
              s.mean = all.sum / all.length
              s.std = getSD(all)

              s.size10Left = avgAndSD(a.filter(d => d.action_size == 10 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size14Left = avgAndSD(a.filter(d => d.action_size == 14 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size28Left = avgAndSD(a.filter(d => d.action_size == 28 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size40Left = avgAndSD(a.filter(d => d.action_size == 40 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size10Right = avgAndSD(a.filter(d => d.action_size == 10 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size14Right = avgAndSD(a.filter(d => d.action_size == 14 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size28Right = avgAndSD(a.filter(d => d.action_size == 28 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size40Right = avgAndSD(a.filter(d => d.action_size == 40 && !d.sti_is_left).map(_.duration_time_ms.toDouble))

              println(s"Result with File ${f.getFileName} ==================================")
              println(s"空行数据： " + empty_line_count + "\n")
              println(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              println(s"错误回答数据：" + un_correct_count + "\n")
              println(s"使用的数据： ${a.length}\n")
              println(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  filtered_length + "\n")
              println(s"ACC： $acc\n")
              println("检测到本实验为 SIZE CHECK_BY")
              println(s"For BIG_LEFT: $bigLeftAvg ms, SD is ${getSD(bl_res)} ms")
              println(s"For BIG_RIGHT: $bigRightAvg ms, SD is ${getSD(br_res)} ms")
              println(s"For SMALL_LEFT: $smallLeftAvg ms, SD is ${getSD(sl_res)} ms")
              println(s"For SMALL_RIGHT: $smallRightAvg ms, SD is ${getSD(sr_res)} ms")
              println("==============================================================")

              sb.append(s"空行数据： " + empty_line_count + "\n")
              sb.append(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              sb.append(s"错误回答数据：" + un_correct_count + "\n")
              sb.append(s"使用的数据： ${a.length}\n")
              sb.append(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  filtered_length + "\n")
              sb.append(s"ACC： $acc\n")
              sb.append(s"CHECK_BY = SIZE\n")
              sb.append(s"For BIG_LEFT: $bigLeftAvg ms, SD is ${getSD(bl_res)} ms\n")
              sb.append(s"For BIG_RIGHT: $bigRightAvg ms, SD is ${getSD(br_res)} ms\n")
              sb.append(s"For SMALL_LEFT: $smallLeftAvg ms, SD is ${getSD(sl_res)} ms\n")
              sb.append(s"For SMALL_RIGHT: $smallRightAvg ms, SD is ${getSD(sr_res)} ms\n")
              sb.append("\n\n\n")
            } else {
              //实验 3、4 双任务
              //对于 SIZE 部分，使用实验 1A 参数，对于 ORDER 部分，使用实验 1B 参数
              val size = a.filter(_.check_by == "SIZE")
              val order = a.filter(_.check_by == "ORDER")
              assert(order.length + size.length == a.length)

              val big_left = size.groupBy(d => d.sti_is_left && d.size_is_big)(true).map(_.duration_time_ms.toDouble)
              val big_right = size.groupBy(d => !d.sti_is_left && d.size_is_big)(true).map(_.duration_time_ms.toDouble)
              val small_left = size.groupBy(d => d.sti_is_left && !d.size_is_big)(true).map(_.duration_time_ms.toDouble)
              val small_right = size.groupBy(d => !d.sti_is_left && !d.size_is_big)(true).map(_.duration_time_ms.toDouble)

              val bl_res = filterInSD(2)(big_left, getSD(big_left))
              val br_res = filterInSD(2)(big_right, getSD(big_right))
              val sl_res = filterInSD(2)(small_left, getSD(small_left))
              val sr_res = filterInSD(2)(small_right, getSD(small_right))

              val left_late = order.groupBy(d => d.sti_is_left && d.order_is_late)(true).map(_.duration_time_ms.toDouble)
              val left_early = order.groupBy(d => d.sti_is_left && !d.order_is_late)(true).map(_.duration_time_ms.toDouble)
              val right_late = order.groupBy(d => !d.sti_is_left && d.order_is_late)(true).map(_.duration_time_ms.toDouble)
              val right_early = order.groupBy(d => !d.sti_is_left && !d.order_is_late)(true).map(_.duration_time_ms.toDouble)

              val ll_res = filterInSD(2)(left_late, getSD(left_late))
              val le_res = filterInSD(2)(left_early, getSD(left_early))
              val rl_res = filterInSD(2)(right_late, getSD(right_late))
              val re_res = filterInSD(2)(right_early, getSD(right_early))

              val filtered_length_size = size.length - bl_res.length - br_res.length - sl_res.length - sr_res.length
              val filtered_length_order = order.length - ll_res.length - le_res.length - rl_res.length - re_res.length
              val bigLeftAvg = bl_res.sum / bl_res.length
              val bigRightAvg = br_res.sum / br_res.length
              val smallLeftAvg = sl_res.sum / sl_res.length
              val smallRightAvg = sr_res.sum / sr_res.length
              val leftLateAvg = ll_res.sum / ll_res.length
              val leftEarlyAvg = le_res.sum / le_res.length
              val rightLateAvg = rl_res.sum / rl_res.length
              val rightEarlyAvg = re_res.sum / re_res.length
              val acc = i.count(_.correct) * 1.0/i.length

              //解析每个被试为单行数据
              s.acc = acc
              s.smallLeft = smallLeftAvg
              s.smallRight = smallRightAvg
              s.bigLeft = bigLeftAvg
              s.bigRight = bigRightAvg
              s.leftLate = leftLateAvg
              s.leftEarly = leftEarlyAvg
              s.rightLate = rightLateAvg
              s.rightEarly = rightEarlyAvg
              val all = bl_res ++ br_res ++ sl_res ++ sr_res ++ ll_res ++ le_res ++ rl_res ++ re_res
              s.min = all.min
              s.max = all.max
              s.mean = all.sum / all.length
              s.std = getSD(all)

              s.size10Left = avgAndSD(size.filter(d => d.action_size == 10 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size14Left = avgAndSD(size.filter(d => d.action_size == 14 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size28Left = avgAndSD(size.filter(d => d.action_size == 28 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size40Left = avgAndSD(size.filter(d => d.action_size == 40 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size10Right = avgAndSD(size.filter(d => d.action_size == 10 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size14Right = avgAndSD(size.filter(d => d.action_size == 14 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size28Right = avgAndSD(size.filter(d => d.action_size == 28 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.size40Right = avgAndSD(size.filter(d => d.action_size == 40 && !d.sti_is_left).map(_.duration_time_ms.toDouble))

              s.order0Left = avgAndSD(order.filter(d => d.action_order == 0 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order1Left = avgAndSD(order.filter(d => d.action_order == 1 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order3Left = avgAndSD(order.filter(d => d.action_order == 3 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order4Left = avgAndSD(order.filter(d => d.action_order == 4 && d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order0Right = avgAndSD(order.filter(d => d.action_order == 0 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order1Right = avgAndSD(order.filter(d => d.action_order == 1 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order3Right = avgAndSD(order.filter(d => d.action_order == 3 && !d.sti_is_left).map(_.duration_time_ms.toDouble))
              s.order4Right = avgAndSD(order.filter(d => d.action_order == 4 && !d.sti_is_left).map(_.duration_time_ms.toDouble))

              println(s"Result with File ${f.getFileName} ==================================")
              println(s"空行数据： " + empty_line_count + "\n")
              println(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              println(s"错误回答数据：" + un_correct_count + "\n")

              println(s"使用的数据： ${a.length}\n")
              println(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  filtered_length_size + "(SIZE)   " + filtered_length_order + "(ORDER)" + "\n")
              println(s"ACC： $acc\n")

              println("检测到本实验为 SIZE/ORDER CHECK_BY")
              println("FOR SIZE CHECK_BY")
              println(s"For BIG_LEFT: $bigLeftAvg ms, SD is ${getSD(bl_res)} ms")
              println(s"For BIG_RIGHT: $bigRightAvg ms, SD is ${getSD(br_res)} ms")
              println(s"For SMALL_LEFT: $smallLeftAvg ms, SD is ${getSD(sl_res)} ms")
              println(s"For SMALL_RIGHT: $smallRightAvg ms, SD is ${getSD(sr_res)} ms")
              println("\nFOR ORDER CHECK_BY")
              println(s"For LEFT_LATE: $leftLateAvg ms, SD is ${getSD(ll_res)} ms")
              println(s"For LEFT_EARLY: $leftEarlyAvg ms, SD is ${getSD(le_res)} ms")
              println(s"For RIGHT_LATE: $rightLateAvg ms, SD is ${getSD(rl_res)} ms")
              println(s"For RIGHT_EARLY: $rightEarlyAvg ms, SD is ${getSD(re_res)} ms")
              println("==============================================================")

              sb.append(s"空行数据： " + empty_line_count + "\n")
              sb.append(s"反应时小于 250ms 的数据：" + smaller_than_250ms + "\n")
              sb.append(s"错误回答数据：" + un_correct_count + "\n")
              sb.append(s"使用的数据： ${a.length}\n")
              sb.append(s"使用的数据中，两个标准差过滤后剔除的数据个数为： " +  (filtered_length_size - 160) + "(SIZE), " + (filtered_length_order - 160) + "(ORDER)" + "\n")
              sb.append(s"ACC： $acc\n")
              sb.append(s"CHECK_BY = SIZE/ORDER\n")
              sb.append("FOR SIZE CHECK_BY\n")
              sb.append(s"For BIG_LEFT: $bigLeftAvg ms, SD is ${getSD(bl_res)} ms\n")
              sb.append(s"For BIG_RIGHT: $bigRightAvg ms, SD is ${getSD(br_res)} ms\n")
              sb.append(s"For SMALL_LEFT: $smallLeftAvg ms, SD is ${getSD(sl_res)} ms\n")
              sb.append(s"For SMALL_RIGHT: $smallRightAvg ms, SD is ${getSD(sr_res)} ms\n")
              sb.append("\nFOR ORDER CHECK_BY\n")
              sb.append(s"For LATE_LEFT: $leftLateAvg ms, SD is ${getSD(ll_res)} ms\n")
              sb.append(s"For EARLY_LEFT: $leftEarlyAvg ms, SD is ${getSD(le_res)} ms\n")
              sb.append(s"For LATE_RIGHT: $rightLateAvg ms, SD is ${getSD(rl_res)} ms\n")
              sb.append(s"For EARLY_RIGHT: $rightEarlyAvg ms, SD is ${getSD(re_res)} ms\n")
              sb.append("\n\n\n")
            }

            val newFile = Paths.get(f.getFileName.toString.replace(".csv", "") + "_final.csv")
            saveTo(newFile) {
              sb.append("ID, SHOW_TIME, ACTION_TIME, SHOW_TIME_MS, DURATION_TIME_MS," +
                "STAND_SIZE, ACTION_SIZE, SIZE_IS_BIG, ACTION_ORDER, STI_IS_LEFT," +
                "CHECK_BY, ANSWER, ORDER_IS_LATE, STAND_ANSWER, CORRECT").append("\n")
              i.foreach(data => {
                import DataConvert._
                sb.append(data.id).append(", ")
                sb.append(data.show_time).append(", ")
                sb.append(data.action_time).append(", ")
                sb.append(data.show_time_ms).append(", ")

                data.duration_time_ms.inSb
                data.stand_size.inSb
                data.action_size.inSb
                data.size_is_big.str.inSb
                data.action_order.inSb
                data.sti_is_left.str.inSb
                data.check_by.inSb
                data.answer.str.inSb
                data.order_is_late.str.inSb
                data.stand_answer.str.inSb
                data.correct.str.endLineInSb
              })
              sb
            }

            subjects.append(s)
        })
    })(_ => {
      val statFile = Paths.get("stat_info.csv")
      saveTo(statFile) {
        val builder = new StringBuilder
        builder.append(title).append("\n")
        subjects.foreach(subject => builder.append(subject.toString).append("\n"))
        builder
      }
      val statFile2 = Paths.get("stat_info_again.csv")
      saveTo(statFile2) {
        val builder = new StringBuilder
        builder.append(title2).append("\n")
        subjects.foreach(subject => builder.append(subject.toStringAgain).append("\n"))
        builder
      }
    })

  /**
    * Java GUI 外部接口
    * @param isPreExp 是否是预实验
    */
  def runInJava(isPreExp:Boolean = false, isExp1:Boolean = false, isExp2: Boolean = false, isExp3: Boolean, isExp4:Boolean = false): Unit = {
    var isExp = 0
    if (isExp1) isExp = 1
    if (isExp2) isExp = 2
    if (isExp3) isExp = 3
    if (isExp4) isExp = 4
    if (isPreExp) isExp = -1
    printToFile(Paths.get("result.log")) {
      invoke(Paths.get("."), isExp = isExp)
    }
  }
}

/**
  * 数据结构
  * @param id ID
  * @param show_time 显示刺激的时间
  * @param action_time 做出反应的时间
  * @param show_time_ms 显示刺激的时间 MS
  * @param duration_time_ms 反应时
  * @param stand_size 标准刺激尺寸
  * @param action_size 反应时的刺激尺寸
  * @param size_is_big 尺寸是否更大
  * @param action_order 反应时的顺序
  * @param sti_is_left 刺激是否在右边
  * @param check_by 根据 ORDER 还是 SIZE 做出反应
  * @param answer 主试补充的正确答案
  * @param real 该试次是否可用
  * @param isExp 实验编号
  */
case class Data(
                 id: String,
                 show_time: Long,
                 action_time: Long,
                 show_time_ms: Long,
                 duration_time_ms: Long,
                 stand_size: Long,
                 var action_size: Long,
                 var size_is_big: Boolean,
                 action_order: Long,
                 sti_is_left: Boolean,
                 check_by: String,
                 answer: Boolean,
                 var real:Boolean = true,
                 var isExp: Int
               ) {
  private val logger: Logger = LoggerFactory.getLogger("Data")
  /**
    * 对于 ORDER 判断条件的正确答案
    * @return
    */
  def order_is_late: Boolean = {
      if (action_order == 0 || action_order == 1) false
      else if (action_order == 3 || action_order == 4) true
      else {
        logger.warn(s"ACTION_ORDER 列错误 -> $action_order")
        false
      }
  }

  /**
    * 对于 PRE_EXP 预实验的正确答案
    * @return
    */
  def stand_answer: Boolean = {
    //如果是预实验，那么 奇数返回 false，偶数返回 true
    //if (action_order == 0 || action_order == 4) false else true
    if (action_order == 0 || action_order == 1) false else true
  }

  import DataBatch._

  /**
    * 根据不同条件的正确答案，结合主试的输入答案，计算出是否正确
    * @return 比较结果
    */
  def correct: Boolean = {
    if (isExp == -1) {
      if (answer && stand_answer) true
      else if (!answer && !stand_answer) true
      else false
    } else if (check_by == "SIZE" || isExp == 1 || isExp == 3) {
      if (size_is_big && answer) true
      else if (!size_is_big && !answer) true
      else false
    } else if (check_by == "ORDER" || isExp == 2) {
      if (order_is_late && answer) true
      else if (!order_is_late && !answer) true
      else false
    } else {
      false
    }
  }
}

object TestIt extends App {

  import DataUtils._

  def testWalkAndProcess() = {
    val path = Paths.get("/Users/corkine/工作文件夹/cmPsyLab/Lab/Lab/src/test/test_wsh_files")
    DataBatch.invoke(path, -1)
  }

  printToFile(Paths.get("result.log"), real = false) {
    testWalkAndProcess()
  }
}
