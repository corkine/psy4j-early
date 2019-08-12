package com.mazhangjing.zsw.screen;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.LabUtils;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.zsw.SET;
import com.mazhangjing.zsw.stimulate.Array;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import static com.mazhangjing.lab.LabUtils.*;

/**
 * 正式实验的刺激前屏幕
 */
public class StiHeadScreen extends Screen {

    protected Array array;

    private HBox box;

    public StiHeadScreen(Array array) {
        this.array = array;
    }

    @Override
    public void callWhenShowScreen() {
       box.setSpacing(getScene().getWidth()/2);
    }

    @Override
    public Screen initScreen() {
        //BorderPane box = new BorderPane();
        box = new HBox();

        Label left = new Label((array.getHead().getLeft() != 999) ? String.valueOf(array.getHead().getLeft()) : "  ");
        left.setTextFill(Color.BLACK);
        left.setFont(Font.font(array.getHead().getLeft() != 999 ? array.getHead().getLeftSize() : SET.BIGGER_SIZE.getValue()));

        Label right = new Label((array.getHead().getRight() != 999) ? String.valueOf(array.getHead().getRight()) : "  ");
        right.setFont(Font.font(array.getHead().getRight() != 999 ? array.getHead().getRightSize() : SET.BIGGER_SIZE.getValue()));
        right.setTextFill(Color.BLACK);

        //box.setLeft(left);
        StackPane spLeft = new StackPane();
        Rectangle angLeft = new Rectangle(SET.BIGGER_SIZE.getValue(), SET.BIGGER_SIZE.getValue());
        angLeft.setFill(Color.rgb(30,30,30,0.0));
        spLeft.getChildren().addAll(left, angLeft);
        spLeft.setAlignment(Pos.CENTER_RIGHT);

        //box.setRight(right);
        StackPane spRight = new StackPane();
        Rectangle angRight = new Rectangle(SET.BIGGER_SIZE.getValue(), SET.BIGGER_SIZE.getValue());
        angRight.setFill(Color.rgb(30,30,30,0.0));
        spRight.getChildren().addAll(right,angRight);
        spRight.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(spLeft, spRight);
        box.setAlignment(Pos.CENTER);

        spRight.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            logger.debug("Get Right clicked" + "::" + System.nanoTime());
            makeJudge(false);
        });

        spLeft.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            logger.debug("Get Left clicked" + "::" + System.nanoTime());
            makeJudge(true);
        });

        infomation = "首次刺激呈现屏幕";
        layout = box;
        duration = SET.STI_SHOW_HEAD_MS.getValue();
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
    }

    protected void makeJudge(boolean isLeftClicked) {
        //这里不能判断异步，如果异步并且反应，则无反应
        if (array.getHead().getLeft() != 999 || array.getHead().getRight() != 999) {
            if (makeJudgeWrongBy(isLeftClicked)) {
                logger.info(String.format("WRONG LEFT:%s,%s RIGHT:%s,%s ||| %s", array.getHead().getLeft(), array.getHead().getLeftSize(), array.getHead().getRight(),
                        array.getHead().getRightSize(), array));
                showWrongScene();
            } else {
                logger.info(String.format("RIGHT LEFT:%s,%s RIGHT:%s,%s ||| %s", array.getHead().getLeft(), array.getHead().getLeftSize(),
                        array.getHead().getRight(), array.getHead().getRightSize(), array));
            }
        } else {
            logger.info(String.format("ERROR LEFT:%s,%s RIGHT:%s,%s LEFT2:%s,%s RIGHT2:%s,%s ||| %s",
                    array.getHead().getLeft(), array.getHead().getLeftSize(),
                    array.getHead().getRight(), array.getHead().getRightSize(),
                    array.getBack().getLeft(), array.getBack().getLeftSize(),
                    array.getBack().getRight(), array.getBack().getRightSize(), array));
        }
    }

    private Boolean makeJudgeWrongBy(boolean isLeftClicked) {
        switch (SET.CHOOSE.getValue()) {
            case 1: {//尺寸更大
                return (array.getHead().getLeftSize() > array.getHead().getRightSize() && !isLeftClicked) ||
                        (array.getHead().getLeftSize() < array.getHead().getRightSize() && isLeftClicked);
            } case 2: {//尺寸更小
                return (array.getHead().getLeftSize() > array.getHead().getRightSize() && isLeftClicked) ||
                        (array.getHead().getLeftSize() < array.getHead().getRightSize() && !isLeftClicked);
            } case 3: {//数值更大
                return (array.getHead().getLeft() > array.getHead().getRight() && !isLeftClicked) ||
                        (array.getHead().getLeft() < array.getHead().getRight() && isLeftClicked);
            } case 4: {//数值更小
                return (array.getHead().getLeft() > array.getHead().getRight() && isLeftClicked) ||
                        (array.getHead().getLeft() < array.getHead().getRight() && !isLeftClicked);
            }
        }
        throw new RuntimeException("Can't by that way.");
    }


    protected void showWrongScene() {
        //停止计时器
        logger.debug("[HEAD] Stopping timer and show wrong scene now...");

        stopGlobalTimerGoNext(getExperiment());
        doInScreenAction(getExperiment(), this, (exp, scr) -> {
            FlowPane pane = new FlowPane();
            pane.setAlignment(Pos.CENTER);
            Label label = new Label("×");
            label.setFont(Font.font(SET.ERROR_SIZE.getValue()));
            label.setTextFill(Color.RED);
            pane.getChildren().addAll(label);

            getScene().setRoot(pane);
            return null;
        });
        doAfter(SET.ERROR_ANS_MS.getValue(), getExperiment(), this, () -> {
            logger.debug("[HEAD] Go to next normal scene now...");
            LabUtils.goNextScreenUnSafe(getExperiment());
            return null;
        });
        /*getExperiment().terminal.set(-999);
        //静态资源呈现
        FlowPane pane = new FlowPane();
        pane.setAlignment(Pos.CENTER);
        Label label = new Label("×");
        label.setFont(Font.font(SET.ERROR_SIZE.getValue()));
        label.setTextFill(Color.RED);
        pane.getChildren().addAll(label);

        getScene().setRoot(pane);
        //设置展示时间
        //在 Psy4J 中要慎重的调用 Timer，必须首先确保 Queue 计时器停止，然后再设置计时器
        //否则就应该在继续前先检查状态
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                logger.debug("[HEAD] Go to next normal scene now...");
                getExperiment().terminal.set(0);
                getExperiment().terminal.set(1);
            }
        }, SET.ERROR_ANS_MS.getValue());*/
    }
}
