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
 * 先后判断的测试屏幕
 */
public class TestScreen2 extends Screen {

    private CircleMap left;
    private CircleMap right;
    private Integer leftOrder;
    private Integer rightOrder;

    private Result result;

    private Boolean needFirstChoose;

    private Logger logger = LoggerFactory.getLogger(TestTrial.class);

    public TestScreen2(List<CircleMap> maps, List<CircleMap> rightAnswers, Result result, Boolean needFirstChoose) {
        //从 Pair 中获取 Maps，并且转换为可供 GUI 显示的对象
        this.left = maps.get(0);
        this.right = maps.get(1);
        List<Boolean> left = this.left.getCircles().stream().map(circle -> ((boolean) circle.getUserData())).collect(Collectors.toList());
        List<Boolean> right = this.right.getCircles().stream().map(circle -> ((boolean) circle.getUserData())).collect(Collectors.toList());
        //从正确答案中获取 Maps, 解决最主要的问题：确定左边和右边各自出现的顺序
        for (int i = 0 ; i < rightAnswers.size(); i++) {
            CircleMap circleMap = rightAnswers.get(i);
            List<Boolean> answers = circleMap.getCircles().stream().map(circle -> ((boolean) circle.getUserData())).collect(Collectors.toList());
            if (answers.equals(left)) leftOrder = i;
            else if (answers.equals(right)) rightOrder = i;
        }
        this.result = result;
        this.needFirstChoose = needFirstChoose;
        result.justCountAdd();
    }

    @Override
    public Screen initScreen() {
        duration = SET.SHOW_TEST_MS.getValue();
        infomation = "测试2 Screen";
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
        if (needFirstChoose) {
            if (leftOrder > rightOrder && isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftOrder,rightOrder));
                result.setNextRight(true);
                showScene(true);
            } else if (leftOrder < rightOrder && !isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftOrder,rightOrder));
                result.setNextRight(true);
                showScene(true);
            } else {
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","RIGHT",leftOrder,rightOrder));
                result.setNextWrong(true);
                showScene(false);
            }
        } else {
            if (leftOrder < rightOrder && isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftOrder,rightOrder));
                result.setNextRight(true);
                showScene(true);
            } else if (leftOrder > rightOrder && !isLeftClicked) {
                //正确
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","LEFT",leftOrder,rightOrder));
                result.setNextRight(true);
                showScene(true);
            } else {
                logger.info(String.format("RIGHT CLICK:%S LEFT:%s RIGHT:%s","RIGHT",leftOrder,rightOrder));
                result.setNextWrong(true);
                showScene(false);
            }
        }
    }
}
