package com.mazhangjing.wsh.screen.test;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.SET;
import com.mazhangjing.wsh.stimulate.Result;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 检测阶段成绩统计和展示屏幕
 */
public class ScoreScreen extends Screen {
    private Text text = new Text();
    private Result result;
    public ScoreScreen(Result result) {
        this.result = result;
    }

    @Override
    public void callWhenShowScreen() {
        String success = "已完成此部分实验，请联系主试以进行下一部分实验。";
        String failed = "正确率未达标: 标准为百分之" + SET.EXP_CORRECT_RATE.getValue()
                + "， 你的成绩为 百分之" + (Math.toIntExact((long) (result.getRightRate() * 100)))
                + "\n\n点击空格继续实验";
        text.setText((Math.toIntExact((long) (result.getRightRate() * 100)) > SET.EXP_CORRECT_RATE.getValue() ? success : failed));
        new Thread(this::saveLogData).start();
    }

    @Override
    public Screen initScreen() {
        duration = SET.SCORE_SHOW_MS.getValue();
        BorderPane pane = new BorderPane();
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFill(Color.BLACK);
        text.setFont(Font.font(SET.SCORE_FONT_SIZE.getValue()));
        text.setText("The text you should not see.");
        pane.setCenter(text);
        layout = pane;
        return this;
    }

    private void saveLogData() {
        logger.info("In Saving Method...");
        //保存 Seed
        String name = "test_error.log";
        //保存日志
        File data = Paths.get(System.getProperty("user.dir") + File.separator + name).toFile();
        String old_files = "";
        //从原来文件中读取，然后再往下写
        if (data.exists()) {
            logger.info("Old TEST_ERROR FILE EXIST！");
            try {
                FileReader fr = new FileReader(data);
                BufferedReader bf = new BufferedReader(fr);
                old_files = Optional.ofNullable(bf.lines().collect(Collectors.joining("\n")))
                        .orElse("");
                old_files += "\n";
                fr.close();
                bf.close();
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }
        }
        //将当前结果写出到文件
        old_files += LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")) + " ||| " +
                result.toString() + " ||| Right Rate is: " + result.getRightRate();
        logger.info("NEW LING IS " + old_files);
        try {
            PrintWriter wr = new PrintWriter(data);
            wr.print(old_files);
            wr.flush();
            wr.close();
        } catch (FileNotFoundException e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void eventHandler(Event event, Experiment experiment, Scene scene) {
        Optional.of(event)
                .filter(event1 -> event1 instanceof KeyEvent && ((KeyEvent) event1).getCode().equals(KeyCode.SPACE))
                .ifPresent(event1 ->
                        getExperiment().terminal.set(1)
                );
    }
}
