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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import static com.mazhangjing.lab.LabUtils.*;

/**
 * 正式实验刺激展示后屏幕
 */
public class StiBackScreen extends Screen {

    private Boolean checkedMouseMoveEvent;

    protected Array array;

    private HBox box;

    public StiBackScreen(Array array) { this.array = array; }

    @Override
    public void callWhenShowScreen() {
        box.setSpacing(getScene().getWidth()/2);
    }

    @Override
    public Screen initScreen() {
        box = new HBox();

        //如果是异步的情况，则重新显示
        Label left = new Label((array.getBack().getLeft() != 999) ? String.valueOf(array.getBack().getLeft()) : String.valueOf(array.getHead().getLeft()));
        Label right = new Label((array.getBack().getRight() != 999) ? String.valueOf(array.getBack().getRight()) : String.valueOf(array.getHead().getRight()));
        left.setFont(Font.font(array.getBack().getLeft() != 999 ? array.getBack().getLeftSize() : array.getHead().getLeftSize()));
        right.setFont(Font.font(array.getBack().getRight() != 999 ? array.getBack().getRightSize() : array.getHead().getRightSize()));
        left.setTextFill(Color.BLACK);
        right.setTextFill(Color.BLACK);

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

        infomation = "第二次刺激呈现屏幕";
        layout = box;
        duration = SET.STI_SHOW_BACK_MS.getValue();
        return this;
    }

    protected void makeJudge(boolean isLeftClicked) {
        //如果是同步的情况，则判断
        if (array.getBack().getLeft() != 999 && array.getBack().getRight() != 999) {
            if (makeSyncJudgeWrongBy(isLeftClicked)) {
                logger.debug(String.format("1 left is %s and right is %s, the answer is wrong", array.getBack().getLeft(), array.getBack().getRight()));
                logger.info(String.format("WRONG LEFT:%s,%s RIGHT:%s,%s ||| %s", array.getBack().getLeft(), array.getBack().getLeftSize(), array.getBack().getRight(),
                        array.getBack().getRightSize(), array));
                showWrongScene();
            } else {
                logger.info(String.format("RIGHT LEFT:%s,%s RIGHT:%s,%s ||| %s",array.getBack().getLeft(),array.getBack().getLeftSize(),array.getBack().getRight(),
                        array.getBack().getRightSize(), array));
                goBackNormalScene();
            }
        }

        //如果是异步的情况，获取正确的值后再判断
        else if (array.getBack().getLeft() == 999) {
            int left = array.getHead().getLeft();
            if (makeAsyncJudgeWrongBy(true,isLeftClicked)) {
                logger.debug(String.format("2 left is %s and right is %s, the ans is wrong", left, array.getBack().getRight()));
                logger.info(String.format("WRONG LEFT:%s,%s RIGHT:%s,%s ||| %s", left, array.getHead().getLeftSize(), array.getBack().getRight(), array.getBack().getRightSize(),
                        array));
                showWrongScene();
            } else {
                logger.info(String.format("RIGHT LEFT:%s,%s RIGHT:%s,%s ||| %s", left, array.getHead().getLeftSize(), array.getBack().getRight(), array.getBack().getRightSize(),
                        array));
                goBackNormalScene();
            }
        } else if (array.getBack().getRight() == 999) {
            int right = array.getHead().getRight();
            if (makeAsyncJudgeWrongBy(false, isLeftClicked)) {
                logger.debug(String.format("3 left is %s and right is %s, the ans is wrong", array.getBack().getLeft(), right));
                logger.info(String.format("WRONG LEFT:%s,%s RIGHT:%s,%s ||| %s", array.getBack().getLeft(), array.getBack().getLeftSize(), right, array.getHead().getRightSize(),
                        array));
                showWrongScene();
            } else {
                logger.info(String.format("RIGHT LEFT:%s,%s RIGHT:%s,%s ||| %s", array.getBack().getLeft(), array.getBack().getLeftSize(), right, array.getHead().getRightSize(),
                        array));
                goBackNormalScene();
            }
        }
    }

