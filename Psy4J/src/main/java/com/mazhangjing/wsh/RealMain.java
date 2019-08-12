package com.mazhangjing.wsh;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.lab.Log;
import com.mazhangjing.lab.Screen;
import com.mazhangjing.voice.Capture;
import com.mazhangjing.wsh.experiment.WshExperiment;
import com.mazhangjing.wsh.experiment.WshLog;
import com.mazhangjing.wsh.experiment.WshRealExperiment;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
 * @apiNote 2019年03月11日 更新了 Java Sound API 的实现
 */
public class RealMain extends Application {

    private final Log log = new WshLog();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Experiment experiment;

    private Screen currentScreen;

    private ScheduledThreadPoolExecutor executor;

    private static SimpleIntegerProperty terminal;

    private Runnable changeTask;

    private final Scene scene = new Scene(drawWelcomeContent(),400,300);

    @Override
    public void init() throws Exception {
        Parameters parameters = getParameters();
        String is_pre_exp = parameters.getNamed().getOrDefault("is_pre_exp", "1");
        String exp_number = parameters.getNamed().getOrDefault("exp_number", "2");
        Config.setIsPreExperiment(is_pre_exp.contains("1"));
        Config.setExperimentNumber(Integer.parseInt(exp_number));
        initExperiment(new WshRealExperiment());
    }

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

    private void setTimer(Integer duration) {
        executor.getQueue().clear();
        logger.debug("RunTimer with max " + duration + "ms to act");
        executor.schedule(changeTask, duration, TimeUnit.MILLISECONDS);
    }

    private void setRecognizer() {
        Capture capture = new Capture();
        capture.messageProperty().addListener(e -> {
            //当数据发生变化时，调用此监听器，如果数据不等于 0.0 则调用逻辑传递信息给 currentScreen
            Double value = Double.valueOf(capture.getMessage());
            if (!value.equals(0.0)) {
                logger.debug("Receive Sound Event Now..., Current Sound Signal is " + value);
                currentScreen.eventHandler(new Event(Event.ANY), experiment, scene);
            }
        });
        new Thread(capture).start();
    }

    private Parent drawWelcomeContent() {
        BorderPane root = new BorderPane();
        Button start = new Button("Start Experiment");
        start.setEffect(new DropShadow(10, Color.GREY));
        start.setFont(Font.font(20));
        root.setCenter(start);
        Text version = new Text(log.getCurrentVersion());
        Text text = new Text(log.getLog());
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
        setRecognizer();
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

    public static void main(String[] args) { launch(args); }
}
