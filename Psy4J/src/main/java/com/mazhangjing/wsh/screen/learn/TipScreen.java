package com.mazhangjing.wsh.screen.learn;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.Config;
import com.mazhangjing.wsh.SET;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * 学习阶段被试提醒呈现屏幕
 */
public class TipScreen extends Screen {

    private String getTEXT() {
        String text = "";
        if (Config.experimentNumber() == 1) {
            text = "请记住每种点阵的尺寸";
        } else if (Config.experimentNumber() == 2) {
            text = "请记住每种点阵的顺序";
        } else {
            text = "请记住每种点阵的尺寸和顺序";
        }
        return text;
    }

    @Override
    public Screen initScreen() {
        duration = SET.TIP_MS.getValue();
        BorderPane pane = new BorderPane();
        Text text = new Text();
        text.setText(getTEXT());
        text.setFont(Font.font("宋体",SET.INTRO_FONT_SIZE.getValue()));
        text.setLineSpacing(12.0);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setWrappingWidth(800.0);
        pane.setCenter(text);
        layout = pane;
        return this;
    }

    @Override public void eventHandler(Event event, Experiment experiment, Scene scene) { }
}
