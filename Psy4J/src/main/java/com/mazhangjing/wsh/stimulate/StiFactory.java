package com.mazhangjing.wsh.stimulate;

import com.mazhangjing.wsh.SET;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 定义刺激的生成工厂，所有 Psy4J 需要呈现的刺激均在此处混合和平衡，然后注入 GUI 界面。
 */
public class StiFactory {

    private static Logger logger = LoggerFactory.getLogger(StiFactory.class);

    public static List<Boolean> seed;

    private static Boolean needBigChoose = new Random().nextBoolean();

    public static Boolean getNeedBigChoose() {
        return needBigChoose;
    }

    public static Boolean getNeedFirstChoose() {
        return needBigChoose;
    }

    /**
     * 获取从刺激呈现中心点到屏幕中央的举例
     * @return 像素数
     */
    private static Integer getStandSpacing() {
        Integer spacingToEdge = SET.SHOW_TEST_SPACING.getValue();
        Integer standHalfWidth = SET.PIXEL_270_SIZE.getValue() * 5;
        return spacingToEdge + standHalfWidth;
    }

    /**
     * 获取各种条件下的动态边距值
     * @param circleRadius 呈现的圆的半径
     * @return 边距值
     */
    public static Integer setSpacing(Integer circleRadius) {
        /*Integer realHalfWidth = circleRadius * 5;
        Integer standHalfWidth = SET.PIXEL_270_SIZE.getValue() * 5;
        int diff = realHalfWidth - standHalfWidth;
        return getStandSpacing() - diff;
        */
        return SET.SHOW_TEST_SPACING.getValue();
    }

