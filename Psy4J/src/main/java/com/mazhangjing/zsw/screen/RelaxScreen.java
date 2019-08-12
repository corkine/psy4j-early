package com.mazhangjing.zsw.screen;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.LabUtils;
import com.mazhangjing.lab.Screen;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Optional;
import static com.mazhangjing.lab.LabUtils.*;

/**
 * 休息屏幕
 */
public class RelaxScreen extends Screen {
    @Override
    public Screen initScreen() {
        this.duration = 10000000;
        BorderPane pane = new BorderPane();
        Text text = new Text("请休息，准备好后，按下 空格 键继续实验。");
        text.setFont(Font.font("楷体",20.0));
        pane.setCenter(text);

        this.layout = pane;
        return this;
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
        ifKeyButton(KeyCode.SPACE, event, experiment, scene, (exp, sce) -> {
            LabUtils.goNextScreenUnSafe(experiment);
            return null;
        });
    }
}
