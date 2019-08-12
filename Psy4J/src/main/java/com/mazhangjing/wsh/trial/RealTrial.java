package com.mazhangjing.wsh.trial;

import com.mazhangjing.lab.LabUtils;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.lab.ScreenBuilder;
import com.mazhangjing.lab.Trial;
import com.mazhangjing.wsh.Config;
import com.mazhangjing.wsh.SET;
import com.mazhangjing.wsh.WshUtils;
import com.mazhangjing.wsh.data.ExperimentData;
import com.mazhangjing.wsh.data.TrialData;
import com.mazhangjing.wsh.screen.learn.InfoScreen;
import com.mazhangjing.wsh.screen.real.*;
import com.mazhangjing.wsh.stimulate.CircleMap;
import com.mazhangjing.wsh.stimulate.StiFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 定义检测试次
 */
public class RealTrial extends Trial {

    private final ExperimentData experimentData;

    public RealTrial(ExperimentData experimentData) {
        this.experimentData = experimentData;
    }

    @Override
    public Trial initTrial() {
        try {
            StiFactory.loadSeedForStaticUsage("seed.log");
        } catch (Exception ignore) {
        }

        //用户信息收集阶段
        screens.add(new InfoScreen(experimentData).initScreen());

        //根据实验不同，指导语不同，且判断次数不同
        //如果是实验 1 和 实验 2，那么长度为 160，对于实验 1，指导语为根据 Size 判断，对于实验 2，指导语为根据 Order 判断
        if (Config.experimentNumber() == 1 || Config.experimentNumber() == 2) {
           setScreens(Config.experimentNumber() == 1, screens);
        //如果是实验 3 和 4，那么实验的长度为 160 * 2，根据 Size 和 根据 Order 判断要进行平衡
        } else if (Config.experimentNumber() == 3 || Config.experimentNumber() == 4) {
            boolean choose = new Random().nextBoolean();
            //如果是先 Size 后 Order
            if (choose) {
                setScreens(true, screens);
                setScreens(false, screens);
            } else {
                setScreens(false, screens);
                setScreens(true, screens);
            }
        }

        //结束阶段
        screens.add(new EndScreen().initScreen());
        return this;
    }

    private Integer getRandomTime() {
        return new Random().nextInt(250) + 250;
    }

