package com.izis.yzext.base;

public class RxEvent {
    /**
     * 模拟点击
     */
    public static final int click = 1;

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
