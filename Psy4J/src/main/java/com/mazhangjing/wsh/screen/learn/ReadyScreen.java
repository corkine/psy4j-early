package com.mazhangjing.wsh.screen.learn;

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
 * 学习阶段指导语呈现屏幕
 */
public class ReadyScreen extends Screen {

    @Override public Screen initScreen() {
        duration = SET.INTRO_START_MS.getValue();
        BorderPane pane = new BorderPane();
        ImageView view = new ImageView();
        String url = "";
        if (Config.experimentNumber() == 1) url = "A_learn_2.png";
        else if (Config.experimentNumber() == 2) url = "B_learn_2.png";
        else if (Config.experimentNumber() == 3) url = "C_learn_2.png";
        else if (Config.experimentNumber() == 4) url = "D_learn_2.png";
        view.setImage(new Image(url));
        view.setPreserveRatio(true);
        view.setFitWidth(SET.EXP_PICTURE_WIDTH.getValue());
        pane.setCenter(view);
        layout = pane;
        return this;
    }

    @Override public void eventHandler(Event event, Experiment experiment, Scene scene) {
        Optional.of(event)
                .filter(event1 -> event1 instanceof KeyEvent && ((KeyEvent) event1).getCode().equals(KeyCode.SPACE))
                .ifPresent(event1 ->
                        getExperiment().terminal.set(1)
                );
    }
}
