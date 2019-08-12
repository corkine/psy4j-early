package com.mazhangjing.wsh.stimulate;

/**
 * 定义测试阶段的结果对象
 */
public class Result {
    private String based_on;
    public Result(String based_on) {
        this.based_on = based_on;
    }
    private Integer rightCount = 0;
    private Integer totalCount = 0;
    public Result setNextRight() {
        return setNextRight(false);
    }
    public Result setNextWrong() {
        return setNextWrong(false);
    }
    public Result setNextRight(Boolean withoutCount) {
        rightCount += 1;
        if (!withoutCount) totalCount += 1;
        return this;
    }
    public Result setNextWrong(Boolean withoutCount) {
        if (!withoutCount) totalCount += 1;
        return this;
    }
    public Result justCountAdd() {
        totalCount += 1;
        return this;
    }
    public Double getRightRate() {
        if (totalCount == 0) return 0.0;
        else return ((double) rightCount) / totalCount;
    }
    public Boolean isBiggerThan(Double other) {
        return getRightRate() > other;
    }
    public Integer getRightCount() { return rightCount; }
    public Result resetAll() {
        rightCount = 0;
        totalCount = 0;
        return this;
    }
    @Override
    public String toString() {
        return "Result{" +
                "rightCount=" + rightCount +
                ", totalCount=" + totalCount +
                ", basedOn=" + based_on +
                '}';
    }
}
