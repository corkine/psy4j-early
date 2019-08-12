package com.mazhangjing.zsw.data;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class DataCollectTest {

    private static DataCollect c;

    @BeforeClass public static void before() {
        String pathString = "/Users/corkine/工作文件夹_旧/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example";
        String dataName = "1-1_毕东娇_女.log";
        String logName = "1-1.log";
        String standLocalTime = "08:02:06.280";
        String standRelativeTime = "739185589156";
        c = new DataCollect(pathString, dataName, logName, standLocalTime, standRelativeTime);
        c.computeFullArea = true;
        c.produceCoordinateCsvFile = true;
    }

    @Test
    public void testMesaDataParse() {
        String metaData = "20:36:18.418 [JavaFX Application Thread] INFO  c.mazhangjing.zsw.screen.InfoScreen - METADATA::01::01::2::0";
        if (!c.isMetaDataLine(metaData)) throw new RuntimeException();
        c.parseMetaData(metaData);
        System.out.println("id = " + c.id);
        System.out.println("name = " + c.name);
        System.out.println("gender = " + c.gender);
        System.out.println("otherInfo = " + c.otherInfo);
        assertEquals(c.id, "01");
        assertEquals(c.name, "01");
        assertEquals(c.gender, "2");
        assertEquals(c.otherInfo, "0");
    }

    @Test public void testParseOneSimpleData() {
        String clickLine = "20:37:17.880 [JavaFX Application Thread] DEBUG c.m.z.screen.StiBackScreen - Get Left clicked::1763846706551188";
        //String resultLine = "20:37:17.881 [JavaFX Application Thread] INFO  c.m.z.screen.StiBackScreen - RIGHT LEFT:5,70 RIGHT:4,90";
        String resultLine = "14:50:13.162 [JavaFX Application Thread] INFO  c.m.zsw.screen.StiBackScreen - RIGHT LEFT:8,90 RIGHT:3,70 ||| " +
                "Array{head=Stimulate{left=8, right=999, leftSize=90, rightSize=70}, back=Stimulate{left=999, right=3, leftSize=70, rightSize=70}}";
        String moveLine = "20:37:02.737 [JavaFX Application Thread] DEBUG c.m.z.screen.test.TestStiBackScreen - Mouse Moved Detected.";
        String btnClick = "20:36:59.873 [pool-2-thread-1] DEBUG com.mazhangjing.zsw.Main - Current Screen is 首次刺激呈现屏幕";
        if (!c.isBtnClickLine(btnClick) || !c.isMouseMoveLine(moveLine)) throw new RuntimeException();
        if (!c.isClickedLine(clickLine)) throw new RuntimeException("错误的 ClickLine 监测");
        if (!c.isResultLine(resultLine)) throw new RuntimeException("错误的 ResultLine 监测");
        c.parseBtnClickData(btnClick);
        System.out.println("clickStartBtnNow = " + c.clickStartBtnNow);
        assertEquals(c.clickStartBtnNow, "46032778589156");
        c.parseMouseMoveData(moveLine);
        System.out.println("mouseMoveNow = " + c.mouseMoveNow);
        assertEquals(c.mouseMoveNow, "46035642589156");
        c.parseClickedData(clickLine);
        System.out.println("clickedNowIs = " + c.clickedNowIs);
        assertEquals(c.clickedNowIs, "LEFT");
        System.out.println("clickNowNanoTime = " + c.clickNowNanoTime);
        assertEquals(c.clickNowNanoTime, "1763846706551188");
        c.parseResultData(c.clickedNowIs,c.clickNowNanoTime,c.clickStartBtnNow, c.mouseMoveNow, resultLine);
        System.out.println("currentLine = " + c.resultData.toString());
        assertTrue(c.resultData.toString().contains("TIME, ANSWER, ANSWER_CHECK, LEFT_NUMBER, LEFT_SIZE, RIGHT_NUMBER, RIGHT_SIZE, NUMBER_FIRST_BIG, SIZE_FIRST_BIG ,IS_CONGRUENCE, CLICK_BTN, MOUSE_MOVE"));
        assertTrue(c.resultData.toString().contains("1763846706551188, LEFT, RIGHT, 8, 90, 3, 70, 1, 1, 1, 46032778589156, 46035642589156"));

    }

    @Test public void testParseAllSimplesData() throws IOException {
        c.parseAllData();
        System.out.println("resultData = " + c.resultData.toString());
        assertTrue(c.resultData.toString().contains("852952869275, LEFT, RIGHT, 6, 90, 1, 70, 2, 2, 1, 851904589156, 852186589156"));
        assertTrue(c.resultData.toString().contains("1402558550848, RIGHT, RIGHT, 9, 70, 4, 90, 1, 0, 0, 1401102589156, 1401495589156"));
        assertTrue(c.resultData.toString().contains("2145467342977, LEFT, RIGHT, 3, 90, 1, 70, 0, 0, 1, 2144387589156, 2144491589156"));
    }

    @Test public void testLogStream() throws IOException {
        Stream<String> logStream = c.initLogStream(c.logFile, null);
        //logStream.forEach(System.out::println);
        assertNotNull(logStream);
    }

    @Test public void findDataWithLogTest() throws IOException {
        c.parseAllData();
        c.findDataWithLog(c.resultData.toString());
        System.out.println("cleanNewData = " + c.cleanNewData);
    }

    @Test public void findDataBetweenMoveAndClickTest() throws IOException {
        c.parseAllData();
        c.findDataWithLog(c.resultData.toString());
        c.findDataBetweenMoveAndClick(c.cleanNewData);
        System.out.println("finalData = " + c.finalData);
    }

    @Ignore
    @Test public void doAction1() throws IOException {
        String PathString = "/Users/corkine/工作文件夹/cmPsyLab/Lab/Lab/src/main/resources/zsw_data_example";
        String DataName = "9-3_张双伟_女.log";
        String LogName = "9-3.log";

        String standLocalTime = "16:05:57.082";
        String standRelativeTime = "78802263075361";
        DataCollect dataCollect = new DataCollect(PathString, DataName, LogName, standLocalTime, standRelativeTime);
        dataCollect.computeCutArea = 1;
        dataCollect.computeFullArea = false;
        dataCollect.doAction();
    }

    //@Ignore
    @Test public void doAction2() throws IOException {
        long from = System.currentTimeMillis();
        c.doAction();
        long end = System.currentTimeMillis();
        assertTrue(end - from > 1000 * 30);
    }

    @Test public void testSplitCoordinateToString() {
        ArrayList<Tuple2<Integer, Integer>> tuple2s = new ArrayList<>();
        tuple2s.add(Tuple2.apply(10,20));
        tuple2s.add(Tuple2.apply(30,40));
        Random random = new Random();
        for (int i = 0 ; i < 100; i++) {
            tuple2s.add(Tuple2.apply(random.nextInt(1000), random.nextInt(1000)));
        }
        System.out.println("For Array Length: " + tuple2s.size());
        String s = DataCollect.splitCoordinateToString(tuple2s);
        System.out.println("Result is \n" + s);
        assertTrue(!s.isEmpty());
        assertTrue(!s.endsWith("-"));
        assertTrue(!s.startsWith("-"));
        assertTrue(s.contains("+") && s.contains("-"));
        assertEquals(102, s.split("-").length);
    }

    @Test public void testSplitCoordinateToString2() {
        ArrayList<Tuple2<Integer, Integer>> tuple2s = new ArrayList<>();
        Random random = new Random();
        for (int i = 0 ; i < 1300; i++) {
            tuple2s.add(Tuple2.apply(random.nextInt(1000), random.nextInt(1000)));
        }
        System.out.println("For Array Length: " + tuple2s.size());
        String s = DataCollect.splitCoordinateToString(tuple2s);
        System.out.println(tuple2s);
        System.out.println("Result is \n" + s);
        assertTrue(!s.isEmpty());
        assertTrue(!s.endsWith("-"));
        assertTrue(!s.startsWith("-"));
        assertTrue(s.contains("+") && s.contains("-"));
        assertEquals(100, s.split("-").length);
    }

    @Test public void testSplitCoordinateToString3() {
        ArrayList<Tuple2<Integer, Integer>> tuple2s = new ArrayList<>();
        System.out.println("For Array Length: " + tuple2s.size());
        String s = DataCollect.splitCoordinateToString(tuple2s);
        System.out.println("Result is \n" + s);
        assertTrue(s.isEmpty());
    }

    @Test public void test() {
    }

}
