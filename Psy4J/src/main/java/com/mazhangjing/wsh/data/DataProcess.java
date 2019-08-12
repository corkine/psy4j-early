package com.mazhangjing.wsh.data;

import com.mazhangjing.wsh.SET;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本类用来从 .log 文件中提取实验得到的结果，并且导出为 .csv 文件。注意，这是数据提取的核心程序，对于一个文件夹中的批处理程序，使用 DataCollect 进行操作。
 * 操作后的数据，即 .csv 文件还需要通过 DataBatch 脚本以进行统计和分析。
 * @version 1.0.0 2019年03月09日 更新了静态展示刺激的收集问题
 * @apiNote 本程序输出数据，如果一行没有反应，也输出，但是输出行的列数不够，以备区分
 *          在调试程序前，要注释掉带有参数的构造器，修改 PathString 和 DataName、standLocalTime、standRelativeTime，此外，对于使用此构造器
 *          的别的类，也要记性注释。
 *          在处理数据的时候 isXXXLine 极其依赖于程序生成的日志
 *          在生成数据的时候生成的 CSV 文件依赖于 id 和 name，这两者均不能为空
 */
public class DataProcess {

    /**
     * 提供标准被试信息，比如文件路径、名称、对应时刻，用于测试
     */
    private static Logger logger = LoggerFactory.getLogger(DataProcess.class);
    private String PathString = "/Users/corkine/Desktop";
    private String PathString2 = "C:\\工作文件夹\\cmPsyLab\\Lab\\Lab\\src\\main\\resources\\wsh_test_batch_log";
    private String DataName = "5-7_zhangguangying_female.log";
    private String standLocalTime = "10:54:35.882";
    private String standRelativeTime = "9533780756167";

    /**
     * 供 GUI 程序调用的外部构造器接口
     * @throws FileNotFoundException 找不到文件
     */
    public DataProcess(String PathString, String DataName, String standLocalTime, String standRelativeTime) throws FileNotFoundException {
        this.PathString = PathString;
        this.DataName = DataName;
        this.standLocalTime = standLocalTime;
        this.standRelativeTime = standRelativeTime;
        initReader();
    }

    /**
     * 供测试使用的内部构造器接口
     * @throws FileNotFoundException 找不到文件
     */
    public DataProcess() throws FileNotFoundException {
        initReader();
    }

    /**
     * 根据基本文件信息读取到 Reader 中，以备数据处理
     */
    private File dataFile;
    private BufferedReader dataReader;

