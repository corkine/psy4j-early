package com.mazhangjing.wsh.screen.real;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.LabUtils;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.Config;
import com.mazhangjing.wsh.SET;
import com.mazhangjing.wsh.data.ExperimentData;
import com.mazhangjing.wsh.data.TrialData;
import com.mazhangjing.wsh.stimulate.CircleMap;
import com.mazhangjing.wsh.stimulate.StiFactory;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.Instant;
import java.util.List;

/**
 * 测试阶段刺激展示屏幕
 */
public class ShowScreen extends Screen {

    private final ExperimentData experimentData;

    private final TrialData trialData;

    private Boolean isNormal;
    public Boolean isLeft;
    private CircleMap map;
    public Integer mapIndex;
    private Boolean isSizeCheck;

    private  HBox box = new HBox();
    {
        box.setAlignment(Pos.CENTER); //减轻执行负担
    }

    public ShowScreen(Boolean isNormal, CircleMap map, Boolean isLeft, Integer index,
                      Boolean isSizeCheck, ExperimentData experimentData, TrialData trialData) {
        this.experimentData = experimentData;
        this.isNormal = isNormal;
        this.map = map;
        this.isLeft = isLeft;
        this.infomation = "ShowScreen - Index - " + index;
        this.isSizeCheck = isSizeCheck;

        StringBuilder builder = new StringBuilder();
        map.getCircles().stream().map(circle -> ((Boolean) circle.getUserData()) ? "1" : "0").forEach(builder::append);
        String thisMapShape = builder.toString();

        //解析当前 Map 出现的 Order 信息
        List<CircleMap> stand = StiFactory.initPersonStandCircleMaps();
        for (int i = 0; i < stand.size(); i++) {
            CircleMap circleMap = stand.get(i);
            StringBuilder builder2 = new StringBuilder();
            circleMap.getCircles().stream().map(circle -> ((Boolean) circle.getUserData()) ? "1" : "0").forEach(builder2::append);
            String mapShape = builder2.toString();
            if (thisMapShape.equals(mapShape)) this.mapIndex = i;
        }
        this.trialData = trialData;
    }

    @Override
    public void callWhenShowScreen() {
        logger.info("Showing sti now..." + "current show is SizeCheck ?" + (isSizeCheck ? "SIZE" : "ORDER")
                + " current show is normal? " + isNormal + "::: current show is left? " +
                (isLeft ? "LEFT" : "RIGHT") + ":::" + mapIndex + ":::" + map.getRadius().toString());
        trialData.isNormal_$eq(isNormal);
        trialData.checkBySizeOrOrder_$eq(isSizeCheck ? "SIZE" : "ORDER");
        trialData.showByLeftOrRight_$eq(isLeft ? "LEFT" : "RIGHT");
        trialData.orderIndex_$eq(mapIndex);
        trialData.stimulateRadius_$eq(map.getRadius());
        trialData.showStiTimeInstant_$eq(Instant.now());
        trialData.showStiNanoTime_$eq(System.nanoTime());
    }

    @Override
    public Screen initScreen() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        GridPane leftPane = new GridPane();
        GridPane rightPane = new GridPane();
        leftPane.setAlignment(Pos.CENTER);
        rightPane.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Circle circle = map.getCircles().get(5*i + j); //标准化
                circle.setRadius(SET.PIXEL_270_SIZE.getValue());
                Circle fakeCircle = new Circle();
                fakeCircle.setFill(Color.WHITE);
                fakeCircle.setRadius(SET.PIXEL_270_SIZE.getValue());
                if (isLeft) {
                    leftPane.add(circle,j,i);
                    rightPane.add(fakeCircle,j,i);
                } else {
                    rightPane.add(circle,j,i);
                    leftPane.add(fakeCircle,j,i);
                }
            }
        }
        Text text = new Text();
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font(100));
        text.setText(" ");
        box.setSpacing(StiFactory.setSpacing(map.getRadius()));
        if (!Config.isPreExperiment()) {
            box.getChildren().addAll(leftPane,text,rightPane);
        } else {
            Text left = new Text(); left.setFont(Font.font(SET.PRE_EXP_SIZE.getValue()));
            Text right = new Text(); right.setFont(Font.font(SET.PRE_EXP_SIZE.getValue()));
            if (isLeft) {
                if (mapIndex == 4 || mapIndex == 3) left.setText((mapIndex + 1 + 4) + "");
                else left.setText((mapIndex + 1) + "");
            }
            else {
                if (mapIndex == 4 || mapIndex == 3) right.setText((mapIndex + 1 + 4) + "");
                else right.setText((mapIndex + 1) + "");
            }
            box.getChildren().addAll(left, text, right);
            box.setSpacing(SET.PRE_EXP_SPACING.getValue());
        }
        layout = box;
        duration = SET.EXP_STI_ALL_MS.getValue();
        return this;
    }

    private transient boolean receivedMoveEvent = false;

    @Override
    public void callWhenLeavingScreen() {
        if (trialData.actionTimeInstant() == null) {
            trialData.actionTimeNanoTime_$eq(0);
            experimentData.trialsData().add(trialData);
        }
    }

    @Override
    synchronized public void eventHandler(Event event, Experiment experiment, Scene scene) {
        //当接受到声音事件，则进行处理。因为可能密集的收到多条事件，因此如果在当前试次收集到，那么不再接受别的信号。
        //同时，当收集到信号后，可能正好前往了下一屏幕，因此调用 terminal 时需要保证线程安全
        if (event.getEventType().toString().equals("EVENT") && !receivedMoveEvent) {
            receivedMoveEvent = true;
            Integer radius = map.getRadius();
            String result = radius.toString();
            logger.info("\n\n\n\n\n\n========================================\n");
            logger.info("Get result and go now... - " + ":::" + mapIndex + ":::" + result + ":::" + System.nanoTime());
            logger.info("\n========================================\n\n\n\n\n\n");
            trialData.actionTimeInstant_$eq(Instant.now());
            trialData.actionTimeNanoTime_$eq(System.nanoTime());
            experimentData.trialsData().add(trialData);
            //getExperiment().terminal.set(1);
            LabUtils.goNextScreenSafe(getExperiment(), this);
        }
    }
}
