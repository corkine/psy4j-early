package com.mazhangjing.wsh

import java.util

import com.mazhangjing.wsh.stimulate.{CircleMap, StiFactory}

import scala.collection.mutable
import scala.util.Random

/**
  * StiFactory 采用 Java 实现，很蹩脚，难用，因此，对于一些需要平衡的部分，采用 Scala 提供的工具集实现。
 *
  * @note 2019.03.14 更新了生成 160 个刺激的方法，现在不会连续重复。
  */
object WshUtils {
  import collection.JavaConverters._

  def shuffle5Times[T]: collection.mutable.Buffer[T] => collection.mutable.Buffer[T] =
    in => {
      var next = Random.shuffle(in)
      1.to(5).foreach(_ => {next = Random.shuffle(next)})
      next
    }

  def get160CircleMaps: util.List[CircleMap] = {
    val stand = StiFactory.initPersonStandCircleMaps
    val result = collection.mutable.Buffer[CircleMap]()
    1.to(40).foreach(_ => {
      val maps4List = StiFactory.initPersonDifferentSizeCircleMapsExcept270.asScala
      result ++= resolveOrder(result, Random.shuffle(maps4List), stand)
    })
    result.asJava
  }

  /**
    * 如果新生成的头和原来末尾元素 Index 重合，则将新生成的头接到屁股上，然后插入
    * @param res 累计序列
    * @param in 新插入的序列
    * @param stand 标准刺激
    * @return 解决问题后的累计序列
    */
  def resolveOrder(res: mutable.Buffer[CircleMap],
                   in: mutable.Buffer[CircleMap],
                   stand: util.List[CircleMap]): mutable.Buffer[CircleMap] = {
    if (res.isEmpty) in
    else {
      if (getIndex(in.head, stand) == getIndex(res.last, stand)) {
        (in - in.head) ++ mutable.Buffer(in.head)
      } else in
    }
  }

  def get160Boolean: util.List[Boolean] = {
    val t = 1.to(80).map(_ => true)
    val f = 1.to(80).map(_ => false)
    val res = t ++ f
    shuffle5Times(res.toBuffer).asJava
  }

  def main(args: Array[String]): Unit = {
    assert(get160Boolean.asScala.count(_ == true) == 80)
    val stand = StiFactory.initPersonStandCircleMaps
    val ints = get160CircleMaps.asScala.map(cm => getIndex(cm, stand))
    println(ints.mkString(", "))
    val a0 = ints.filter(_ == 0)
    val a1 = ints.filter(_ == 1)
    val a3 = ints.filter(_ == 3)
    val a4 = ints.filter(_ == 4)
    println(a0.length, a1.length, a3.length, a4.length)
  }

  def getIndex(current: CircleMap, stand: util.List[CircleMap]): Int = {
    val sb2 = new StringBuilder()
    current.getCircles.asScala.map(c => if (c.getUserData.asInstanceOf[Boolean]) "1" else "0").foreach(sb2.append)
    val currentMap = sb2.toString()
    var currentIndex = -1
    for (i <- 0 until stand.size()) {
      val circleMap = stand.get(i)
      val sb = new StringBuilder()
      circleMap.getCircles.asScala
        .map(circle => if (circle.getUserData.asInstanceOf[Boolean]) "1" else "0").foreach(sb.append)
      if (currentMap.equals(sb.toString())) currentIndex = i
    }
    currentIndex
  }
}
