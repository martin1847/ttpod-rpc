package com.ttpod.search.bean;

/**
 * TODO Comment here.
 * date: 14-8-28 11:02
 *
 * @author: yangyang.cong@ttpod.com
 */
public class Pojo {

    String data;
    int size;
    public Pojo(){}
    public Pojo(String data, int size) {
        this.data = data;
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Pojo{" +
                "data='" + data + '\'' +
                ", size=" + size +
                '}';
    }
}
