package com.mazhangjing.wsh.screen.learn;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import com.mazhangjing.wsh.stimulate.CircleMap;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;

/**
 * 学习阶段刺激呈现屏幕
 */
public class ShowScreen extends Screen {

    private CircleMap circle;

    public ShowScreen(CircleMap circle) {
        this.circle = circle;
    }

    @Override
    public Screen initScreen() {
        duration = SET.SHOW_STI_MS.getValue();
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                pane.add(circle.getCircles().get(5*i + j),j,i);
            }
        }
        layout = pane;
        infomation = "学习阶段刺激展示屏幕";
        return this;
    }

    @Override public void eventHandler(Event event, Experiment experiment, Scene scene) { }
}
