package com.mazhangjing.wsh.screen.real;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 检测阶段注视点呈现屏幕
 */
public class FixScreen extends Screen {
    @Override
    public Screen initScreen() {
        duration = SET.EXP_FIX_MS.getValue();
        BorderPane pane = new BorderPane();
        Text text = new Text("+");
        text.setFont(Font.font(SET.EXP_FIX_SIZE.getValue()));
        text.setFill(Color.BLACK);
        pane.setCenter(text);
        layout = pane;
        infomation = "注视点页面";
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) { }
}
