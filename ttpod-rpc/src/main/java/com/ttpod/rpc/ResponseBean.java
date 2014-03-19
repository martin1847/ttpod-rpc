package com.ttpod.rpc;

/**
 * date: 14-2-7 上午11:55
 *
 * @author: yangyang.cong@ttpod.com
 */
public class ResponseBean<Data> {

    short _req_id;

    int code;
    int rows;
    int pages;

    Data data;

    public ResponseBean(){}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseBean{" +
                "code=" + code +
                ", rows=" + rows +
                ", pages=" + pages +
                ", data=" + data +
                ", _req_id=" + _req_id +
                '}';
    }


    public ResponseBean(int code){this.code=code;}

    /**
     * used for Exception .
     */
    public static final int ERROR = -1;


    public boolean success() {
        return code != ERROR;
    }

    public boolean empty() {
        return rows == 0 || null == data;
    }


    public static <Data>ResponseBean<Data> error(){
        return new ResponseBean<>(ERROR);
    }

    public static <Data>ResponseBean<Data> code0(){
        return new ResponseBean<>(0);
    }
    public static <Data> ResponseBean<Data> code1(){
        return new ResponseBean<>(1);
    }
}
