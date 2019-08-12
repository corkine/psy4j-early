package com.mazhangjing.zsw.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 本类用来进行 .log 的数据提取以及鼠标记录的对齐和曲下线面积的计算
 */
public class DataCollect {

    //////////////////////// 外部参数设置选项 ////////////////////////

    int computeCutArea = -1;

    boolean computeFullArea = false;

    boolean produceCoordinateCsvFile = false;

    //////////////////////// 构造器设置选项 ////////////////////////

    private static Logger logger = LoggerFactory.getLogger(DataCollect.class);

    public DataCollect(String PathString, String DataName,
                       String LogName, String standLocalTime, String standRelativeTime) {
        this.PathString = PathString;
        this.DataName = DataName;
        this.LogName = LogName;
        this.standLocalTime = standLocalTime;
        this.standRelativeTime = standRelativeTime;
        initReader();
    }

    private String PathString;
    //"/Users/corkine/工作文件夹/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example";
    private String DataName;
    //"9-1_张双伟_女.log";
    private String LogName;
    //"9-1.log";

    private String standLocalTime;
    //"14:48:41.234";
    private String standRelativeTime;
    //"74166292791463";

    /**
     * 初始化读写鼠标和日志文件，准备好两个流，此时的流尚未关闭
     */
    void initReader() {
        try {
            dataFile = Paths.get(PathString + File.separator + DataName).toFile();
            logFile = Paths.get(PathString + File.separator + LogName).toFile();
            initDataStream(dataFile,null);
            initLogStream(logFile,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Stream<String> initDataStream(File fromFile, String encode) throws IOException {
        BufferedReader dataReader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fromFile),
                        Charset.forName(encode == null ? "GBK" : encode)));
        return dataReader.lines();
    }

