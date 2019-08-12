package com.mazhangjing.zsw.stimulate;

import java.util.Objects;

/**
 * 定义一个 Trial 展示的数据结构
 */
public class Array {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Array array = (Array) o;
        return Objects.equals(head, array.head) &&
                Objects.equals(back, array.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, back);
    }

    private Stimulate head;
    private Stimulate back;

    public Array(Stimulate head, Stimulate back) {
        this.head = head;
        this.back = back;
    }

    public static Array of(Stimulate head, Stimulate back) {
        return new Array(head,back);
    }

    public Stimulate getHead() {
        return head;
    }

    public void setHead(Stimulate head) {
        this.head = head;
    }

    public Stimulate getBack() {
        return back;
    }

    public void setBack(Stimulate back) {
        this.back = back;
    }

    @Override
    public String toString() {
        return "Array{" +
                "head=" + head +
                ", back=" + back +
                "}";
    }

    public static void main(String[] args) {
        Stimulate s = Stimulate.of(10, 9);
        s.setLeftSize(90);
        s.setRightSize(70);

        Stimulate s2 = Stimulate.of(10, 9);
        s.setLeftSize(90);
        s.setRightSize(70);

        Array array = new Array(s, s2);
        System.out.println("array = " + array);
    }
}