    private Boolean makeSyncJudgeWrongBy(boolean isLeftClicked) {
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

    private Boolean makeAsyncJudgeWrongBy(boolean backLeftIs999, boolean isLeftClicked) {
        if (backLeftIs999) {
            switch (SET.CHOOSE.getValue()) {
                case 1: {//尺寸更大
                    return (array.getHead().getLeftSize() < array.getBack().getRightSize() && isLeftClicked) ||
                            (array.getHead().getLeftSize() > array.getBack().getRightSize() && !isLeftClicked);
                } case 2: {//尺寸更小
                    return (array.getHead().getLeftSize() < array.getBack().getRightSize() && !isLeftClicked) ||
                            (array.getHead().getLeftSize() > array.getBack().getRightSize() && isLeftClicked);
                } case 3: {//数值更大
                    return (array.getHead().getLeft() < array.getBack().getRight() && isLeftClicked) ||
                            (array.getHead().getLeft() > array.getBack().getRight() && !isLeftClicked);
                } case 4: {//数值更小
                    return (array.getHead().getLeft() < array.getBack().getRight() && !isLeftClicked) ||
                            (array.getHead().getLeft() > array.getBack().getRight() && isLeftClicked);
                }
            }
        } else {
            switch (SET.CHOOSE.getValue()) {
                case 1: {//尺寸更大
                    return (array.getHead().getRightSize() > array.getBack().getLeftSize() && isLeftClicked) ||
                            (array.getHead().getRightSize() < array.getBack().getLeftSize() && !isLeftClicked);
                } case 2: {//尺寸更小
                    return (array.getHead().getRightSize() > array.getBack().getLeftSize() && !isLeftClicked) ||
                            (array.getHead().getRightSize() < array.getBack().getLeftSize() && isLeftClicked);
                } case 3: {//数值更大
                    return (array.getHead().getRight() > array.getBack().getLeft() && isLeftClicked) ||
                            (array.getHead().getRight() < array.getBack().getLeft() && !isLeftClicked);
                } case 4: {//数值更小
                    return (array.getHead().getRight() > array.getBack().getLeft() && !isLeftClicked) ||
                            (array.getHead().getRight() < array.getBack().getLeft() && isLeftClicked);
                }
            }
        }
        throw new RuntimeException("Can't by that way.");
    }

    protected void goBackNormalScene() {
        logger.debug("[BACK] Go to next normal scene now...");
        /*getExperiment().terminal.set(0);
        getExperiment().terminal.set(1);*/
        goNextScreenUnSafe(getExperiment());
    }

    protected void showWrongScene() {
        //停止计时器
        logger.debug("[BACK] Stopping timer and show wrong scene now...");
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
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                goBackNormalScene();
            }
        }, SET.ERROR_ANS_MS.getValue());*/

        stopGlobalTimerGoNext(getExperiment());
        doInScreenAction(getExperiment(), this, (exp, sce) -> {
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
            LabUtils.goNextScreenUnSafe(getExperiment()); return null;
        });
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
        /*Optional.ofNullable(event)
                .filter(event1 -> event1 instanceof MouseEvent && ((MouseEvent) event1).getButton() == MouseButton.NONE)
                .ifPresent(event1 -> {
                    if (checkedMouseMoveEvent == null) {
                        //保证只触发一次
                        checkedMouseMoveEvent = true;
                        getExperiment().terminal.set(-999);
                        //如果移动鼠标，但没有回答，则自动退出本试次
                        //new Timer().schedule(new TimerTask() { @Override public void run() { goBackNormalScene(); }}, SET.MOVE_BUT_NOT_DONE.getValue());
                        logger.debug("Mouse Moved Detected.");
                    }
                });*/
        /*ifMouseButton(MouseButton.NONE, event, experiment, scene, (exp, sce) -> {
            if (checkedMouseMoveEvent == null) {
                //保证只触发一次
                checkedMouseMoveEvent = true;
                LabUtils.stopGlobalTimerGoNext(experiment);
                //如果移动鼠标，但没有回答，则自动退出本试次
                //new Timer().schedule(new TimerTask() { @Override public void run() { goBackNormalScene(); }}, SET.MOVE_BUT_NOT_DONE.getValue());
                logger.debug("Mouse Moved Detected.");
            }
            return null;
        });*/
        //这个东西采用 implicit Class 更优雅：
        // event.ifPressedButton(MouseButton.NONE) {
        //      xxx
        // }
        ifMouseButton(MouseButton.NONE, event, () -> {
            if (checkedMouseMoveEvent == null) {
                //保证只触发一次
                checkedMouseMoveEvent = true;
                LabUtils.stopGlobalTimerGoNext(experiment);
                //如果移动鼠标，但没有回答，则自动退出本试次
                //new Timer().schedule(new TimerTask() { @Override public void run() { goBackNormalScene(); }}, SET.MOVE_BUT_NOT_DONE.getValue());
                logger.debug("Mouse Moved Detected.");
            }
            return null;
        });
    }
}
