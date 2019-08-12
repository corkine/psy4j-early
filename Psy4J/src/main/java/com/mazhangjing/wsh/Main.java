package com.mazhangjing.wsh;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Log;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.wsh.experiment.WshExperiment;
import com.mazhangjing.wsh.experiment.WshLog;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Psy4J 早期的主要 JavaFx 界面，其定义了定时器、
 * 单线程计时器，是 Psy4J 的主要实现部分，Experiment 需要传入这里才能被 JavaFx 接管运行。
 */
public class  Main extends Application {

    private final Log log = new WshLog();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Experiment experiment;

    private Screen currentScreen;

    private ScheduledThreadPoolExecutor executor;

    private static SimpleIntegerProperty terminal;

    private Runnable changeTask;

    private final Scene scene = new Scene(drawWelcomeContent(),400,300);

    private void initExperiment(Experiment experiment) {
        this.experiment = experiment;
        experiment.initExperiment();
        //注入依赖
        experiment.initScreensAndTrials(scene);
        //设置状态、单任务定时器策略，定时器执行的具体行为
        currentScreen = experiment.getScreen();
        terminal = experiment.terminal;
        terminal.addListener((observable, oldV, newV)->{
            if (oldV.intValue() == 0 && newV.intValue() == 1) {
                nextScreen();
                terminal.set(0); //不能写在changeScreen中，否则会递归导致失败
            }
        });
        changeTask = () -> terminal.set(1);
        executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
    }

    private void nextScreen() { experiment.release(); setScreen(); }

    private void setScreen() {
        //首先处理页面遗留问题
        try {
            currentScreen.callWhenLeavingScreen();
        } catch (Exception e) { logger.warn(e.getMessage()); }
        //之后重载页面
        currentScreen = experiment.getScreen();
        logger.debug("Current Screen is " + currentScreen);
        //绘制GUI
        if (currentScreen == null) drawEndContent();
        else {
            Platform.runLater(() -> {
                scene.setRoot(currentScreen.layout);
                try {
                    currentScreen.callWhenShowScreen();
                } catch (Exception e) { logger.warn(e.getMessage()); }
            });
            //添加时间监听器
            Platform.runLater(()-> setTimer(currentScreen.duration));
        }
    }

    @Override
    public void init() throws Exception {
        Parameters parameters = getParameters();
        String is_pre_exp = parameters.getNamed().getOrDefault("is_pre_exp", "1");
        String exp_number = parameters.getNamed().getOrDefault("exp_number", "2");
        Config.setIsPreExperiment(is_pre_exp.contains("1"));
        Config.setExperimentNumber(Integer.parseInt(exp_number));
        initExperiment(new WshExperiment());
    }

    private void setTimer(Integer duration) {
        executor.getQueue().clear();
        logger.debug("RunTimer with max " + duration + "ms to act");
        executor.schedule(changeTask, duration, TimeUnit.MILLISECONDS);
    }

    private Parent drawWelcomeContent() {
        BorderPane root = new BorderPane();
        Button start = new Button("Start Experiment");
        start.setEffect(new DropShadow(10, Color.GREY));
        start.setFont(Font.font(20));
        root.setCenter(start);
        Text text = new Text( log.getLog());
        Text version = new Text(log.getCurrentVersion());

        VBox infoBox = new VBox(); infoBox.setSpacing(10); infoBox.getChildren().addAll(version, text);
        infoBox.setAlignment(Pos.BOTTOM_LEFT);
        root.setBottom(infoBox);
        Text copy = new Text(log.getCopyRight());
        root.setTop(copy);
        start.setOnAction((event -> setScreen()));
        return root;
    }

    private void drawEndContent() {
        executor.shutdownNow(); //关闭定时器
        FlowPane root = new FlowPane(); root.setAlignment(Pos.CENTER);
        Label label = new Label("End of Experiment");
        label.setFont(Font.font(30));
        root.getChildren().add(label);
        scene.setRoot(root);
    }

    private void addEventHandler(Event event) {
        if (currentScreen != null) currentScreen.eventHandler(event,experiment,scene);
    }

    @Override @SuppressWarnings("unchecked")
    public void start(Stage primaryStage) {
        primaryStage.setScene(scene);
        scene.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler) this::addEventHandler);
        scene.addEventHandler(KeyEvent.KEY_PRESSED,(EventHandler) this::addEventHandler);
        //scene.addEventHandler(MouseEvent.MOUSE_MOVED, (EventHandler) this::addEventHandler);
        if (SET.FULL_SCREEN.getValue() == 1) {
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.valueOf("F11"));
        }
        primaryStage.setWidth(900.0);
        primaryStage.setHeight(600.0);
        primaryStage.setTitle(
                String.format("Experiment - %s - A Bad Girl - Powered by PSY4J - http://psy4j.mazhangjing.com",
                        log.getCurrentVersion() + " - " + Config.getExperimentNumber()));

        //添加样式表文件
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getClassLoader().getResource("style.css")).toExternalForm());

        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            experiment.saveData();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
