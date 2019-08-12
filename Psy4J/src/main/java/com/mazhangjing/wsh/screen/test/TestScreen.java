package com.mazhangjing.wsh.screen.test;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import com.mazhangjing.wsh.stimulate.CircleMap;
import com.mazhangjing.wsh.stimulate.Result;
import com.mazhangjing.wsh.trial.TestTrial;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * 大小判断的检测判断屏幕
 */
public class TestScreen extends Screen {

    private CircleMap left;
    private CircleMap right;
    private Integer leftSize;
    private Integer rightSize;

    private Result result;

    private Boolean needBigChoose;

    private Logger logger = LoggerFactory.getLogger(TestTrial.class);

    public TestScreen(List<CircleMap> maps, List<CircleMap> rightAnswers, Result result, Boolean needBigChoose) {
        //从 Pair 中获取 Maps，并且转换为可供 GUI 显示的对象
        this.left = maps.get(0);
        this.right = maps.get(1);
        List<Boolean> left = this.left.getCircles().stream().map(circle -> ((boolean) circle.getUserData())).collect(Collectors.toList());
        List<Boolean> right = this.right.getCircles().stream().map(circle -> ((boolean) circle.getUserData())).collect(Collectors.toList());
        //从正确答案中获取 Maps
        rightAnswers.forEach(circleMap -> {
            //每一张 CircleMap 的大小
            Integer size = circleMap.getRadius();
            //每一张 CircleMap 的形状
            List<Boolean> answers = circleMap.getCircles().stream().map(circle -> ((boolean) circle.getUserData())).collect(Collectors.toList());
            //对于实验1，要根据尺寸判断，因此，要提取和当前展示 GUI 刺激图案一样的那对刺激的实际大小
            if (answers.equals(left)) leftSize = size;
            else if (answers.equals(right)) rightSize = size;
            //对于实验2，要根据位置判断，因此，如果左边出现的顺序在前，那么 leftSize 等同于 leftFirst
        });
        this.result = result;
        this.needBigChoose = needBigChoose;
        result.justCountAdd();
    }

    @Override
    public Screen initScreen() {
        duration = SET.SHOW_TEST_MS.getValue();
        infomation = "测试 Screen";
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        GridPane leftPane = new GridPane();
        GridPane rightPane = new GridPane();
        leftPane.setAlignment(Pos.CENTER);
        rightPane.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                leftPane.add(left.getCircles().get(5*i + j),j,i);
                rightPane.add(right.getCircles().get(5*i + j),j,i);
            }
        }
        Text text = new Text();
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font(100));
        text.setText("+");
        box.setSpacing(SET.SHOW_TEST_SPACING.getValue());
        box.getChildren().addAll(leftPane,text,rightPane);
        layout = box;
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
        /*Optional.ofNullable(event)
                .filter(event1 -> event1 instanceof MouseEvent)
                .ifPresent(event1 -> {
                    MouseButton button = ((MouseEvent) event1).getButton();
                    if (button.equals(MouseButton.PRIMARY)) makeJudge(true);
                    else if (button.equals(MouseButton.SECONDARY)) makeJudge(false);
                    }
                );*/
        Optional.of(event)
                .filter(event1 -> event1 instanceof KeyEvent)
                .ifPresent(event3 -> {
                    if (((KeyEvent) event3).getCode().equals(KeyCode.F)) {
                        makeJudge(true);
                    } else if (((KeyEvent) event3).getCode().equals(KeyCode.J)) {
                        makeJudge(false);
                    }
                });
    }

    protected void showScene(boolean answerRight) {
        //停止计时器
        logger.debug("[HEAD] Stopping timer and show scene now...");
        getExperiment().terminal.set(-999);
        //静态资源呈现
        FlowPane pane = new FlowPane();
        pane.setAlignment(Pos.CENTER);
        Label label = new Label(answerRight ? "正确" : "错误");
        label.setFont(Font.font(SET.FEEDBACK_SIZE.getValue()));
        label.setTextFill(answerRight ? Color.GREEN: Color.RED);
        pane.getChildren().addAll(label);

        getScene().setRoot(pane);
        //设置展示时间
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                logger.debug("[HEAD] Go to next normal scene now...");
                getExperiment().terminal.set(0);
                getExperiment().terminal.set(1);
            }
        }, SET.FEEDBACK_ANS_MS.getValue());
    }

    private void makeJudge(boolean isLeftClicked) {
        if (needBigChoose) {
            if (leftSize > rightSize && isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftSize,rightSize));
                result.setNextRight(true);
                showScene(true);
            } else if (leftSize < rightSize && !isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftSize,rightSize));
                result.setNextRight(true);
                showScene(true);
            } else {
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","RIGHT",leftSize,rightSize));
                result.setNextWrong(true);
                showScene(false);
            }
        } else {
            if (leftSize < rightSize && isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftSize,rightSize));
                result.setNextRight(true);
                showScene(true);
            } else if (leftSize > rightSize && !isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftSize,rightSize));
                result.setNextRight(true);
                showScene(true);
            } else {
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","RIGHT",leftSize,rightSize));
                result.setNextWrong(true);
                showScene(false);
            }
        }
    }
}
