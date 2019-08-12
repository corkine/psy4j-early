package com.mazhangjing.wsh.screen.real;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 检测阶段结束引导呈现屏幕
 */
public class EndScreen extends Screen {
    @Override
    public Screen initScreen() {
        duration = 10000;
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Text text = new Text("实验结束，谢谢参与！请联系实验人员。");
        text.setFill(Color.BLACK);
        text.setFont(Font.font(SET.EXP_BYE_SIZE.getValue()));
        hBox.getChildren().add(text);
        layout = hBox;
        infomation = "结束页面";
        return this;
    }

    @Override public void eventHandler(Event event, Experiment experiment, Scene scene) { }
}
