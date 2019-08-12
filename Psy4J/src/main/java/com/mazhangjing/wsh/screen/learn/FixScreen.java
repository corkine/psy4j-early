package com.mazhangjing.wsh.screen.learn;

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
 * 学习阶段注视点的呈现类
 */
public class FixScreen extends Screen {

    @Override public Screen initScreen() {
        duration = SET.EXP_LEARN_FIX_MS.getValue();
        BorderPane pane = new BorderPane();
        Text text = new Text("+");
        text.setFont(Font.font(100));
        text.setFill(Color.BLACK);
        pane.setCenter(text);
        layout = pane;
        infomation = "学习阶段注视点屏幕";
        return this;
    }

    @Override public void eventHandler(Event event, Experiment experiment, Scene scene) { }
}
