package com.izis.yzext;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 棋局相关信息
 * Created by lxf on 18-6-20.
 */
public class GameInfo implements Parcelable {
    /**
     * 棋盘路数
     */
    private int boardSize = 19;
    /**
     * 棋局类型：1.从头开始  2.中途进入
     */
    private int type = 1;
    /**
     * 执子颜色：1.黑  2.白
     */
    private int bw = 1;
    /**
     * 下一步落子颜色：1.黑  2.白
     */
    private int nextBW = 1;

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBw() {
        return bw;
    }

    public void setBw(int bw) {
        this.bw = bw;
    }

    public int getNextBW() {
        return nextBW;
    }

    public void setNextBW(int nextBW) {
        this.nextBW = nextBW;
    }

    public GameInfo() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.boardSize);
        dest.writeInt(this.type);
        dest.writeInt(this.bw);
        dest.writeInt(this.nextBW);
    }

    protected GameInfo(Parcel in) {
        this.boardSize = in.readInt();
        this.type = in.readInt();
        this.bw = in.readInt();
        this.nextBW = in.readInt();
    }

    public static final Creator<GameInfo> CREATOR = new Creator<GameInfo>() {
        @Override
        public GameInfo createFromParcel(Parcel source) {
            return new GameInfo(source);
        }

        @Override
        public GameInfo[] newArray(int size) {
            return new GameInfo[size];
        }
    };
}
