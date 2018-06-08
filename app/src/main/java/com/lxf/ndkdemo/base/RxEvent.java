package com.lxf.ndkdemo.base;

public class RxEvent {
    /**
     * 题库二级目录
     */
    public static final int exercise_2 = 1;
    /**
     * 作业二级目录
     */
    public static final int homework_2 = 2;
    /**
     * 作业三级目录
     */
    public static final int homework_3 = 3;

    private int code;
    private Object object;
    public RxEvent(int code, Object object){
        this.code=code;
        this.object=object;
    }
    public RxEvent(){}

    public int getCode() {
        return code;
    }

    public Object getObject() {
        return object;
    }
}
