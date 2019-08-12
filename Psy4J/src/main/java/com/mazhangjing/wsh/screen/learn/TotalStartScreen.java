package com.mazhangjing.wsh.screen.learn;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
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
 * 学习阶段总的指导语呈现屏幕
 */
public class TotalStartScreen extends Screen {
    @Override
    public Screen initScreen() {
        duration = SET.TOTAL_INTRO_MS.getValue();
        BorderPane pane = new BorderPane();
        ImageView view = new ImageView();
        view.setImage(new Image("A_learn_1.png"));
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
