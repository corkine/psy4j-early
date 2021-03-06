package com.mazhangjing.zsw.screen.test;

import com.mazhangjing.zsw.SET;
import com.mazhangjing.zsw.screen.StiBackScreen;
import com.mazhangjing.zsw.stimulate.Array;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 测试阶段刺激呈现后屏幕
 */
public class TestStiBackScreen extends StiBackScreen {
    public TestStiBackScreen(Array array) {
        super(array);
        duration = SET.STI_TEST_SHOW_BACK_MS.getValue();
    }

    private void showRightScene() {
        //停止计时器
        logger.debug("[BACK] Stopping timer and show right scene now...");
        getExperiment().terminal.set(-999);
        //静态资源呈现
        FlowPane pane = new FlowPane();
        pane.setAlignment(Pos.CENTER);
        Label label = new Label("✔");
        label.setFont(Font.font(SET.ERROR_SIZE.getValue()));
        label.setTextFill(Color.GREEN);
        pane.getChildren().addAll(label);

        getScene().setRoot(pane);
        //设置展示时间
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                goBackNormalScene();
            }
        }, SET.ERROR_ANS_MS.getValue());
    }

    @Override
    protected void makeJudge(boolean isLeftClicked) {
        //如果是同步的情况，则判断
        if (array.getBack().getLeft() != 999 && array.getBack().getRight() != 999) {
            if (makeSyncJudgeWrongBy(isLeftClicked)) {
                logger.debug(String.format("1 left is %s and right is %s, the answer is wrong", array.getBack().getLeft(), array.getBack().getRight()));
                logger.info(String.format("WRONG LEFT:%s,%s RIGHT:%s,%s", array.getBack().getLeft(), array.getBack().getLeftSize(), array.getBack().getRight(), array.getBack().getRightSize()));
                showWrongScene();
            } else {
                logger.info(String.format("RIGHT LEFT:%s,%s RIGHT:%s,%s",array.getBack().getLeft(),array.getBack().getLeftSize(),array.getBack().getRight(),array.getBack().getRightSize()));
                showRightScene();
            }
        }

        //如果是异步的情况，获取正确的值后再判断
        else if (array.getBack().getLeft() == 999) {
            int left = array.getHead().getLeft();
            if (makeAsyncJudgeWrongBy(true,isLeftClicked)) {
                logger.debug(String.format("2 left is %s and right is %s, the ans is wrong", left, array.getBack().getRight()));
                logger.info(String.format("WRONG LEFT:%s,%s RIGHT:%s,%s", left, array.getHead().getLeftSize(), array.getBack().getRight(), array.getBack().getRightSize()));
                showWrongScene();
            } else {
                logger.info(String.format("RIGHT LEFT:%s,%s RIGHT:%s,%s", left, array.getHead().getLeftSize(), array.getBack().getRight(), array.getBack().getRightSize()));
                showRightScene();
            }
        } else if (array.getBack().getRight() == 999) {
            int right = array.getHead().getRight();
            if (makeAsyncJudgeWrongBy(false,isLeftClicked)) {
                logger.debug(String.format("3 left is %s and right is %s, the ans is wrong", array.getBack().getLeft(), right));
                logger.info(String.format("WRONG LEFT:%s,%s RIGHT:%s,%s", array.getBack().getLeft(), array.getBack().getLeftSize(), right, array.getHead().getRightSize()));
                showWrongScene();
            } else {
                logger.info(String.format("RIGHT LEFT:%s,%s RIGHT:%s,%s", array.getBack().getLeft(), array.getBack().getLeftSize(), right, array.getHead().getRightSize()));
                showRightScene();
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
}
