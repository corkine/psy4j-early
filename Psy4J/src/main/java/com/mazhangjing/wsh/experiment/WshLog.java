package com.mazhangjing.wsh.experiment;

import com.mazhangjing.lab.Log;
import com.mazhangjing.wsh.Config;
import com.mazhangjing.wsh.SET;

/**
 * 提供了对于 wsh 包的一个日志记录帮助类
 */
public class WshLog implements Log {

    @Override
    public String getCurrentVersion() {
        return "100.0.0";
    }

    @Override
    public String getLog() {
        return "2019-01-09 1.0.0 开始编写程序\n" +
                    "2019-01-14 修复了语音记录的多线程错误，添加了图片指导语，更改了检测阶段刺激呈现的位置，修复了随机生成最开始的刺激的算法，使用文字而不是图标表示反馈。\n" +
                    "2019-01-15 2.0.1 修复了正确率的判断逻辑。学习阶段增加试次为8。修复了被试内指导语随机的问题。修改了根据 Seed 重新学习顺序的问题。\n" +
                    "2019-01-15 2.1.1 更改了指导语。更改了 F J 代替鼠标反应。\n" +
                    "2019-01-18 2.1.3 修改了刺激大小，现在等比呈现。在学习阶段添加了注视点和空白屏幕。修改了测试阶段的试次总数。添加了允许休息的界面。\n" +
                    "2019-01-19 2.1.5 修复了随机 seed 后去除标准刺激的问题。修改了 Task 阶段的试次数 80 * 2，修改了 95% 的相等比较判断\n" +
                    "2019-01-19 2.1.6Beta 暂时开启了最后一个 Task 的提示语\n" +
                    "2019-01-19 2.1.7 添加了数据处理程序\n" +
                    "2019-01-20 2.1.8 更新了数据处理程序，现在可以监测未反应的试次了。\n" +
                    "2019-01-21 2.1.8Beta 试图修复多线程并发问题。\n" +
                    "2019-02-22 3.0.0 使用 Scala 实现部分业务逻辑，修正了伪随机问题。\n" +
                    "2019-02-22 3.0.2 测试部分刺激固定呈现时长为 2000ms，添加批处理功能\n" +
                    "2019-02-23 3.0.3 添加了实验2的全部，实验 3 和 实验 4 的学习、检测部分，剩余实验 3、4 的语音测试部分尚未完成。\n" +
                    "2019-02-24 3.0.4 修正了实验 3 和 实验 4 测试阶段的刺激数量。更改了学习 - 检测的次数，现在只学习检测一次，如果出错，重新运行程序。添加了实验 3 和 4 的语音阶段实验设计，" +
                    "重新规划了数据输出内容，现在可以输入顺序和大小信息了，此外还可以输出基于什么做的判断。\n" +
                    "2019-02-25 10.0.0c 添加了指导语，修改了一个 Bug。\n" +
                    "2019-03-06 10.0.0d 添加了数据统计分析处理。\n" +
                    "2019-03-08 10.1.0 什么都没做。修改了刺激呈现的间距，添加了预实验指导语。\n" +
                    "2019-03-09 10.1.4 修改了静态刺激的试次不兼容问题，修改了统计批处理的被试编号问题，为批处理添加了更详细的信息。\n" +
                    "2019-03-09 10.1.5 兼容了 Microsoft Office Excel 的数据格式。\n" +
                    "2019-03-09 10.1.8 修正了几个批处理的错误。\n" +
                    "2019-03-10 10.2.1 修正了计算标准差的问题。\n" +
                    "2019-03-11 10.2.2 修正了一个描述上的问题。\n" +
                    "2019-03-11 10.2.3 使用了自有的 Java Sound API 实现语音收集。\n" +
                    "2019-03-12 10.2.4 修正了一个批处理数据的 Bug。\n" +
                    "2019-03-12 11.0.0 修正了一个呈现刺激平衡的致命错误。\n" +
                    "2019-03-14 11.1.0 为预实验和正式实验修改了语音测试刺激顺序呈现的重复问题。\n" +
                    "2019-03-24 12.0.1 修正了指导语问题，使用参数进行实验类的选择。为预实验添加了一个指导语。\n" +
                    "2019-03-25 13.0.1 修正了语音反应参数\n" +
                    "2019-03-25 14.0.0 一个稳定的版本。\n" +
                    "2019-04-13 15.0.0 一个稳定的带有数据处理和分析的版本。\n" +
                    "2019-04-21 100.0.0 添加日志并且整理程序依赖。\n";
    }

    @Override
    public String getCopyRight() {
        return "The copyright of this software belongs to the Virtual Behavior Laboratory of the School of Psychology, Central China Normal University. " +
                "Any unauthorized copy of the program or copy of the code will be legally held liable.\n" +
                "The software is based on the Java platform design and Java TM is a registered trademark of Oracle Corporation.\n" +
                "The software is based on the Java FX framework and Java FX is based on the GNU v2 distribution protocol.\n" +
                "The software is based on the PSY4J framework design. PSY4J is the work of Corkine Ma, which allows binary packages and source code to be used, " +
                "but the source code must not be tampered with in any way and closed source.\n" +
                "Contact: psy4j@mazhangjing.com and Support site: www.mazhangjing.com" +
                " © Marvin Studio 2018";
    }
}
