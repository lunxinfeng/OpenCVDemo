package com.izis.yzext.bean;

import android.support.annotation.Nullable;

public class ChessPosition {
    private String packageName;
    private String className;
    private int boardSize;
    private int left;
    private int top;
    private int size;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ChessPosition) {
            return packageName.equals(((ChessPosition) obj).packageName) && className.equals(((ChessPosition) obj).className) && boardSize == ((ChessPosition) obj).boardSize;
        }
        return super.equals(obj);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
