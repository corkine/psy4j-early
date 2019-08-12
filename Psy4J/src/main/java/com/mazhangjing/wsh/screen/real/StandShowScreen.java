package com.mazhangjing.wsh.screen.real;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import com.mazhangjing.wsh.stimulate.CircleMap;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * 标准刺激呈现屏幕
 */
public class StandShowScreen extends Screen {
    private CircleMap map;
    public StandShowScreen(CircleMap map) {
        this.map = map;
    }
    @Override
    public Screen initScreen() {
        duration = SET.EXP_STAND_STI_SHOW_MS.getValue();
        VBox box = new VBox();
        GridPane pane2 = new GridPane();
        pane2.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                pane2.add(map.getCircles().get(5*i + j),j,i);
            }
        }
        Text text = new Text("标准刺激");
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font(SET.INTRO_FONT_SIZE.getValue()));
        box.getChildren().addAll(text,pane2);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(20.0);
        layout = box;
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {

    }
}
