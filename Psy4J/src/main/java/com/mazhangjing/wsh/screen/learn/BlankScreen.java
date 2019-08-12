package com.mazhangjing.wsh.screen.learn;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;

/**
 * 学习阶段的空白屏幕，呈现时间根据 SET 枚举类来确定
 */
public class BlankScreen extends Screen {
    @Override
    public Screen initScreen() {
        duration = SET.EXP_LEARN_BLANK_MS.getValue();
        layout = new HBox();
        infomation = "学习阶段空白屏幕";
        return this;
    }

    @Override public void eventHandler(Event event, Experiment experiment, Scene scene) { }
}
