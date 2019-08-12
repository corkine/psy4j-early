package com.mazhangjing.zsw.trial;

import com.mazhangjing.lab.Screen;
import com.mazhangjing.lab.ScreenBuilder;
import com.mazhangjing.lab.Trial;
import com.mazhangjing.zsw.SET;
import com.mazhangjing.zsw.screen.InfoScreen;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * 信息收集试次
 */
public class InfoTrial extends Trial {
    @Override
    public Trial initTrial() {

        information = "信息收集序列";
        screens.add(new InfoScreen().initScreen());

        //screens.add(screen);
        return this;
    }
}
