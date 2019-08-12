package com.mazhangjing.wsh.experiment;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.wsh.data.ExperimentData;
import com.mazhangjing.wsh.data.TrialData;
import com.mazhangjing.wsh.trial.RealTrial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 提供了检测程序的 Experiment 入口
 */
public class WshRealExperiment extends Experiment {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ExperimentData experimentData = new ExperimentData();

    @Override
    public void initExperiment() {
        trials.add(new RealTrial(experimentData).initTrial());
    }

    @Override
    public void saveData() {
        //保存日志
        File data = Paths.get(System.getProperty("user.dir") + File.separator + "log/logFile.log").toFile();
        logger.info(data.toString());
        String info = Optional.ofNullable(getGlobalData())
                .filter(data1 -> !data1.isEmpty()).filter(data2 -> !data2.equals("__"))
                .orElse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm")));
        Path newPath = Paths.get(System.getProperty("user.dir") + File.separator + "log" + File.separator + info + ".log");
        if (data.exists()) {
            logger.info("Log file Exist, moving now");
            try {
                Files.copy(data.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.info("Log file move failed.");
                e.printStackTrace();
            }
        } else {
            logger.info("Log file not exist!!");
        }

        try {
            Path path = Paths.get(System.getProperty("user.dir") + File.separator + "log" +
                    File.separator + info + ".csv");
            PrintWriter writer = new PrintWriter(path.toFile());
            writer.println("ID, SHOW_TIME, ACTION_TIME, SHOW_TIME_MS, DURATION_TIME_MS, " +
                    "STAND_SIZE, ACTION_SIZE, SIZE_IS_BIG, ACTION_ORDER, STI_IS_LEFT, CHECK_BY");
            experimentData.trialsData().forEach(trialData -> {
                if (trialData.isNormal()) {
                    String res =
                            String.format("%s, $%s$, $%s$, %s, %s, %s, %s, %s, %s, %s, %s",
                                    experimentData.userId(),
                                    trialData.showStiNanoTime(),
                                    trialData.actionTimeNanoTime(),
                                    trialData.showTimeMSFake(),
                                    trialData.durationNanoMS(),
                                    trialData.standSize(),
                                    trialData.stimulateRadius(),
                                    trialData.sizeIsBig() ? 1 : 0,
                                    trialData.orderIndex(),
                                    trialData.showByLeftOrRight().equals("LEFT") ? 1 : 0,
                                    trialData.checkBySizeOrOrder());
                    writer.println(res);
                }
            });
            writer.flush();
            writer.close();

            Path path2 = Paths.get(System.getProperty("user.dir") + File.separator + "log" +
                    File.separator + info + ".obj");
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path2.toFile()));
            stream.writeObject(experimentData);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            logger.info("Save Data to CSV/OBJ Directly failed");
            e.printStackTrace();
        }

    }
}
