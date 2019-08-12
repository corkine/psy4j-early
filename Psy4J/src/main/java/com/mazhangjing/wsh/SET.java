package com.mazhangjing.wsh;

/**
 * 定义了 wsh 包所需要使用的枚举参数
 */
public enum SET {

    PIXEL_90_SIZE("90*90大小的尺寸",10),
    PIXEL_180_SIZE("127*127大小的尺寸",14),
    PIXEL_270_SIZE("180*180大小的尺寸",20),
    PIXEL_360_SIZE("255*255大小的尺寸",28),
    PIXEL_450_SIZE("360*360大小的尺寸",40),

    INFO_SET_MS("填写被试信息超时",100000),
    TOTAL_INTRO_MS("总体指导语超时",2000),
    INTRO_START_MS("指导语自动超时",100000),
    INTRO_FONT_SIZE("指导语字体大小",38),
    SHOW_STI_MS("刺激学习阶段展示时长",3000),
    TIP_MS("指导语提示展示时长",2000),
    EXP_LEARN_FIX_MS("学习阶段注视点呈现时间",500),
    EXP_LEARN_BLANK_MS("学习阶段空白屏幕呈现时间",500),

    EXP_RELAX_MS("学习检测阶段休息时间",60000),

    SHOW_TEST_MS("测试阶段自动超时",3000),
    SHOW_TEST_SPACING("测试阶段刺激间距",230),
    FEEDBACK_SIZE("反馈大小",50),
    FEEDBACK_ANS_MS("反馈时长",1000),
    SCORE_SHOW_MS("正确率时长",1000000),
    SCORE_FONT_SIZE("正确率字体大小",40),

    EXP_INTRO_MS("实验指导语展示超时",100000),
    EXP_BLANK_MS("实验开始展示空屏幕时长",1000),
    EXP_FIX_MS("实验开始展示注视点时长",500),
    EXP_FIX_SIZE("实验开始展示注视点的大小",50),
    EXP_BYE_SIZE("结束界面字体大小",38),
    EXP_STI_ALL_MS("刺激展示时间",2000),

    EXP_PICTURE_WIDTH("图片宽度",1500),
    EXP_STAND_STI_SHOW_MS("标准刺激展示时长",3000),
    EXP_WITH_STAND("是否需要标准刺激",0),
    PRE_EXP_SIZE("预实验刺激大小",100),
    PRE_EXP_SPACING("预实验刺激间距",230),

    EXP_CORRECT_RATE("正确率",94),

    FULL_SCREEN("全屏显示",1),
    //注意，如果是预实验，那么实验编号应为 2
    //IS_PRE_EXP("是否是预实验",0), //--is_pre_exp=0
    //EXP_NUMBER("实验编号 1，2，3，4",4), //--exp_number=1/2/3/4
    ;


    private String description;
    private int value;

    SET(String description, int value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() { return description; }
    public int getValue() { return value; }
}
