package com.mazhangjing.wsh.trial;

import com.mazhangjing.lab.Trial;
import com.mazhangjing.wsh.Config;
import com.mazhangjing.wsh.screen.learn.*;
import com.mazhangjing.wsh.stimulate.CircleMap;
import com.mazhangjing.wsh.stimulate.StiFactory;

import java.util.List;

/**
 * 定义学习试次
 */
public class LearnTrial extends Trial {

    private Boolean isFirstLearn;

    private Integer expNumber = Config.experimentNumber();

    public LearnTrial(Boolean isFirstLearn) {
        this.isFirstLearn = isFirstLearn;
    }

    public LearnTrial() {
        this.isFirstLearn = false;
    }

    @Override
    public Trial initTrial() {
        //screens.add(new InfoScreen().initScreen());
        if (isFirstLearn) screens.add(new TotalStartScreen().initScreen());
        screens.add(new ReadyScreen().initScreen());
        //注入数据，共 8 次循环，每次 5 张
        List<List<CircleMap>> lists = null;
        logger.info("Current ExpNumber is " + expNumber);
        if (expNumber == 1) {
            lists = StiFactory.initPersonDifferentSizeCircleMapsFor8Repeat();
            logger.info("Learn Sti is \n" + lists);
        } else if (expNumber == 2) {
            lists = StiFactory.initPersonDifferentSizeCircleMapsFor8RepeatForExp2();
            logger.info("Learn Sti is \n" + lists);
        } else if (expNumber == 3) {
            lists = StiFactory.initPersonDifferentSizeCircleMapsFor8RepeatForExp34(false);
        } else if (expNumber == 4) {
            lists = StiFactory.initPersonDifferentSizeCircleMapsFor8RepeatForExp34(true);
        } else {
            throw new IllegalArgumentException("传入的参数不正确，试验编号应为 1 2 3 4");
        }
        assert lists != null;
        lists.forEach(list -> {
            list.forEach(map -> {
                //对于 8 次重复的每一次，都创建一组 Screen
                screens.add(new BlankScreen().initScreen());
                screens.add(new FixScreen().initScreen());
                screens.add(new ShowScreen(map).initScreen());
            });
            //添加提示试次
            screens.add(new TipScreen().initScreen());
        });
        //删除最后一次提示
        screens.remove(screens.size()-1);
        return this;
    }
}