    /**
     * 根据条件向 Screens 中注入 Screen
     * @param isSizeCheck 指导语是否需要判断 Size
     * @param screens 传入的 Screens 队列
     */
    private void setScreens(Boolean isSizeCheck, List<Screen> screens) {

        //checkWshUtils();

        Random random = new Random();
        //测试阶段
        //测试准备 Screen
        if (Config.isPreExperiment()) {
            Screen sc = ScreenBuilder.named("实验介绍屏幕")
                    .setScene(() -> {
                        BorderPane pane = new BorderPane();
                        ImageView view = new ImageView();
                        view.setImage(new Image("pre_1.png"));
                        view.setPreserveRatio(true);
                        view.setFitWidth(SET.EXP_PICTURE_WIDTH.getValue());
                        pane.setCenter(view);
                        return pane;
                    })
                    .ifEventThen((event, exp, scene, screen) -> {
                        LabUtils.ifKeyButton(KeyCode.SPACE, event, () -> {
                            LabUtils.goNextScreenSafe(exp, screen);
                            return null;
                        });
                        return null;
                    })
                    .showIn(100000).build();
            screens.add(sc);
        }
        screens.add(new TestReadyScreen(isSizeCheck).initScreen());
        //测试开始 Screen 组，8 次重复，每次包含空屏、注视点、除了标准刺激之外的随机一个刺激

        List<CircleMap> cMaps = WshUtils.get160CircleMaps();
        List<Object> cBools = WshUtils.get160Boolean();

        for (int i = 0; i < 8; i++) {
            screens.add(new BlankScreen().initScreen());
            screens.add(new FixScreen().initScreen());
            //动态空屏幕
            screens.add(new BlankScreen(getRandomTime()).initScreen());
            List<CircleMap> fourMaps = StiFactory.initPersonDifferentSizeCircleMapsExcept270(); //Scene 不能接受一个对象出现在不同位置，因此需要每次构建实例
            Collections.shuffle(fourMaps);
            screens.add(new ShowScreen(false, fourMaps.get(0),
                    random.nextBoolean(), i, isSizeCheck, experimentData, new TrialData()).initScreen());
        }

        //实验阶段
        //实验准备 Screen
        screens.add(new ReadyScreen(isSizeCheck).initScreen());
        //实验开始 Screen 组，160 次重复，每次包含空屏、注视点、除了标准刺激之外的平衡过的一个刺激，对于标准刺激，每隔 16 次呈现，对于休息屏幕，每隔 80 次呈现。
        for (int i = 0; i < 80*2; i++) {
            //呈现标准点阵 每隔16个trail（开始也要呈现，6次） 80%16 == 0
            if (i % 16 == 0 && SET.EXP_WITH_STAND.getValue() == 1)
                screens.add(new StandShowScreen(StiFactory.initPersonDifferentSizeCircleMapOnly270()).initScreen());
            //空白的屏幕
            screens.add(new BlankScreen().initScreen());
            //固定点屏幕
            screens.add(new FixScreen().initScreen());
            //动态空屏幕
            screens.add(new BlankScreen(getRandomTime()).initScreen());
            //20 28 56 80 四张不同的点阵图
            //List<CircleMap> fourMaps = StiFactory.initPersonDifferentSizeCircleMapsExcept270(); //Scene 不能接受一个对象出现在不同位置，因此需要每次构建实例
            screens.add(new ShowScreen( true, cMaps.get(i), ((Boolean) cBools.get(i)),
                    i, isSizeCheck, experimentData, new TrialData()).initScreen());
            //呈现休息屏幕
            if (i % 80 == 0 && i != 0)
                screens.add(new RelaxScreen().initScreen());
        }

        checkScreens();
    }

    private void checkWshUtils() {
        System.out.println("All Boolean Length is " + WshUtils.get160Boolean().size());
        int length = WshUtils.get160Boolean().stream().filter(i -> ((Boolean) i)).toArray().length;
        System.out.println("Left Length is " + length);
        List<Boolean> bls = new ArrayList<>();
        for (int i = 0; i < 160; i++) {
            bls.add(((Boolean) WshUtils.get160Boolean().get(i)));
        }
        System.out.println("Left Length is " + bls.stream().filter(b -> b).toArray().length);
    }

    private void checkScreens() {
        List<Screen> screens = this.screens;
        List<Screen> showScreensOrigin = screens.stream().filter(screen -> screen instanceof ShowScreen).collect(Collectors.toList());
        List<Screen> showScreens = showScreensOrigin.subList(8, showScreensOrigin.size());
        System.out.println("Real Trial have " + showScreens.size());
        int length = showScreens.stream().map(i -> ((ShowScreen) i).isLeft).filter(i -> i).toArray().length;
        System.out.println("IsLeft Length is " + length);
        int length2 = showScreens.stream().map(i -> ((ShowScreen) i).isLeft).filter(i -> !i).toArray().length;
        System.out.println("Not IsLeft Length is " + length2);
        int a0 = showScreens.stream().map(i -> ((ShowScreen) i).mapIndex).filter(i -> i == 0).toArray().length;
        int a1 = showScreens.stream().map(i -> ((ShowScreen) i).mapIndex).filter(i -> i == 1).toArray().length;
        int a3 = showScreens.stream().map(i -> ((ShowScreen) i).mapIndex).filter(i -> i == 3).toArray().length;
        int a4 = showScreens.stream().map(i -> ((ShowScreen) i).mapIndex).filter(i -> i == 4).toArray().length;
        System.out.println("0 have" + a0 + ", 1 have " + a1 + ", 3 have " + a3 + ", 4 have " + a4);
    }
}
