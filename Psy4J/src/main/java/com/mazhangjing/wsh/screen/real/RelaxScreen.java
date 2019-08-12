package com.mazhangjing.wsh.screen.real;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Optional;

/**
 * 测试阶段休息呈现屏幕
 */
public class RelaxScreen extends Screen {
    @Override
    public Screen initScreen() {
        this.duration = SET.EXP_RELAX_MS.getValue();
        BorderPane pane = new BorderPane();
        Text text = new Text("请休息，准备好后，按下 空格 键继续实验。");
        text.setFont(Font.font("楷体",SET.INTRO_FONT_SIZE.getValue()));
        pane.setCenter(text);

        this.layout = pane;
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
        Optional.ofNullable(event)
                .filter(event1 -> event1 instanceof KeyEvent && ((KeyEvent) event1).getCode().equals(KeyCode.SPACE))
                .ifPresent(event1 ->
                        getExperiment().terminal.set(1)
                );
    }
}
