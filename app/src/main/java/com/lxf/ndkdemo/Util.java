package com.lxf.ndkdemo;

import com.lxf.ndkdemo.pl2303.ChessChange;
import com.lxf.ndkdemo.pl2303.LiveType;
import com.lxf.ndkdemo.pl2303.LogToFile;

import java.util.ArrayList;
import java.util.List;

import lxf.widget.tileview.Board;

/**
 * 拍照和前一帧数据比对
 * Created by lxf on 18-5-29.
 */
public class Util {

    public static LiveType parse(String pre,String curr){
       return parse(pre, curr, 0);
    }

    public static LiveType parse(String pre, String curr, int rotate){
        LiveType liveType = new LiveType();

        if (pre == null){
            StringBuilder preBuilder = new StringBuilder();
            for (int i = 0; i<Board.n * Board.n; i++){
                preBuilder.append("0");
            }
            pre = preBuilder.toString();
        }

        List<ChessChange> changeList = getDifferent(curr, pre, rotate);
        //数据异常
        if (changeList == null){
            liveType.setType(LiveType.DO_NOTHING);
            return liveType;
        }

        //只有一个变化点
        if (changeList.size() == 1){
            ChessChange change = changeList.get(0);
            if (change.getNowColor() == 1 && change.getPreColor() == 0) {//多黑子
                liveType.setType(LiveType.NORMAL);
                liveType.setIndex(change.getI() + 1);
                liveType.setAllStep("+" + change.getStep());
                return liveType; // 返回该棋步
            }

            if (change.getNowColor() == 2 && change.getPreColor() == 0){//多白子
                liveType.setType(LiveType.NORMAL);
                liveType.setIndex(change.getI() + 1);
                liveType.setAllStep("-" + change.getStep());
                return liveType; // 返回该棋步
            }
        } else if (changeList.size() > 1){
            //多个变化点，如果一个新增，其他都是消失，也认为正常（不用判断消失的是否是死子，软件会自己做好限制）
            int numAdd = 0;
            for (ChessChange change:changeList){
                if (change.getPreColor() == 0){
                    numAdd++;
                    liveType.setType(LiveType.NORMAL);
                    liveType.setIndex(change.getI() + 1);
                    if (change.getNowColor() == 1)
                        liveType.setAllStep("+" + change.getStep());
                    if (change.getNowColor() == 2)
                        liveType.setAllStep("-" + change.getStep());
                }
            }
            if (numAdd == 1){
                return liveType;
            }
        }

        //暂时只考虑多一颗黑子或白子的正常情况，其他情况均视为异常
        liveType.setType(LiveType.DO_NOTHING);
        return liveType;
    }

    private static List<ChessChange> getDifferent(String curr, String pre, int rotate) {
        List<ChessChange> changeList = new ArrayList<>();

        if (curr.length() != Board.n * Board.n) {//数据发生异常
            LogToFile.d("========数据异常，接收到的数据长度不为361========= ", "curr:" + curr);
            return null;
        }

        char curChar;
        char preChar;

        int xx;
        int yy;
        for (int i = 1; i <= curr.length(); i++) {
            curChar = curr.charAt(i - 1);
            preChar = pre.charAt(i - 1);

            if (!isNum(String.valueOf(curChar))
                    || !isNum(String.valueOf(preChar))) {
                //  System.out.println("========错误指令（盘面包含非数字）========= " + curr);
                LogToFile.d("========错误指令（盘面包含非数字）========= ", "curr:" + curr);
                return null;
            }

            if (curChar != preChar) {
                System.out.println("不同点：" + i + "====" + preChar + curChar);
                String[] b = CoordinateTransition(i, rotate);
                xx = Integer.parseInt(b[0]);
                yy = Integer.parseInt(b[1]);

                ChessChange change = new ChessChange();

                change.setI(i - 1);
                change.setX(xx);
                change.setY(yy);
                // change.setPreColor(board.getValue(xx, yy));
                // 或者
                change.setPreColor(Integer.parseInt(String.valueOf(preChar)));
                change.setNowColor(Integer.parseInt(String.valueOf(curChar)));
                change.setStep(b[2]);
                changeList.add(change);

            }
        }
        return changeList;
    }

    // 一维坐标转化为二维坐标， 例如： i=1 对应 （1,1）,i=2 对于（2,1）
    private static String[] CoordinateTransition(int i, int rotate) {
        String[] arr = {"", "", ""};

        int xx = 0;
        int yy = 0;

        // ===【坐标转化】===
        // i=361-i+1;

        // i=1 对应 （1,1）,i=2 对于（2,1）
        switch (rotate) {
            case 0:
                if (i % Board.n == 0) {
                    yy = (i / Board.n);
                    xx = i - Board.n * (yy - 1);
                } else {
                    xx = i % Board.n;
                    yy = (i - xx) / Board.n + 1;
                }
                break;
            case 90:
                if (i % Board.n == 0) {
                    yy = 1;
                    xx = i / Board.n;
                } else {
                    xx = i / Board.n + 1;
                    yy = Board.n - i % Board.n + 1;
                }
                break;
            case 180:
                if (i % Board.n == 0) {
                    yy = Board.n - (i / Board.n) + 1;
                    xx = 1;
                } else {
                    xx = Board.n - i % Board.n + 1;
                    yy = Board.n - i / Board.n;
                }
                break;
            case 270:
                if (i % Board.n == 0) {
                    xx = Board.n - i / Board.n + 1;
                    yy = Board.n;
                } else {
                    xx = Board.n - i / Board.n;
                    yy = i % Board.n;
                }
                break;

        }


        String Y = String.valueOf(yy);
        String X = String.valueOf(xx);

        String theCoordinateXY = "";

        if (xx < 10 && yy < 10) {
            theCoordinateXY = "0" + Y + "0" + X;
        } else if (xx >= 10 && yy < 10) {
            theCoordinateXY = "0" + Y + X;
        } else if (xx < 10 && yy >= 10) {
            theCoordinateXY = Y + "0" + X;
        } else if (xx >= 10 && yy >= 10) {
            theCoordinateXY = Y + X;
        }

        arr[0] = String.valueOf(xx);
        arr[1] = String.valueOf(yy);
        arr[2] = theCoordinateXY;

        return arr;
    }

    private static boolean isNum(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
