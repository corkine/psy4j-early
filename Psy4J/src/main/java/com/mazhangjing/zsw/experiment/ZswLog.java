package com.mazhangjing.zsw.experiment;

import com.mazhangjing.lab.Log;
import com.mazhangjing.zsw.SET;

/**
 * 日志记录类
 */
public class ZswLog implements Log {

    private final String VERSION = "3.1.8 - " + SET.CHOOSE.getValue() + "(尺寸大 1，尺寸小 2，数值大 3，数值小 4)";

    @Override
    public String getCurrentVersion() {
        return VERSION;
    }

    @Override
    public String getLog() {
        return "2018-12-01 1.0.0 编写程序\n" +
                "2018-12-02 1.0.1 处理多线程计时器 BUG。优化 Main 代码。添加日志系统。\n" +
                "2018-12-14 1.5.0 重设了错误页面字体颜色。更改鼠标移入事件为鼠标点击事件检测。修复了点击后如果正确立马回到界面的代码。修复了异步展示先展示的刺激之后不展示的问题。\n" +
                "2018-12-14 1.5.2 修复了异步状态相同字体的问题。\n" +
                "2018-12-24 2.0.1 修改了刺激材料，现在有 384 个试次。添加指导语部分、休息试次，添加用户鼠标反应监测，添加了练习试次，添加了日志收集系统。\n" +
                "2018-12-26 2.0.2 修改了指导语。\n" +
                "2019-01-09 2.2.2 添加了其余三种条件判断和指导语\n" +
                "2019-01-10 2.2.3 更新字体颜色，延迟。\n" +
                "2019-01-15 2.2.5 更新了修复异步后的随机序列，添加了记录反映时的刺激呈现状态。添加了字体向上的偏移。关闭了正式实验的 400ms 超时。减小了字体的差异。\n" +
                "2019-01-20 2.3.0 更新了数据收集程序，现在可以收集刺激。\n" +
                "2019-02-18 2.3.1 使用 Scala 实现的 LabUtils 类提供按键判断、屏幕切换、GUI 线程安全的设置\n" +
                "2019-03-01 2.3.2 更新了刺激呈现时点击大小不对称的问题。\n" +
                "2019-03-01 2.4.0 添加了批处理程序。\n" +
                "2019-03-04 2.4.1 正式实验后呈现刺激超时设置由 800ms 改为 400ms\n" +
                "2019-03-05 2.4.2 前半部分呈现刺激时间修改为 100，调整刺激呈现偏移。修改了指导语\n" +
                "2019-03-06 2.4.3 修正了刺激呈现时的间隔，现在动态进行计算，间隔占据屏幕宽度的 1/3\n" +
                "2019-03-09 2.4.4 修正了批处理的输出。\n" +
                "2019-03-10 2.4.5 修正了计算标准差的问题。\n" +
                "2019-03-15 3.1.5 添加了批处理的功能。\n" +
                "2019-03-17 3.1.6 修正了一个批处理的错误。\n" +
                "2019-03-20 3.1.7 修正了一个数据处理计算面积为负数的面积的问题，更新了批处理的列，更新了 Form 处理的 GUI 界面。\n" +
                "2019-03-20 3.1.8 什么都没做\n" +
                "2019-04-21 5.2.1 添加日志，打包并且存档\n";
    }

    @Override
    public String getCopyRight() {
        return "The copyright of this software belongs to the Virtual Behavior Laboratory of the School of Psychology, Central China Normal University. Any unauthorized copy of the program or copy of the code will be legally held liable.\n" +
                "" +
                "The software is based on the Java platform design and Java TM is a registered trademark of Oracle Corporation.\n" +
                "" +
                "The software is based on the Java FX framework and Java FX is based on the GNU v2 distribution protocol.\n" +
                "" +
                "The software is based on the PSY4J framework design. PSY4J is the work of Corkine Ma, which allows binary packages and source code to be used, but the source code must not be tampered with in any way and closed source.\n" +
                "" +
                "Contact: psy4j@mazhangjing.com and Support site: www.mazhangjing.com" +
                "" +
                " © Marvin Studio 2018";
    }
}
