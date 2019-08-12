package com.mazhangjing.wsh.screen.test;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.Config;
import com.mazhangjing.wsh.SET;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

/**
 * 检测阶段指导语屏幕
 */
public class ReadyScreen extends Screen {
    private Boolean needBigChoose; //needFirstChoose
    private Boolean isExpSizeCheck;

    protected String getTEXT(Boolean needBigChoose) {
        String TEXT = "接下来，屏幕左右将呈现两张点阵图。根据之前记忆的点阵材料，每种点阵图对应特定的尺寸，请又快又准确的判断哪种点阵图" +
                "对应的%s。屏幕左侧点阵图对应的%s按鼠标左键，屏幕右侧点阵图对应的%s按鼠标右键，准备好后，按空格键开始。";
        if (needBigChoose) return String.format(TEXT,"尺寸更小","尺寸更小","尺寸更小");
        else return String.format(TEXT,"尺寸更大","尺寸更大","尺寸更大");
    }

    public ReadyScreen(Boolean needBigChoose) {
        this.needBigChoose = needBigChoose;
    }

    public ReadyScreen(Boolean needBigChoose, Boolean isExpSizeCheck) {
        this.needBigChoose = needBigChoose;
        this.isExpSizeCheck = isExpSizeCheck;
    }

    @Override
    public Screen initScreen() {
        duration = SET.INTRO_START_MS.getValue();
        BorderPane pane = new BorderPane();
        ImageView view = new ImageView();
        String url = "";
        if (Config.experimentNumber() == 1) {
            url = needBigChoose ? "A_test_2.png" : "A_test_1.png";
        } else if (Config.experimentNumber() == 2) {
            url = needBigChoose ? "B_test_2.png" : "B_test_1.png";
        } else if (Config.experimentNumber() == 3) {
            if (isExpSizeCheck) {
                url = needBigChoose ? "C_test_size_2.png" : "C_test_size_1.png";
            } else {
                url = needBigChoose ? "C_test_ordinal_2.png" : "C_test_ordinal_1.png";
            }
        } else if (Config.experimentNumber() == 4) {
            if (isExpSizeCheck) {
                url = needBigChoose ? "D_test_size_2.png" : "D_test_size_1.png";
            } else {
                url = needBigChoose ? "D_test_ordinal_2.png" : "D_test_ordinal_1.png";
            }
        }
        view.setImage(new Image(url));
        view.setPreserveRatio(true);
        view.setFitWidth(SET.EXP_PICTURE_WIDTH.getValue());
        pane.setCenter(view);
        layout = pane;
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
        Optional.of(event)
                .filter(event1 -> event1 instanceof KeyEvent && ((KeyEvent) event1).getCode().equals(KeyCode.SPACE))
                .ifPresent(event1 ->
                        getExperiment().terminal.set(1)
                );
    }
}
