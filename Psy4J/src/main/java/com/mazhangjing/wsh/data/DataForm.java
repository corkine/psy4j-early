package com.mazhangjing.wsh.data;

import com.mazhangjing.wsh.experiment.WshLog;
import javafx.application.Application;
import javafx.application.Platform;
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

/**
 * 本类用于提供单个 .log 提取为 .csv 的 GUI 界面（第一个按钮） - 调用 DataProcess 类，
 * 一个文件夹内 .log 批量提取为 .csv 的 GUI 界面（第二个按钮） - 调用 DataCollect - DataProcess 类，
 * 一个文件夹内 .csv 批量提取为 final_csv.csv 和 stat_info.csv 的 GUI 界面(第三个按钮) - 调用 DataBatch 类
 */
public class DataForm extends Application {

    private ObservableList<String> list = FXCollections.observableArrayList();

    {
        list.add("预实验");
        list.add("实验一");
        list.add("实验二");
        list.add("实验三");
        list.add("实验四");
    }

    private Scene initScene() {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setItems(list); choiceBox.getSelectionModel().selectFirst();
        HBox cbox = new HBox(); cbox.setSpacing(10); cbox.setAlignment(Pos.CENTER_LEFT);
        cbox.getChildren().addAll(new Label("选择你需要批处理的实验"), choiceBox);
        TextField pathWay = new TextField();
        pathWay.setPromptText("Input Your Log file Path without Name and \"/\"");
        pathWay.setText(".");
        TextField dataPath = new TextField();
        dataPath.setPromptText("Input Experiment Log file Name");
        TextField relativeTime = new TextField();
        relativeTime.setPromptText("Input a relative time String from Experiment Log file");
        TextField realTime = new TextField();
        realTime.setPromptText("Input a human read time from Experiment Log file, need same Line with Relative Time");
        Button doIt = new Button("Log Process");
        Button reDoIt = new Button("Data Process");
        Button doItBatch = new Button("Log Batch Process");
        VBox box = new VBox();
        HBox btns = new HBox(); btns.setAlignment(Pos.CENTER); btns.setSpacing(30);
        btns.getChildren().addAll(doIt, doItBatch, reDoIt);
        box.getChildren().addAll(cbox, pathWay, dataPath, realTime, relativeTime, btns);
        box.setAlignment(Pos.CENTER);
        VBox.setMargin(doIt, new Insets(20,0,0,0));
        box.setPadding(new Insets(0,100,0,100));
        box.setSpacing(10);


        doIt.setOnAction(event -> {
            Task task = new Task() {
                @Override
                protected Object call() throws IOException {
                    DataProcess collect = new DataProcess(
                            pathWay.getText().trim(), dataPath.getText().trim(),
                            realTime.getText().trim(), relativeTime.getText().trim()
                    );
                    collect.doAction();
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    Platform.runLater(() -> {
                        doIt.setText("Log Process");
                        doIt.setDisable(false);
                    });
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("数据保存在" + (pathWay.getText().equals(".") ? "当前" : pathWay.getText()) + "文件夹中，以 csv 结尾。");
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
                    doIt.setText("Log Process");
                });
            });

            new Thread(task).start();
            doIt.setText("Processing...");
            doIt.setDisable(true);
        });

        doItBatch.setOnAction(event -> {
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    DataCollect.runInJava();
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    Platform.runLater(() -> {
                        doItBatch.setText("Log Batch Process");
                        doItBatch.setDisable(false);
                    });
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("数据保存在" + (pathWay.getText().equals(".") ? "当前" :
                                pathWay.getText()) + "文件夹中，以 CSV 结尾。此外，日志文件保存在 batch.log 中。");
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
                    doItBatch.setDisable(false);
                    doItBatch.setText("Log Batch Process");
                });
            });

            new Thread(task).start();
            doItBatch.setText("Processing...");
            doItBatch.setDisable(true);
        });

        reDoIt.setOnAction(event -> {
            Task task = new Task() {
                @Override
                protected Object call() throws IOException {
                   DataBatch.runInJava(choiceBox.getSelectionModel().getSelectedItem().contains("预实验"),
                           choiceBox.getSelectionModel().getSelectedItem().contains("实验一"),
                           choiceBox.getSelectionModel().getSelectedItem().contains("实验二"),
                           choiceBox.getSelectionModel().getSelectedItem().contains("实验三"),
                           choiceBox.getSelectionModel().getSelectedItem().contains("实验四"));
                   return null;
                }

                @Override
                protected void done() {
                    super.done();
                    Platform.runLater(() -> {
                        reDoIt.setText("Data Process");
                        reDoIt.setDisable(false);
                    });
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("数据保存在" + (pathWay.getText().equals(".") ? "当前" : pathWay.getText()) + "文件夹中，以 _final.csv 结尾，日志保存在 result.log 中。");
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
                    reDoIt.setDisable(false);
                    reDoIt.setText("Data Process");
                });
            });

            new Thread(task).start();
            reDoIt.setText("Processing...");
            reDoIt.setDisable(true);
        });

        return new Scene(box, 800, 600);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(initScene());
        primaryStage.setTitle("DataForm v" + new WshLog().getCurrentVersion());
        primaryStage.show();
    }
}
