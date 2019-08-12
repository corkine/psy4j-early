package com.mazhangjing.zsw.data;

import com.mazhangjing.zsw.experiment.ZswLog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;

/**
 * 用于进行数据单个处理、批处理和数据分析的 GUI 界面。
 */
public class DataForm extends Application {

    private ObservableList<String> list = FXCollections.observableArrayList(); {
        list.add("计算曲下线面积");
        list.add("计算分段 1");
        list.add("计算分段 2");
        list.add("计算分段 3");
    }

    private ObservableList<String> list1 = FXCollections.observableArrayList(); {
        list1.add("批处理 - 寻找 Size");
        list1.add("批处理 - 寻找 Number");
    }

    private SimpleBooleanProperty logProcessChooseCSVWithCoordinate = new SimpleBooleanProperty(false);

    private Scene initScene() {
        ChoiceBox<String> computeAreaMethodChoiceBox = new ChoiceBox<>();
        computeAreaMethodChoiceBox.setItems(list); computeAreaMethodChoiceBox.getSelectionModel().selectFirst();
        ChoiceBox<String> lookForSizeOrNumberChoiceBox = new ChoiceBox<>();
        lookForSizeOrNumberChoiceBox.setItems(list1); lookForSizeOrNumberChoiceBox.getSelectionModel().selectFirst();
        TextField pathWay = new TextField();
        pathWay.setPromptText("Input Your Log file Path without Name and \"/\"");
        pathWay.setText(".");
        TextField dataPath = new TextField();
        dataPath.setPromptText("Input Experiment Log file Name");
        TextField logPath = new TextField();
        logPath.setPromptText("Input Mouse Log file Path Name");
        TextField relativeTime = new TextField();
        relativeTime.setPromptText("Input a relative time String from Experiment Log file");
        TextField realTime = new TextField();
        realTime.setPromptText("Input a human read time from Experiment Log file, need same Line with Relative Time");
        Button doIt = new Button("Single Process");
        Button batch = new Button("CSV Batch");
        Button doItFast = new Button("Log Batch");

        CheckBox checkBox = new CheckBox("带坐标");
        checkBox.selectedProperty().bindBidirectional(logProcessChooseCSVWithCoordinate);

        HBox hbox = new HBox();
        hbox.getChildren().addAll(computeAreaMethodChoiceBox, checkBox, new SplitPane(), doIt, new Label("OR"), doItFast);
        hbox.setAlignment(Pos.CENTER); hbox.setSpacing(20);

        HBox hbox2 = new HBox();
        hbox2.getChildren().addAll(lookForSizeOrNumberChoiceBox, batch);
        hbox2.setAlignment(Pos.CENTER); hbox2.setSpacing(20);

        VBox box = new VBox();
        box.getChildren().addAll(pathWay, dataPath, logPath, realTime, relativeTime, hbox, hbox2);
        box.setAlignment(Pos.CENTER);
        VBox.setMargin(doIt, new Insets(20,0,0,0));
        box.setPadding(new Insets(0,100,0,100));
        box.setSpacing(10);

        doIt.setOnAction(event -> {
            Task task = new Task() {
                @Override
                protected Object call() throws IOException {
                    DataCollect collect = new DataCollect(
                            pathWay.getText().trim(), dataPath.getText().trim(),
                            logPath.getText().trim(), realTime.getText().trim(), relativeTime.getText().trim()
                    );

                    String res = computeAreaMethodChoiceBox.getSelectionModel().getSelectedItem().toUpperCase();
                    collect.computeFullArea = false; collect.computeCutArea = -1;
                    collect.produceCoordinateCsvFile = logProcessChooseCSVWithCoordinate.get();
                    if (res.contains("1")) collect.computeCutArea = 1;
                    else if (res.contains("2")) collect.computeCutArea = 2;
                    else if (res.contains("3")) collect.computeCutArea = 3;
                    else collect.computeFullArea = true;

                    collect.doAction();
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    Platform.runLater(() -> {
                        doIt.setText("Single Process");
                        doIt.setDisable(false);
                    });
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("数据保存在 Path 文件夹中，以 csv 结尾。");
                        alert.show();
                    });
                }
            };

            task.exceptionProperty().addListener((e)-> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText("处理数据过程中出现错误");
                String err = "";
                err += "在处理过程中出现错误。";
                if (task.getException() != null && task.getException().getMessage() != null) {
                    StringWriter sw = new StringWriter();
                    task.getException().printStackTrace(new PrintWriter(sw));
                    err += "\n" + sw.toString();
                }
                alert.setContentText(err);
                alert.showAndWait().ifPresent((response)->{
                    doIt.setDisable(false);
                    doIt.setText("Single Process");
                });
            });

            new Thread(task).start();
            doIt.setText("Processing...");
            doIt.setDisable(true);
        });

        batch.setOnAction(event -> {
            Task task2 = new Task() {
                @Override
                protected Object call() {
                    //boolean lookForSizeRes = lookForSize.getText().trim().contains("1");
                    boolean lookForSizeRes = lookForSizeOrNumberChoiceBox.getSelectionModel().getSelectedItem().toUpperCase().contains("SIZE");
                    DataBatch.runInJava(Paths.get(pathWay.getText().trim()),lookForSizeRes);
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    Platform.runLater(() -> {
                        batch.setText("CSV Batch");
                        batch.setDisable(false);
                    });
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("数据保存在 Path 文件夹中，以 csv 结尾。");
                        alert.show();
                    });
                }
            };

            task2.exceptionProperty().addListener((e)-> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText("处理数据过程中出现错误");
                String err = "";
                err += "在处理过程中出现错误。";
                if (task2.getException() != null && task2.getException().getMessage() != null) {
                    StringWriter sw = new StringWriter();
                    task2.getException().printStackTrace(new PrintWriter(sw));
                    err += "\n" + sw.toString();
                }
                alert.setContentText(err);
                alert.showAndWait().ifPresent((response)->{
                    batch.setDisable(false);
                    batch.setText("CSV Batch");
                });
            });

            new Thread(task2).start();
            batch.setText("Processing...");
            batch.setDisable(true);
        });

        doItFast.setOnAction(event -> {
            Task task2 = new Task() {
                @Override
                protected Object call() {
                    String res = computeAreaMethodChoiceBox.getSelectionModel().getSelectedItem().toUpperCase();
                    LogDataCollect.runInJava(res, logProcessChooseCSVWithCoordinate.get(), ".");
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    Platform.runLater(() -> {
                        doItFast.setText("Log Batch");
                        doItFast.setDisable(false);
                    });
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("数据保存在 Path 文件夹中，以 csv 结尾。");
                        alert.show();
                    });
                }
            };

            task2.exceptionProperty().addListener((e)-> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText("处理数据过程中出现错误");
                String err = "";
                err += "在处理过程中出现错误。";
                if (task2.getException() != null && task2.getException().getMessage() != null) {
                    StringWriter sw = new StringWriter();
                    task2.getException().printStackTrace(new PrintWriter(sw));
                    err += "\n" + sw.toString();
                }
                alert.setContentText(err);
                alert.showAndWait().ifPresent((response)->{
                    doItFast.setDisable(false);
                    doItFast.setText("Log Batch");
                });
            });

            new Thread(task2).start();
            doItFast.setText("Processing...");
            doItFast.setDisable(true);
        });

        return new Scene(box, 800, 600);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(initScene());
        primaryStage.setTitle("Experiment DataCollect Powered By Scala，v" + new ZswLog().getCurrentVersion());
        primaryStage.show();
    }
}
