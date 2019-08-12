package com.mazhangjing.wsh.screen.real;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;

/**
 * 测验阶段空白呈现屏幕
 */
public class BlankScreen extends Screen {
    private Integer useDynamicShowTime;

    public BlankScreen(Integer useDynamicShowTime) {
        this.useDynamicShowTime = useDynamicShowTime;
    }

    public BlankScreen() { }

    @Override
    public Screen initScreen() {
        if (useDynamicShowTime == null)
            duration = SET.EXP_BLANK_MS.getValue();
        else
            duration = useDynamicShowTime;
        layout = new HBox();
        infomation = "空白页面";
        return this;
    }

    @Override public void eventHandler(Event event, Experiment experiment, Scene scene) { }
}
