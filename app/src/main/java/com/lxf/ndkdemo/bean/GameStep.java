package com.lxf.ndkdemo.bean;

public class GameStep {
    /**
     * 类型：1.上传棋步；2.悔棋
     */
    private int type;
    /**
     * 当前手顺
     */
    private int playNum;
    /**
     * 棋步
     */
    private String step;
    /**
     * 悔棋步数
     */
    private int backNum;

    public GameStep(int playNum, String step) {
        this.type = 1;
        this.playNum = playNum;
        this.step = step;
    }

    public GameStep(int backNum) {
        this.type = 2;
        this.backNum = backNum;
    }

    public GameStep() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPlayNum() {
        return playNum;
    }

    public void setPlayNum(int playNum) {
        this.playNum = playNum;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public int getBackNum() {
        return backNum;
    }

    public void setBackNum(int backNum) {
        this.backNum = backNum;
    }
}