    /**
     * 从基本文件路径中打开文件，读取到 Reader 中
     * @throws FileNotFoundException 找不到文件
     */
    private void initReader() throws FileNotFoundException {
        dataFile = Paths.get(PathString + File.separator + DataName).toFile();
        dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), Charset.forName("GBK")));
    }

    /**
     * 被试元信息
     */
    private String id;
    private String name;
    private String gender;

    /**
     * 解析一行是否为被试原信息行
     * @param line 传入的数据行
     * @return 是/否
     */
    private Boolean isMetaDataLine(String line) {
        return line.contains("InfoScreen") && line.contains("METADATA");
    }

    /**
     * 解析被试信息行
     * @param line 传入的被试信息行
     */
    private void parseMetaData(String line) {
        String[] data = line.split(" - ")[1].split("::");
        id = data[1];
        name = data[2];
        gender = data[3];
    }

    /**
     * 解析 ShowScreen 上的数据，包括是否正式实验、是否点击左侧、根据 ORDER 还是 SIZE 做出的反应，做出反应时的时间
     */
    private Boolean isNormal;
    private Boolean isLeft;
    private String checkBy;
    private String showStiHumanTime;
    private Integer showCurrentActionIndex = -1;
    private Integer showCurrentActionSize = -1;

    /**
     * 是否是 ShowScreen 行
     * @param line 传入的需要检测的行
     * @return 是/否
     */
    private Boolean isShowStiLine(String line) {
        return line.contains("real.ShowScreen") && line.contains("current show is normal?") && line.contains("current show is left?");
    }

    /**
     * 解析 ShowScreen 行
     * @param line 经过过滤的需要解析的行
     */
    private void parseShowStiData(String line) {
        //09:06:09.522 [JavaFX Application Thread] INFO  c.m.wsh.screen.real.ShowScreen -
        //Showing sti now...current show is SizeCheck ?ORDER current show is normal? false::: current show is left? RIGHT
        String[] split1 = line.split("\\.\\.\\.");
        String time = split1[0].split(" \\[")[0];
        String[] split = split1[1].split(":::"); //current show ... false, is left? LEFT, -1, 56
        showStiHumanTime = time.trim();
        isNormal = split[0].trim().toUpperCase().endsWith("TRUE");
        isLeft = split[1].trim().toUpperCase().endsWith("LEFT");
        try {
            showCurrentActionIndex = Integer.valueOf(split[2].trim().toUpperCase());
            showCurrentActionSize = Integer.valueOf(split[3].trim().toUpperCase());
        } catch (Exception ignore) { }
        if (line.contains("SIZE")) {
            checkBy = "SIZE";
        } else if (line.contains("ORDER")) {
            checkBy = "ORDER";
        } else {
            checkBy = "NONE";
            logger.warn("CHECK_BY IS NONE, IT IS NOT RIGHT");
        }
    }


    /**
     * 用来处理 Trial 的分界，当返回 True 的时候，对中间数据进行一次处理，因此对于第一次数据无法处理，所以，当结束的时候，再进行一次处理
     * @param line 输入的行
     * @return 是否是 Trial 的分界
     */
    private Boolean isFixPointLine(String line) {
        return line.contains("Current Screen is 注视点页面") || line.contains("Current Screen is 结束页面");
    }

    /**
     * 对得到的结果行进行处理，包括绝对时间、相对高精度时间、当前呈现的尺寸大小、标准的尺寸大小、当前呈现的刺激的顺序
     */
    private String humanActionTime;
    private String nanoActionTime;
    private Integer currentActionSize;
    private Integer standCircleMapSize = SET.PIXEL_270_SIZE.getValue();
    private Integer currentActionIndex;

    /**
     * 解析是否为结果行
     * @param line 传入的需要检测的行
     * @return 是/否是结果行
     */
    private Boolean isActionLine(String line) {
        return line.contains("real.ShowScreen") && line.contains("Get result and go now");
    }

    /**
     * 解析结果行为结果需要的数据
     * @param line 需要解析为结果变量的行
     */
    private void parseActionData(String line) {
        //15:01:07.627 [Thread-6] INFO  c.m.wsh.screen.real.ShowScreen - Get result and go now... - :::3:::20:::11528122620447
        //解析时间信息
        String[] split = line.split(" \\[");
        humanActionTime = split[0].trim();
        //解析大小信息
        String[] data = line.split(":::");
        currentActionIndex = Integer.valueOf(data[1].trim());
        //解析先后信息
        currentActionSize = Integer.valueOf(data[2].trim());
        //解析相对时间信息
        nanoActionTime = data[3].trim();
    }

    /**
     * 每个被试最后的数据汇总
     */
    private StringBuilder finalData = new StringBuilder();

    //ID 在最后生成，此处不生成
    { finalData.append("ID, SHOW_TIME, ACTION_TIME, SHOW_TIME_MS, DURATION_TIME_MS, STAND_SIZE, ACTION_SIZE, SIZE_IS_BIG, ACTION_ORDER, STI_IS_LEFT, CHECK_BY\n"); }

    /**
     * 根据刺激行和结果行输出一个 Trial 的数据，根据 nanoActionTime 判断是否是没有结果行的 Trial
     * @param isLeft 刺激是否展示在左侧
     * @param nanoStiHumanTime 转换过的刺激呈现的绝对时间
     * @param nanoActionTime 检测到语音时的时间
     * @param currentActionSize 当前刺激大小
     * @param currentActionIndex 当前刺激顺序
     * @param standCircleMapSize 标准刺激大小
     * @param checkBy 根据 ORDER 还是 SIZE 进行检测
     */
    private void oneTrialProcess(Boolean isNormal, Boolean isLeft, String nanoStiHumanTime,
                                 String nanoActionTime, Integer currentActionSize, Integer currentActionIndex, Integer standCircleMapSize, String checkBy) {
        if (!isNormal) return;
        String dataCurrentLine;
        if (nanoActionTime != null) {
            dataCurrentLine = String.format("$%s$, $%s$, %s, %s, %s, %s, %s, %s, %s, %s\n",
                    nanoStiHumanTime,
                    nanoActionTime,
                    0,
                    //(Long.parseLong(nanoNoShowTIme) - Long.parseLong(nanoStiHumanTime))/1000000,
                    (Long.parseLong(nanoActionTime) - Long.parseLong(nanoStiHumanTime))/1000000,
                    standCircleMapSize,
                    currentActionSize,
                    currentActionSize > standCircleMapSize ? "1" : "0",
                    currentActionIndex,
                    isLeft ? "1" : "0",
                    checkBy
            );
        } else {
            //因为 DataBatch 需要对数据不齐进行伪造，因此这里故意返回数据不齐的行。(2019年03月25日已修改)
            dataCurrentLine = String.format("$%s$, $%s$, %s, %s, %s, %s, %s, %s, %s, %s\n",
                    nanoStiHumanTime,
                    0,
                    0,
                    0,
                    standCircleMapSize,
                    showCurrentActionSize,
                    showCurrentActionSize > standCircleMapSize ? "1" : "0",
                    showCurrentActionIndex,
                    isLeft ? "1" : "0",
                    "NONE"
            );
        }
        finalData.append(dataCurrentLine);
    }


    /**
     * 工具类，用于进行相对时间和绝对时间的转换
     * @param standLocalTime 标准的绝对时间
     * @param standRelativeTime 标准的高精度相对时间
     * @param localTime 需要转换的绝对时间
     * @return 绝对时间经过转换过的高精度 nano 时间
     */
    private String resolveTimeTrans(String standLocalTime, String standRelativeTime, String localTime) {
        LocalTime myLocalTime = LocalTime.parse(localTime);
        LocalTime myStandTime = LocalTime.parse(standLocalTime);
        long delta = Duration.between(myStandTime,myLocalTime).toNanos();
        return String.valueOf((Long.parseLong(standRelativeTime) + delta));
    }

    private Integer showLineCount = 0;
    private Integer actionLineCount = 0;
    private Integer trialCount = 0;

    /**
     * 用于程序 DEBUG
     * @param line 输入的行
     */
    private void debug(String line) {
        //用于检测 Trial 的分界
        if (isFixPointLine(line)) {
            //在清除前收集中间数据到 StringBuilder 中，如果有的话(因为对于第一个 FixPoint，必定是无法收集的，必须从第二个开始收集)
            if (showStiHumanTime != null) {
                oneTrialProcess(
                        isNormal,
                        isLeft,
                        resolveTimeTrans(standLocalTime, standRelativeTime, showStiHumanTime),
                        nanoActionTime,
                        currentActionSize,
                        currentActionIndex,
                        standCircleMapSize,
                        checkBy); //生成副作用 finalData
                trialCount += 1;
            }
            //用于清空展示行和反应行的中间数据
            clearAllTempData();
        }
        //收集刺激呈现的行，均有
        if (isShowStiLine(line)) {
            parseShowStiData(line);
            showLineCount += 1;
        }
        //收集反应呈现的行，可能没有
        if (isActionLine(line)) {
            parseActionData(line);
            actionLineCount += 1;
        }
    }

    /**
     * 清空所有的中间数据
     */
    private void clearAllTempData() {
        isNormal = null;
        isLeft = null;
        showStiHumanTime = null;

        humanActionTime = null;
        nanoActionTime = null;
        currentActionSize = null;
        currentActionIndex = null;
        checkBy = null;
    }

    public void parseAllData() {
        List<String> dataLines = dataReader.lines().filter(line -> !line.isEmpty()).map(String::trim).collect(Collectors.toList());
        for (String line : dataLines) {
            if (isMetaDataLine(line)) parseMetaData(line);
            //debug(line);
            //用于检测 Trial 的分界
            if (isFixPointLine(line)) {
                //在清除前收集中间数据到 StringBuilder 中，如果有的话(因为对于第一个 FixPoint，必定是无法收集的，必须从第二个开始收集)
                if (showStiHumanTime != null) {
                    oneTrialProcess(
                            isNormal,
                            isLeft,
                            resolveTimeTrans(standLocalTime, standRelativeTime, showStiHumanTime),
                            nanoActionTime,
                            currentActionSize,
                            currentActionIndex,
                            standCircleMapSize,
                            checkBy); //生成副作用 finalData
                    trialCount += 1;
                }
                //用于清空展示行和反应行的中间数据
                clearAllTempData();
            }
            //收集刺激呈现的行，均有
            if (isShowStiLine(line)) {
                parseShowStiData(line);
                showLineCount += 1;
            }
            //收集反应呈现的行，可能没有
            if (isActionLine(line)) {
                parseActionData(line);
                actionLineCount += 1;
            }
        }
        //保留副作用 finalData
    }

    private void saveExperimentDataAsCSV(String finalData) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(id + "_" + name + ".csv");
        writer.print(finalData);
        writer.close();
    }

    /**
     * 外部调用接口
     * @throws IOException 处理异常
     */
    public void doAction() throws IOException {
        logger.info("[MAIN] Parse Data now...");
        parseAllData();
        logger.info("[MAIN] Save File now...");
        if (true) {
            //用来生成 id 列
            StringBuilder builder = new StringBuilder();
            if (id != null && id.contains("-")) {
                String[] split = id.split("-");
                id = split[split.length - 1].trim();
            } else {
                if (id == null) id = "noId";
                if (name == null) name = "noName";
            }
            //用来去除练习行
            List<String> collect = Arrays.stream(finalData.toString().split("\n"))
                    .filter(line -> !line.isEmpty()).map(String::trim).map(line2 -> {
                        if (line2.contains("SHOW_TIME")) return line2;
                        else return id + ", " + line2;
                    }).collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                builder.append(collect.get(i));
                if (i != collect.size() - 1) {
                    builder.append("\n");
                }
            }
            saveExperimentDataAsCSV(builder.toString());
        } else {
            saveExperimentDataAsCSV(finalData.toString());
        }
    }

    @Test public void testParseAllData() {
        parseAllData();
        System.out.println("showLineCount = " + showLineCount);
        System.out.println("actionLineCount = " + actionLineCount);
        System.out.println("trialCount = " + trialCount);
    }

    @Test public void testParseShowStiData() {
        final String data = "15:01:05.356 [JavaFX Application Thread] INFO  c.m.wsh.screen.real.ShowScreen - Showing sti now... " +
                "current show is normal? false::: current show is left? RIGHT";
        Boolean showStiLine = isShowStiLine(data);
        assert showStiLine;
        parseShowStiData(data);
        System.out.println("isNormal = " + isNormal);
        System.out.println("isLeft = " + isLeft);
        System.out.println("showStiLine = " + showStiHumanTime);
    }

    @Test public void testParseActionData() {
        final String data = "15:01:07.627 [Thread-6] INFO  c.m.wsh.screen.real.ShowScreen - Get result and go now... - :::3:::20:::11528122620447";
        //todo parse order info
        assert isActionLine(data);
        parseActionData(data);
        System.out.println("humanActionTime = " + humanActionTime);
        System.out.println("nanoActionTime = " + nanoActionTime);
        System.out.println("currentActionSize = " + currentActionSize);
    }

    @Test public void testParseMetaData() {
        final String data = "14:59:40.324 [JavaFX Application Thread] INFO  c.m.wsh.screen.learn.InfoScreen - METADATA::1-3::田佳音::女::";
        Boolean metaDataLine = isMetaDataLine(data);
        System.out.println("metaDataLine = " + metaDataLine);
        parseMetaData(data);
        System.out.println("id = " + id);
        System.out.println("name = " + name);
        System.out.println("gender = " + gender);
    }

    @Test public void parseAllDataTest() {
        parseAllData();
        System.out.println("finalData = " + finalData.toString());
        int length = finalData.toString().split("\n").length;
        System.out.println("length = " + length);
    }

    @Test public void testAll() throws IOException {
        doAction();
    }
}
