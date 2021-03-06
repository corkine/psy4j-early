package com.mazhangjing.zsw.screen;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.zsw.SET;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 准备开始屏幕
 */
public class StartScreen extends Screen {
    @Override
    public Screen initScreen() {

        duration = SET.START_CLICK_MS.getValue();
        infomation = "准备开始屏幕";
        BorderPane box = new BorderPane();
        Text text = new Text("+");
        text.setFont(Font.font(50)); text.setFill(Color.BLACK);
        box.setCenter(text);

        Button btn = new Button("START");
        HBox btns = new HBox();
        btns.getChildren().add(btn);
        btns.setAlignment(Pos.BOTTOM_CENTER);
        box.setBottom(btns);
        layout = box;


        btn.setOnAction(event -> getExperiment().terminal.set(1));

        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
    }
}
