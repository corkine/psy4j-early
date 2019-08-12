package com.mazhangjing.wsh.experiment;

import com.mazhangjing.lab.Experiment;
import com.mazhangjing.wsh.stimulate.StiFactory;
import com.mazhangjing.wsh.trial.LearnTrial;
import com.mazhangjing.wsh.trial.TestTrial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 本类是 Experiment 类的继承类，是最主要的刺激呈现、记录和反馈的入口 - 区别于 WshRealExperiment，本类定义的是学习和测验，而后者定义的是检测
 */
public class WshExperiment extends Experiment {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 用来初始化实验，这里主要先预加载了 seed.log 文件以定义刺激样式，同时添加了练习和测验 Trial
     */
    @Override public void initExperiment() {
        try {
            StiFactory.loadSeedForStaticUsage("seed.log");
        } catch (Exception ignore) {
        }

        trials.add(new LearnTrial(true).initTrial());
        trials.add(new TestTrial().initTrial());
    }

    /**
     * 用来保存 seed.log 文件以备下次使用、保存并且重命名日志文件以备提取实验结果数据。
     */
    @Override public void saveData() {
        //保存 Seed
        String name = "seed.log";
        logger.info("Saving Seed For next time use...");
        try {
            StiFactory.saveSeedAsFile(name);
        } catch (FileNotFoundException e) {
            logger.info("Save Failed, caused by " + e.getMessage());
        }
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

    }
}
