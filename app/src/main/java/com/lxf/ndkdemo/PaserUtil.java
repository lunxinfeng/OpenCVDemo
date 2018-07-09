package com.lxf.ndkdemo;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class PaserUtil {
    private final static boolean DEBUG = true;
    public static int BOARD_SIZE = 19;
    public static int thresh = 80;

    private static void log(String msg) {
        if (DEBUG)
            System.out.println(msg);
    }

    public static List<Rect> findRects(Bitmap bitmap) {
        return contourToRect(jiuJiu(bitmap));
    }

    /**
     * 查找符合预期的轮廓区域，自己根据逻辑去实现
     *
     * @param contours 图片中的所有轮廓
     * @return 所选区域
     */
    public static List<Rect> contourToRect(List<MatOfPoint> contours) {
        List<Rect> rects = new ArrayList<>();
        int size = contours.size();
        for (int i = 0; i < size; i++) {
            MatOfPoint mp = contours.get(i);
            Rect rect = Imgproc.boundingRect(mp);
            double ratio = rect.width / (double) rect.height;
            if (ratio > 0.9 && ratio < 1.1 && rect.width > 300 && rect.width < 475)
                rects.add(rect);
        }
        return rects;
    }

    public static List<MatOfPoint> jiuJiu(Bitmap bitmap) {
        //1.获取源Mat
        Mat src = bitmapToMat(bitmap);
        //2.灰化
        Mat gray = gray(src);
        //3.二值
        Mat thresMat = threshold(gray, thresh, 255, Imgproc.THRESH_BINARY);
        System.out.println("二值化：" + thresh);
        //4.边缘检测
//        Mat cannyMat = canny(thresMat, 60, 180);
        //5.查找轮廓
        List<MatOfPoint> contourList = findContours(thresMat, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(contourList);
        return contourList;
    }

    public static Mat bitmapToMat(Bitmap bitmap) {
        Mat dst = new Mat();
        Utils.bitmapToMat(bitmap, dst);
        return dst;
    }

    public static Bitmap matToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public static void cout(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
//                if (i%100 == 0 && j%100 == 0)
                log("i:" + i + ";j:" + j + "---" + Arrays.toString(mat.get(i, j)));
            }
        }
    }

    /**
     * 灰化
     *
     * @param src 源Mat
     * @return 灰化后的Mat
     */
    public static Mat gray(Mat src) {
        Mat temp = new Mat();
        Mat dst = new Mat();
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        return dst;
    }

    /**
     * 二值化
     *
     * @param src    源Mat
     * @param thresh 自定义阈值
     * @param maxval 最大值
     * @param type   二值化类型
     * @return 二值化后的Mat
     */
    public static Mat threshold(Mat src, int thresh, int maxval, int type) {
        Mat dst = new Mat();
        Imgproc.threshold(src, dst, thresh, maxval, type);
        return dst;
    }

    /**
     * Canny边缘检测
     *
     * @param src        源Mat
     * @param threshold1 低阈值
     * @param threshold2 高阈值
     * @return Canny边缘检测后的Mat
     */
    public static Mat canny(Mat src, int threshold1, int threshold2) {
        Mat dst = new Mat();
        Imgproc.Canny(src, dst, threshold1, threshold2);
        return dst;
    }

    /**
     * @param src    源Mat
     * @param mode   模式
     * @param method 方法
     * @return
     */
    public static List<MatOfPoint> findContours(Mat src, int mode, int method) {
        List<MatOfPoint> contourList = new ArrayList<>();
        Imgproc.findContours(src, contourList, new Mat(), mode, method);
        return contourList;
    }

    /**
     * 解析图片中的黑白子
     *
     * @param bitmap 源图片
     * @return 矩阵：0空1黑2白
     */
    public static int[][] parse(Bitmap bitmap) {
        int[][] a = new int[BOARD_SIZE][BOARD_SIZE];

        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);

        int numB = 0;
        int numW = 0;
        double size = dst.rows() / (double)BOARD_SIZE;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int bw = checkBW(src, dst, (int)(i * size), (int)((i + 1) * size), (int)(j * size), (int)((j + 1) * size));
                if (bw == 1) {
                    numB++;
                    a[i][j] = 1;
                    log("黑：" + i + ";" + j);
                } else if (bw == 2) {
                    numW++;
                    a[i][j] = 2;
                    log("白：" + i + ";" + j);
                } else {
                    a[i][j] = 0;
                }
            }
        }
        log("黑子：" + numB + ";白子：" + numW);
        return a;
    }

    private static int checkBW(Mat src, Mat dst, int x1, int x2, int y1, int y2) {
        int total = (x2 - x1 + 1) * (y2 - y1 + 1);
        int numB = 0;
        int numW = 0;
        for (int i = x1; i < x2; i++) {
            for (int j = y1; j < y2; j++) {
                double value = dst.get(i, j)[0];
                if (value < 60) {
                    numB++;
                } else if (value > 200) {
                    double[] srcValue = src.get(i, j);
                    /*
                        有可能棋盘背景色比较亮，直接用平均值的话很难区分背景和白子，这里判断RGB三原色亮度均超过200
                        才认为该点为白点，因为棋盘一般为黄色，B通道色值会偏低。比如99围棋
                     */
                    if (srcValue[0] > 200 && srcValue[1] > 200 && srcValue[2] > 200)
                        numW++;
                }


                if (i == x1 || i == x2-1 || j == y1 || j==y2-1){
                    dst.put(i,j,255,0,0,255);
                }
            }
        }
        if (numB > total / 3) {
            return 1;
        } else if (numW > total / 5) {
            return 2;
        } else {
            return 0;
        }
    }

    public static Mat parseTest(Bitmap bitmap) {
        int[][] a = new int[BOARD_SIZE][BOARD_SIZE];

        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);

        int numB = 0;
        int numW = 0;
        double size = dst.rows() / (double)BOARD_SIZE;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int bw = checkBW(src, dst, (int)(i * size), (int)((i + 1) * size), (int)(j * size), (int)((j + 1) * size));
                if (bw == 1) {
                    numB++;
                    a[i][j] = 1;
                    log("黑：" + i + ";" + j);
                } else if (bw == 2) {
                    numW++;
                    a[i][j] = 2;
                    log("白：" + i + ";" + j);
                } else {
                    a[i][j] = 0;
                }
            }
        }
        log("黑子：" + numB + ";白子：" + numW);
        return dst;
    }

    /**
     * 转换矩阵成361长串
     *
     * @param a 矩阵
     */
    public static String exChange(int[][] a) {
        int rows = a.length;
        int cols = a[0].length;
        StringBuilder result = new StringBuilder();
        for (int[] anA : a) {
            for (int j = cols - 1; j >= 0; j--) {
                result.append(anA[j]);
            }
        }
        return result.toString();
    }
}
