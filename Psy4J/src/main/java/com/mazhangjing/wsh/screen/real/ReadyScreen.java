package com.mazhangjing.wsh.screen.real;

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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

/**
 * 检测阶段指导语呈现屏幕
 */
public class ReadyScreen extends Screen {

    private final Boolean isSizeCheck;

    public ReadyScreen(Boolean isSizeCheck) {
        this.isSizeCheck = isSizeCheck;
    }

    @Override
    public Screen initScreen() {
        duration = SET.EXP_INTRO_MS.getValue();
        BorderPane pane = new BorderPane();
        ImageView view = new ImageView();
        String url = "";
        if (Config.experimentNumber() == 1) {
            url = "A_task_2.png";
        } else if (Config.experimentNumber() == 2) {
            url = "B_task_2.png";
        } else if (Config.experimentNumber() == 3) {
            if (isSizeCheck) url = "C_task_size_2.png";
            else url = "C_task_ordinal_2.png";
        } else if (Config.experimentNumber() == 4) {
            if (isSizeCheck) url = "D_task_size_2.png";
            else url = "D_task_ordinal_2.png";
        }
        if (Config.isPreExperiment()) url = "pre_3.png";
        view.setImage(new Image(url));
        view.setPreserveRatio(true);
        view.setFitWidth(SET.EXP_PICTURE_WIDTH.getValue());
        pane.setCenter(view);
        layout = pane;
        infomation = "指导语页面";
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
        Optional.ofNullable(event)
                .filter(event1 -> event1 instanceof KeyEvent && ((KeyEvent) event1).getCode().equals(KeyCode.SPACE))
                .ifPresent(event1 ->
                        getExperiment().terminal.set(1)
                );
    }
}
