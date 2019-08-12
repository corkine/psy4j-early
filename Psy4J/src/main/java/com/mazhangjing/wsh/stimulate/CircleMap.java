package com.mazhangjing.wsh.stimulate;

import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Objects;

/**
 * 定义一种刺激类型
 */
public class CircleMap {
    private List<Circle> circles;
    private Integer radius;

    public Integer getRadius() {
        return radius;
    }

    public void setRadius(Integer radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        StringBuilder map = new StringBuilder();
        circles.forEach(circle -> {
            boolean userData = (boolean) circle.getUserData();
            map.append(userData ? 1 : 0);
        });
        return "CircleMap{" +
                "map=" + map.toString() + ", " +
                "radius=" + radius +
                '}';
    }

    public List<Circle> getCircles() {
        return circles;
    }

    public void setCircles(List<Circle> circles) {
        this.circles = circles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CircleMap circleMap = (CircleMap) o;
        return Objects.equals(circles, circleMap.circles) &&
                Objects.equals(radius, circleMap.radius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(circles, radius);
    }
}
