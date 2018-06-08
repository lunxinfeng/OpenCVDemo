package lxf.widget.tileview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 读取Sgf文件
 */
public class SgfHelper {
    private static final int ACode = 'a';

    public static List<Coordinate> getCoordListformStr(String str)
            throws IOException {
        List<Coordinate> cs = new ArrayList<Coordinate>();
        if (str == "")
            return cs;

        int MaxX = str.length() / 5;
        String ns = "";

        for (int i = 0; i < MaxX; i++) {
            ns = str.substring(i * 5, (i + 1) * 5); // 5位为1手

            /**
             * 此行改动，为实现题库标记，李朦利3-8  增加'l'判断,表示题库标记位
             */
            if (ns.length() == 5
                    && (ns.charAt(0) == '+' || ns.charAt(0) == '-' || ns.charAt(0) == 'l')) {

                int x;
                int y;
                try {
                    x = Integer.parseInt(ns.substring(1, 3));
                    y = Integer.parseInt(ns.substring(3, 5));
                } catch (Exception e) {
                    x = 0;
                    y = 0;
                }

                int bw;
//				String bwstr = ns.substring(0, 1);
                if (ns.substring(0, 1).equals("+")) {
                    bw = 1;
                } else if (ns.substring(0, 1).equals("-")) {
                    bw = 2;
                } else {
                    bw = 3;
                }

                Coordinate c = new Coordinate(y, x, bw); // 为了兼容电脑客户端
                cs.add(c);
            }
        }

        return cs;
    }

    public static String getBoardsgfStr(Board b) {
        if (b.getCount() == 0)
            return "";
        String resStr = "";
        for (int i = 0, n = b.getCount(); i < n; i++) {
            PieceProcess p = b.getPieceProcess(i);
            // 为了兼容电脑客户端，保存时(X,Y)变成(Y,X)
            resStr = resStr + mygopw2String(p.bw) + mygoint2String(p.c.y)
                    + mygoint2String(p.c.x);
        }
        return resStr;
    }

//	public static String getGeneralBoardsgfStr(GeneralBoard b) {
//		if (b.getCount() == 0)
//			return "";
//		String resStr = "";
//		for (int i = 0, n = b.getCount(); i < n; i++) {
//			GeneralPieceProcess p = b.getGeneralPieceProcess(i);
//			// 为了兼容电脑客户端，保存时(X,Y)变成(Y,X)
//			resStr = resStr + mygopw2String(p.bw) + mygoint2String(p.c.y)
//					+ mygoint2String(p.c.x);
//		}
//		return resStr;
//	}

    public static String getBoardsgfStr(GeneralBoard b) {
        if (b.getCount() == 0)
            return "";
        String resStr = "";
        for (int i = 0, n = b.getCount(); i < n; i++) {
            GeneralPieceProcess p = b.getGeneralPieceProcess(i);
            // 为了兼容电脑客户端，保存时(X,Y)变成(Y,X)
            resStr = resStr + mygopw2String(p.bw) + mygoint2String(p.c.y)
                    + mygoint2String(p.c.x);
        }
        return resStr;
    }

    // 获取最后一步棋串
    public static String getBoardlastStr(Board b) {
        if (b.getCount() == 0)
            return "";
        String resStr = "";
        int StepCount = b.getCount() - 1;

        PieceProcess p = b.getPieceProcess(StepCount);
        resStr = resStr + mygopw2String(p.bw) + mygoint2String(p.c.y)
                + mygoint2String(p.c.x);

        return resStr;
    }

    public static String getBoardlastStr(GeneralBoard b) {
        if (b.getCount() == 0)
            return "";
        String resStr = "";
        int StepCount = b.getCount() - 1;

        GeneralPieceProcess p = b.getGeneralPieceProcess(StepCount);
        resStr = resStr + mygopw2String(p.bw) + mygoint2String(p.c.y)
                + mygoint2String(p.c.x);

        return resStr;
    }

    public static List<Coordinate> getCoordList(String fileName)
            throws IOException {
        String str = TextFile.read(fileName);
        List<Coordinate> cs = new ArrayList<Coordinate>();
        if (str == "")
            return cs;

        String data = str.substring(0, str.indexOf(")"));
        String[] ds = data.split(";");

        for (String s : ds) {
            if (s == "")
                continue;

            String ns = s.toLowerCase();

            if (ns.length() > 5 && (ns.charAt(0) == 'b' || ns.charAt(0) == 'w')) {
                char xChar = ns.charAt(2);
                char yChar = ns.charAt(3);

                int x = xChar - ACode;
                int y = yChar - ACode;
                Coordinate c = new Coordinate(x, y);
                cs.add(c);
            }
        }
        return cs;
    }

    public static void save(Board b, String fileName) throws IOException {
        if (b.getCount() == 0)
            return;

        TextFile textFile = new TextFile();
        textFile.add("(;US[MyGo]");
        for (int i = 0, n = b.getCount(); i < n; i++) {
            PieceProcess p = b.getPieceProcess(i);
            textFile.add(Coordinate2String(p));
        }
        textFile.add(")");

        String dir = "/sdcard/MyGo";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }

        fileName = dir + "/" + fileName;
        textFile.write(fileName);
    }

    private static String Coordinate2String(PieceProcess p) {
        String s = ";" + pw2String(p.bw) + "[" + int2String(p.c.x)
                + int2String(p.c.y) + "]";
        return s;
    }

    private static String int2String(int a) {
        int code = a + ACode;
        char c = (char) code;
        return String.valueOf(c);
    }

    private static String pw2String(int bw) {
        if (bw == Board.Black)
            return "B";
        if (bw == Board.White)
            return "W";
        return "X";
    }

    private static String mygoint2String(int a) {
        if (a >= 10) {
            return String.valueOf(a);
        } else {
            return "0" + String.valueOf(a);
        }
    }

    private static String mygopw2String(int bw) {
        if (bw == Board.Black)
            return "+";
        if (bw == Board.White)
            return "-";
        return "X";
    }

}