    static {
        seed = new ArrayList<>();
        //为每个被试生成一组 5 幅图案
        for (int j = 0; j < 5; j++) {
            //生成每张图案的 25 个圆
            List<Boolean> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) list.add(true);
            for (int i = 0; i < 20; i++) list.add(false);
            Collections.shuffle(list);
            seed.addAll(list);
        }
    }

    private static Circle initCircle(int radius, boolean isBlack) {
        Circle circle = new Circle();
        circle.setRadius(radius);
        if (isBlack) circle.setFill(Color.BLACK);
        else circle.setFill(Color.WHITE);
        circle.setUserData(isBlack);
        return circle;
    }

    private static CircleMap initCircleMap(int radius, List<Boolean> isBlackFor25Circles) {
        List<Circle> list = isBlackFor25Circles.stream()
                .map(value -> initCircle(radius, value)).collect(Collectors.toList());
        CircleMap map = new CircleMap();
        map.setCircles(list);
        map.setRadius(radius);
        return map;
    }

    /**
     * 基本方法，用来根据球的大小、球的黑白来生成若干个 CircleMap（每张 Map 包含 25 个球）
     * @param radiusOf5Maps 球的半径
     * @param isBlackFor125Circles 球的颜色（是否为黑色）
     * @return 入参列表长度/25 个 CircleMap
     */
    private static List<CircleMap> initPersonCircleMaps(List<Integer> radiusOf5Maps, List<Boolean> isBlackFor125Circles, Boolean randomShapePosition) {
        List<CircleMap> maps = new ArrayList<>();
        maps.add(initCircleMap(radiusOf5Maps.get(0),isBlackFor125Circles.subList(0,25)));
        maps.add(initCircleMap(radiusOf5Maps.get(1),isBlackFor125Circles.subList(25,50)));
        maps.add(initCircleMap(radiusOf5Maps.get(2),isBlackFor125Circles.subList(50,75)));
        maps.add(initCircleMap(radiusOf5Maps.get(3),isBlackFor125Circles.subList(75,100)));
        maps.add(initCircleMap(radiusOf5Maps.get(4),isBlackFor125Circles.subList(100,125)));
        //形状的位置打乱
        if (randomShapePosition) {
            Collections.shuffle(maps);
            Collections.shuffle(maps);
            Collections.shuffle(maps);
        }
        return maps;
    }

    /**
     * 生成五张标准的 CircleMaps，尺寸均为 270，其中样式由 isBlackFor125Circles 定义，可选是否图案顺序随机。
     * @param isBlackFor125Circles 定义球的图案样式，一般传入值为 seed
     * @return 5 张标准大小的，可能随机或者不随机图案顺序的 CircleMap
     */
    private static List<CircleMap> initPersonStandCircleMaps(List<Boolean> isBlackFor125Circles, Boolean randomShapePosition) {
        List<Integer> list = Arrays.asList(
                SET.PIXEL_270_SIZE.getValue(), SET.PIXEL_270_SIZE.getValue(), SET.PIXEL_270_SIZE.getValue(),
                SET.PIXEL_270_SIZE.getValue(), SET.PIXEL_270_SIZE.getValue());
        return initPersonCircleMaps(list, isBlackFor125Circles,randomShapePosition);
    }

    /**
     * 语音测试阶段 - 实验 2、3、4 需要 Order 信息
     * @return 五张标准大小的、按顺序呈现的 CircleMap
     */
    public static List<CircleMap> initPersonStandCircleMaps() {
        return initPersonStandCircleMaps(seed,false);
    }

    /**
     * 生成五张不同尺寸大小的、不同形状的、随机排布的 CircleMap
     * @param isBlackFor125Circles Seed
     * @return 5 张 CircleMap
     */
    private static List<CircleMap> initPersonDifferentSizeCircleMaps
        (List<Boolean> isBlackFor125Circles, Boolean randomShapePosition, Boolean randomSizePosition, Boolean reverseSize) {
        List<Integer> list = Arrays.asList(
                SET.PIXEL_90_SIZE.getValue(), SET.PIXEL_180_SIZE.getValue(), SET.PIXEL_270_SIZE.getValue(),
                SET.PIXEL_360_SIZE.getValue(), SET.PIXEL_450_SIZE.getValue());
        if (randomSizePosition) Collections.shuffle(list);
        if (reverseSize) Collections.reverse(list);
        //批量生成 5 张点，因为 isBlackFor125Circles 是固定的，因此，每次调用此方法生成的 Shape 也是一一对应的（每次调用的第n个的形状都等同于任意一次调用的第n个的形状）
        //这意味着，对于试验 2，seed 中的 125 个布尔值，按照 25 分隔开，每 25 个决定了一个形状。
        return initPersonCircleMaps(list,isBlackFor125Circles,randomShapePosition);
    }

    /**
     * 测试阶段 - 标准答案
     * 如果是实验1，则尺寸是判断标准，因此不能随机，如果是实验2，则位置是判断标准，因此不能随机
     * @param expNumber 实验编号 只能选择 1 2 3 4
     * @return 5 张作为答案的 CircleMap
     */
    public static List<CircleMap> initPersonDifferentSizeCircleMapsForTest(Integer expNumber) {
        if (expNumber == 1)
            return initPersonDifferentSizeCircleMaps(seed, true, false, false);
        else if (expNumber == 2)
            return initPersonDifferentSizeCircleMaps(seed, false, true, false);
        else if (expNumber == 3) {
            return initPersonDifferentSizeCircleMaps(seed, false, false, false);
        } else if (expNumber == 4) {
            return initPersonDifferentSizeCircleMaps(seed, false, false, true);
        }
        else return null;
    }

    public static List<List<CircleMap>> initPersonDifferentSizeCircleMapsFor5Repeat() {
        List<List<CircleMap>> maps = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            maps.add(initPersonDifferentSizeCircleMaps(seed, true, false, false));
        }
        return maps;
    }

    /**
     * 学习阶段 - 数量
     * 根据 initPersonDifferentSizeCircleMaps 生成 8 次顺序的重复
     * @return 40 张 CircleMap（8 重复* 5 图案（随机顺序））
     */
    public static List<List<CircleMap>> initPersonDifferentSizeCircleMapsFor8Repeat() {
        List<List<CircleMap>> maps = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            maps.add(initPersonDifferentSizeCircleMaps(seed, true, false,false));
        }
        return maps;
    }

    /**
     * 学习阶段 - 位置
     * 根据 initPersonDifferentSizeCircleMaps 生成 8 次顺序的重复，区别在于，每次重复中，每个位置（1，2，3，4，5）的形状相同，大小不同
     * @return 40 张 CircleMap（8 重复* 5 图案（位置固定，大小随机））
     */
    public static List<List<CircleMap>> initPersonDifferentSizeCircleMapsFor8RepeatForExp2() {
        List<List<CircleMap>> maps = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            maps.add(initPersonDifferentSizeCircleMaps(seed, false, true, false));
        }
        //maps.forEach(System.out::println);
        return maps;
    }

    /**
     * 学习阶段 - 数量和位置
     * 生成 8 次重复的固定图案和大小展示的 CircleMap
     * @return 40 张 CircleMap
     */
    public static List<List<CircleMap>> initPersonDifferentSizeCircleMapsFor8RepeatForExp34(boolean reverse) {
        List<List<CircleMap>> maps = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            maps.add(initPersonDifferentSizeCircleMaps(seed, false, false, reverse));
        }
        //maps.forEach(System.out::println);
        return maps;
    }

    @Test public void test() {
        List<List<CircleMap>> lists = initPersonDifferentSizeCircleMapsFor8RepeatForExp34(true);
        lists.forEach(System.out::println);
    }


    /**
     * 测试阶段 - 标准刺激生成
     * @return 20 对 CircleMap 内部的 List 是 Pair，size 为 2，外部的 List 是 List，size 为 20
     */
    public static List<List<CircleMap>> initPersonStandCircleMapsFor20Pair() {
        List<List<CircleMap>> result = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<CircleMap> maps = initPersonStandCircleMaps(seed, true);
            //在上一步已经打乱了 ShapePosition 图案呈现的顺序 Collections.shuffle(maps);
            List<CircleMap> pairNow = maps.subList(0, 2);
            result.add(pairNow);
        }
        return result;
    }


    //用于语音报告测试
    public static List<CircleMap> initPersonDifferentSizeCircleMapsExcept270() {
        List<CircleMap> fiveMaps = initPersonDifferentSizeCircleMaps(seed, true, false, false);
        CircleMap needRemove = null;
        for (CircleMap circleMap : fiveMaps) {
            if (circleMap.getRadius().equals(SET.PIXEL_270_SIZE.getValue())) needRemove = circleMap;
        }
        fiveMaps.remove(needRemove);
        return fiveMaps;
    }

    public static CircleMap initPersonDifferentSizeCircleMapOnly270() {
        List<CircleMap> fiveMaps = initPersonDifferentSizeCircleMaps(seed, true, false, false);
        return fiveMaps.stream().filter(map -> map.getRadius().equals(SET.PIXEL_270_SIZE.getValue())).collect(Collectors.toList()).get(0);
    }

    public static void main(String[] args) {
        initPersonStandCircleMapsFor20Pair();
    }

    public static void saveSeedAsFile(String fileName) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
        seed.forEach(writer::println);
        writer.close();
    }

    public static void loadSeedForStaticUsage(String fileName) throws FileNotFoundException {
        logger.info("Load Seed For Static Usage done!");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        seed = reader.lines()
                .filter(line -> !line.isEmpty())
                .map(line2 -> line2.toUpperCase().equals("TRUE"))
                .collect(Collectors.toList());
    }

    @Test public void testSaveSeed() throws FileNotFoundException {
        saveSeedAsFile("seed.log");
        loadSeedForStaticUsage("seed.log");
    }

    @Test public void test2() {
        initPersonDifferentSizeCircleMapsFor8RepeatForExp2();
    }
}
