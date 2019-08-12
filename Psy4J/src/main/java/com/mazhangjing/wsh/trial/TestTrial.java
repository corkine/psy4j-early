package com.mazhangjing.wsh.trial;

import com.mazhangjing.lab.Trial;
import com.mazhangjing.wsh.Config;
import com.mazhangjing.wsh.screen.test.ReadyScreen;
import com.mazhangjing.wsh.screen.test.ScoreScreen;
import com.mazhangjing.wsh.screen.test.TestScreen;
import com.mazhangjing.wsh.screen.test.TestScreen2;
import com.mazhangjing.wsh.stimulate.CircleMap;
import com.mazhangjing.wsh.stimulate.Result;
import com.mazhangjing.wsh.stimulate.StiFactory;

import java.util.List;
import java.util.Random;

/**
 * 定义学习检测试次
 * 注意，这里实验 3、4 的区分在 StiFactory 中进行
 */
public class TestTrial extends Trial {
    @Override
    public Trial initTrial() {
        //需要判断大还是小，早还是晚
        Boolean needBigChoose = StiFactory.getNeedBigChoose();
        //注入 20 对需要比较的刺激
        List<List<CircleMap>> lists = StiFactory.initPersonStandCircleMapsFor20Pair();
        //注入测试的正确答案
        List<CircleMap> testAns = StiFactory.initPersonDifferentSizeCircleMapsForTest(Config.experimentNumber());
        //注入界面
        if (Config.experimentNumber() == 1) {

            Result result1 = new Result("SIZE");
            screens.add(new ReadyScreen(needBigChoose).initScreen());
            lists.forEach(list -> {
                screens.add(new TestScreen(list, testAns, result1, needBigChoose).initScreen());
            });
            //注入结果显示界面
            screens.add(new ScoreScreen(result1).initScreen());

        } else if (Config.experimentNumber() == 2) {

            Result result2 = new Result("ORDER");
            screens.add(new ReadyScreen(needBigChoose).initScreen());
            lists.forEach(list -> {
                screens.add(new TestScreen2(list, testAns, result2, needBigChoose).initScreen());
            });
            //注入结果显示界面
            screens.add(new ScoreScreen(result2).initScreen());

        } else if (Config.experimentNumber() == 3 || Config.experimentNumber() == 4) {

            //平衡先判断大小和先判断先后
            boolean chooseOne = new Random().nextBoolean();
            if (chooseOne) {

                //先判断大小
                Result result31 = new Result("SIZE");
                screens.add(new ReadyScreen(needBigChoose, true).initScreen());
                StiFactory.initPersonStandCircleMapsFor20Pair()
                        .forEach(list -> screens.add(new TestScreen(list, testAns, result31, needBigChoose).initScreen()));
                //注入结果显示界面
                screens.add(new ScoreScreen(result31).initScreen());

                //然后判断先后
                Result result32 = new Result("ORDER");
                screens.add(new ReadyScreen(needBigChoose, false).initScreen());
                StiFactory.initPersonStandCircleMapsFor20Pair()
                        .forEach(list -> screens.add(new TestScreen2(list, testAns, result32, needBigChoose).initScreen()));
                //注入结果显示界面
                screens.add(new ScoreScreen(result32).initScreen());

            } else {

                //先判断先后
                Result result41 = new Result("ORDER");
                screens.add(new ReadyScreen(needBigChoose, false).initScreen());
                StiFactory.initPersonStandCircleMapsFor20Pair()
                        .forEach(list -> screens.add(new TestScreen2(list, testAns, result41, needBigChoose).initScreen()));
                //注入结果显示界面
                screens.add(new ScoreScreen(result41).initScreen());

                //再判断大小
                Result result42 = new Result("SIZE");
                screens.add(new ReadyScreen(needBigChoose, true).initScreen());
                StiFactory.initPersonStandCircleMapsFor20Pair()
                        .forEach(list -> screens.add(new TestScreen(list, testAns, result42, needBigChoose).initScreen()));
                //注入结果显示界面
                screens.add(new ScoreScreen(result42).initScreen());
            }
        }
        return this;
    }
}