    Stream<String> initLogStream(File fromFile, String encode) throws IOException {
        logReader =
                new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(fromFile),
                                Charset.forName(encode == null ? "GBK" : encode)
                        )
                );
        return logReader.lines();
    }

    private File dataFile;
    File logFile;
    private BufferedReader logReader;

    ////////////////////////////// 基本信息解析 ///////////////////////////

    String id;
    String name;
    String gender;
    String otherInfo;

    Boolean isMetaDataLine(String line) {
        return line.contains("screen.InfoScreen") && line.contains("METADATA");
    }

    void parseMetaData(String line) {
        String[] data = line.split(" - ")[1].split("::");
        id = data[1];
        name = data[2];
        gender = data[3];
        if (data.length <= 5) otherInfo = data[4];
        else otherInfo = "";
    }

    ////////////////////////////// 反应信息解析 ///////////////////////////

    String clickStartBtnNow;
    String mouseMoveNow;

    String clickedNowIs;
    String clickNowNanoTime;
    Boolean congruenceOrNot;

    Boolean isBtnClickLine(String line) {
        return line.contains("Current Screen is 首次刺激呈现屏幕");
    }

    void parseBtnClickData(String line) {
        String localTime = line.trim().split(" ")[0];
        clickStartBtnNow = resolveTimeTrans(standLocalTime, standRelativeTime, localTime);
    }

    String resolveTimeTrans(String standLocalTime,
                            String standRelativeTime,
                            String localTime) {
        LocalTime myLocalTime = LocalTime.parse(localTime);
        LocalTime myStandTime = LocalTime.parse(standLocalTime);
        long delta = Duration.between(myStandTime,myLocalTime).toNanos();
        return String.valueOf((Long.parseLong(standRelativeTime) + delta));
    }

    Boolean isMouseMoveLine(String line) {
        return line.contains("Mouse Moved Detected");
    }

    void parseMouseMoveData(String line) {
        mouseMoveNow = resolveTimeTrans(standLocalTime,standRelativeTime,line.trim().split(" ")[0]);
    }

    Boolean isClickedLine(String line) {
        return line.contains("StiBackScreen")
                && line.contains("Get") && line.contains("clicked") && !line.contains("test.TestSti");
    }

    void parseClickedData(String line) {
        String[] data = line.split(" - ")[1].trim().split("::");
        if (data[0].toUpperCase().contains("RIGHT")) clickedNowIs = "RIGHT";
        else if (data[0].toUpperCase().contains("LEFT")) clickedNowIs = "LEFT";
        clickNowNanoTime = data[1];
    }

    ////////////////////////////// 结果信息解析 ///////////////////////////

    StringBuilder resultData = new StringBuilder();

    {
        resultData.append("TIME, ANSWER, ANSWER_CHECK, LEFT_NUMBER, LEFT_SIZE, RIGHT_NUMBER, RIGHT_SIZE, " +
                "NUMBER_FIRST_BIG, SIZE_FIRST_BIG ,IS_CONGRUENCE, CLICK_BTN, MOUSE_MOVE\n");
    }

    Boolean isResultLine(String line) {
        return line.contains("StiBackScreen") &&
                line.contains("LEFT:") && line.contains("RIGHT:") && !line.contains("test.TestSti");
    }

    void parseResultData(String clickedNowIs, String clickNowNanoTime,
                         String clickStartBtnNow, String mouseMoveNow, String line) {
        String[] data = line.split("\\|\\|\\|")[0].trim().split(" - ")[1].trim().split(" ");
        String answerCheck = data[0];
        String[] leftAll = data[1].split(":")[1].split(",");
        String leftNumber = leftAll[0];
        String leftSize = leftAll[1];
        String[] rightAll = data[2].split(":")[1].split(",");
        String rightNumber = rightAll[0];
        String rightSize = rightAll[1];
        //一致性判断
        if ((Integer.parseInt(leftNumber) > Integer.parseInt(rightNumber) && Integer.parseInt(leftSize) > Integer.parseInt(rightSize))
        ||(Integer.parseInt(leftNumber) < Integer.parseInt(rightNumber) && Integer.parseInt(leftSize) < Integer.parseInt(rightSize))) {
            congruenceOrNot = true;
        } else congruenceOrNot = false;
        //同步或者异步
        String result1 = null; String result2 = null;
        String cleanData = line.split("\\|\\|\\|")[1].trim().replace("Array{head=Stimulate{", "")
                .replace("}, back=Stimulate{", ":::").replace("}}", "");
        if (!cleanData.contains("999")) {
            result1 = "2"; result2 = "2";
        }
        else {
            String head = cleanData.split(":::")[0];
            String back = cleanData.split(":::")[1];
            List<String> headData = Arrays.stream(head.split(", ")).map(tuple -> tuple.trim().split("=")[1]).collect(Collectors.toList());
            List<String> backData = Arrays.stream(back.split(", ")).map(tuple -> tuple.trim().split("=")[1]).collect(Collectors.toList());
            Integer headInteger = null; Integer headSize = null; Integer backInteger = null; Integer backSize = null;
            if (headData.get(0).equals("999")) {
                headInteger = Integer.valueOf(headData.get(1));
                headSize = Integer.valueOf(headData.get(3));
                backInteger = Integer.valueOf(backData.get(0));
                backSize = Integer.valueOf(backData.get(2));
            } else {
                headInteger = Integer.valueOf(headData.get(0));
                headSize = Integer.valueOf(headData.get(2));
                backInteger = Integer.valueOf(backData.get(1));
                backSize = Integer.valueOf(backData.get(3));
            }
            result1 = headInteger > backInteger ? "1" : "0";
            result2 = headSize > backSize ? "1" : "0";
        }

        String currentLine = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                clickNowNanoTime,clickedNowIs,answerCheck,leftNumber,leftSize,rightNumber,rightSize, result1, result2,
                congruenceOrNot ? "1" : "0", clickStartBtnNow, mouseMoveNow);
        resultData.append(currentLine).append("\n");
    }

    ////////////////////////////// 所有 Data 整合的调用 ///////////////////////////

    void parseAllData() throws IOException {
        initDataStream(dataFile,null).forEach(line -> {
            if (isMetaDataLine(line)) parseMetaData(line);
            else if (isBtnClickLine(line)) parseBtnClickData(line); //可能是测试部分，但无所谓，因为在 parseResultData 之前，循环会覆盖掉测试结果
            else if (isMouseMoveLine(line)) parseMouseMoveData(line); //同上
            else if (isClickedLine(line)) parseClickedData(line); //生成了 clickedNowIs 和 clickNowNanoTime
            else if (isResultLine(line)) {
                parseResultData(clickedNowIs, clickNowNanoTime, clickStartBtnNow, mouseMoveNow, line);
                clickedNowIs = null;
                clickNowNanoTime = null;
                clickStartBtnNow = null;
                congruenceOrNot = null;
                mouseMoveNow = null; //清空这四个中间状态值
            }
        });
    }

    ////////////////////////////// 鼠标日志分析 - 对应时间点 ///////////////////////////

    List<String> cleanNewData = new ArrayList<>();

    private static long getSimilarTime(Stream<String> logStream, long standTime) {
        long DIFFERENCE = 10000000000L; //10s
        long similarTime = 0;
        Object[] logArray = logStream.filter(logLine1 -> logLine1.contains("Mouse Moved:")).toArray();
        for (Object o : logArray) {
            String currentLine = (String) o;
            String[] split = currentLine.split(" Mouse Moved: ");
            long currentTime = Long.parseLong(split[0]);
            if (Math.abs(currentTime - standTime) < DIFFERENCE) {
                similarTime = currentTime;
                DIFFERENCE = Math.abs(currentTime - standTime);
            }
        }
        return similarTime;
    }

    private static long computeAreaBetween(int x1, int y1, int x2, int y2) {
        y1 = Math.abs(y1); y2 = Math.abs(y2);
        long res = Math.abs(x2-x1) * ((y2+y1)/2);
        if (res < 0) {
            System.out.println("x, y is " + x1 + ", " + x2 + ", " + y1 + ", " + y2);
            throw new RuntimeException("不可能的面积");
        }
        return res;
    }

    //这一步只是找到不同记录时间点的对应，不计算积分面积。
    void findDataWithLog(String resultData){
        logger.info("Align Data With Log Time now... (This will take a bit long time...)");
        cleanNewData = Arrays.stream( //将数据重新拆分为行
                resultData.split("\n"))
                .filter(line1 -> !line1.isEmpty() && !line1.startsWith("TIME")) //去除第一行
                .map(String::trim)
                .map(line -> {
                    //logger.debug("Parse Similar Action Time for Line now... " + (line.length() < 15 ? line : line.substring(10)));
                    Stream<String> logStream;
                    //因为 reader 每次 lines 都会自动穷尽，因此每次重新打开文件为流
                    try {
                        logReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        logStream = initLogStream(logFile, null);
                        long actionTime = Long.parseLong(line.split(", ")[0]); //获取当前反应的时间
                        long similarActionTime = getSimilarTime(logStream, actionTime); //找到最近反应的时间
                        return line + ", " + similarActionTime; //添加到行的结尾
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                    }
                })
                .filter(newLine -> !newLine.isEmpty())
                .map(line -> {
                    //logger.debug("Parse Similar Mouse Move Time for Line now... " + (line.length() < 15 ? line : line.substring(10)));
                    Stream<String> logStream;
                    //因为 reader 每次 lines 都会自动穷尽，因此每次重新打开文件为流
                    try {
                        logReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        logStream = initLogStream(logFile, null);
                        String[] res = line.split(", ");
                        long moveTime = Long.parseLong(res[res.length - 2]); //获取鼠标移动时间
                        long similarMoveTime = getSimilarTime(logStream,moveTime);
                        return line + ", " + similarMoveTime; //添加到行的结尾
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                    }
                })
                .filter(newLine -> !newLine.isEmpty())
                .collect(Collectors.toList()); //输出为列表，因为 String 太麻烦了，直接列表
    }

    ////////////////////////////// 鼠标日志分析 - 计算面积 ///////////////////////////

    String finalData;

    /**
     * 给定起始时间和终止时间，从鼠标记录流中寻找对应的点， 统计计算出相应的面积
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param logStream 鼠标记录流
     * @param computeCutArea 计算区间面积
     * @param computeFullArea 计算总体面积
     * @return 返回面积和坐标，比如： "23223123, 10+15-22+33-33+44" 其中 + 表示 x + y，- 表示不同的坐标，即 (x+y)-(x+y)-(x+y)，一共 101 对坐标
     * @throws IOException 读写异常
     */
    private String getAreaAndCoordinateBetween(long startTime, long endTime, Stream<String> logStream,
                                               int computeCutArea, boolean computeFullArea)
            throws IOException {

        if (!computeFullArea) { //如果是根据分段计算，那么
            long part = (endTime - startTime)/3;
            assert part >= 0;
            if (computeCutArea == 1) {
                long realEndTime = startTime + part;
                endTime = getSimilarTime(initLogStream(logFile, null), realEndTime);
            } else if (computeCutArea == 2) {
                startTime = getSimilarTime(initLogStream(logFile, null), startTime + part);
                endTime = getSimilarTime(initLogStream(logFile, null), endTime - part);
            } else if (computeCutArea == 3) {
                startTime = getSimilarTime(initLogStream(logFile, null),  endTime - part);
            }
            if (startTime > endTime) throw new RuntimeException("不可能的时间差值");
        }  //如果是根据总体计算，那么 什么都不做
        //System.out.println(startTime + ", " + endTime);
        //78905092320610, 78905287010427
        //78905092320610, 78905676390061

        List<Tuple2<Integer, Integer>> coordinateList = new ArrayList<>();
        boolean inALoop = false;
        int currentX = 0; int currentY = 0; long allArea = 0;
        for (Object line : logStream.filter(logLine -> logLine.contains("Mouse Moved:")).toArray()) {
            String[] split = ((String) line).split(" Mouse Moved: ");
            long currentTime = Long.parseLong(split[0]);
            String data = split[1];
            if (currentTime == startTime) { //当找到开始时间，标记
                inALoop = true;
                String[] xy = data.trim().split(", ");
                currentX = Integer.parseInt(xy[0]);
                currentY = Integer.parseInt(xy[1]);
            }
            //节省计算资源，间隔计算前包后包
            if (inALoop) { //当在开始和结束之间，尝试累加 Area 面积
                String[] xy = data.trim().split(", ");
                int x = Integer.parseInt(xy[0]);
                int y = Integer.parseInt(xy[1]);
                allArea += computeAreaBetween(currentX, currentY, x, y);
                currentX = x; currentY = y; //将 currentXY 定义为上一次的坐标
                coordinateList.add(Tuple2.apply(x,y));
            }
            if (currentTime == endTime) { //当在结束的时候，标记
                //inALoop = false; //直接返回
                return produceCoordinateCsvFile ? allArea + ", " + splitCoordinateToString(coordinateList) : String.valueOf(allArea);
            }
        } //当无法捕获最终时间，完全遍历返回
        logger.error("不应该在计算面积的时候，遍历所有数据，可能终止时间点不能对应，请检查文件是否有误（空文件或者错误时间对应文件、极端数据可忽略此提示）");
        return produceCoordinateCsvFile ? allArea + ", " + splitCoordinateToString(coordinateList) : String.valueOf(allArea);
    }

    static String splitCoordinateToString(List<Tuple2<Integer, Integer>> coordinate) {
        StringBuilder sb = new StringBuilder();
        final int countNumber = 99;
        final int size = coordinate.size();
        final int step = size / countNumber;
        if (step == 1 || step == 2 || step == 3) logger.warn("传入的数量不足，不能够等比分成 100 分，因此，按照原样输出，结果约等于 100, " + "传入数量为：" + size);
        for (int i = 0; i < coordinate.size(); i++) {
            if (step == 0) {
                sb
                .append(coordinate.get(i)._1)
                .append("+")
                .append(coordinate.get(i)._2)
                .append("-");
            } else if (i % step == 0) {
                //是步长的整数，那么取出这些数据
                sb
                .append(coordinate.get(i)._1)
                .append("+")
                .append(coordinate.get(i)._2)
                .append("-");
            }
        }
        String result = sb.toString();
        if (result.endsWith("-")) result = result.substring(0, result.length() - 1); //取出末尾的 -
        return result;
    }

    //获取在移动和点击之间的所有数据，并且计算积分
    void findDataBetweenMoveAndClick(List<String> cleanNewData) {
        logger.info("Find Data, Compute Area Between Move And Click now...");
        finalData =
        cleanNewData.stream().map(line -> {
                //logger.debug("Parse Between Move And Click Time for Line now... " + (line.length() < 8 ? line : line.substring(5)));
                Stream<String> logStream;
                //因为 reader 每次 lines 都会自动穷尽，因此每次重新打开文件为流
                try {
                    logReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    logStream = initLogStream(logFile, null);
                    String[] split = line.split(", ");
                    long startTime = Long.parseLong(split[split.length - 1]);
                    long endTime = Long.parseLong(split[split.length - 2]);
                    //System.out.println("startTime = " + startTime);
                    //System.out.println("endTime = " + endTime);
                    String areaAndCoordinate = getAreaAndCoordinateBetween(startTime, endTime, logStream, computeCutArea, computeFullArea);
                    return line + ", " + areaAndCoordinate;
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            })
        .filter(line -> !line.isEmpty())
        .collect(Collectors.joining("\n"));
    }

    ////////////////////////////// 数据保存和总调用脚本 ///////////////////////////

    private void saveExperimentDataAsCSV(String finalData) throws FileNotFoundException {
        String info = "";
        if (computeFullArea) info = "full_area";
        else {
            if (computeCutArea == 1) info = "cut_1";
            else if (computeCutArea == 2) info = "cut_2";
            else if (computeCutArea == 3) info = "cut_3";
        }
        if (produceCoordinateCsvFile) info += "_with_coordinate";
        PrintWriter writer = new PrintWriter(id + "_" + name + "_" + info + ".csv");
        writer.print("TIME, ANSWER, ANSWER_CHECK, LEFT_NUMBER, LEFT_SIZE, RIGHT_NUMBER, RIGHT_SIZE, NUMBER_FIRST_BIG, SIZE_FIRST_BIG ,IS_CONGRUENCE, " +
                "CLICK_BTN, MOUSE_MOVE, NEAR_ACTION_TIME, NEAR_MOUSE_MOVE, AREA, COORDINATE_101\n");
        writer.print(finalData);
        writer.close();
    }

    public void doAction() throws IOException {
        logger.info("[MAIN] Parse Data now...");
        parseAllData();
        logger.info("[MAIN] Marge Log with Data now...");
        findDataWithLog(resultData.toString());
        logger.info("[MAIN] Compute Area now...");
        findDataBetweenMoveAndClick(cleanNewData);
        logger.info("[MAIN] Save File now...");
        saveExperimentDataAsCSV(finalData);
    }
}
