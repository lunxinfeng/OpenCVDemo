package com.lxf.ndkdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private int result = 0;
    private Intent intent = null;
    private final int REQUEST_MEDIA_PROJECTION = 1;
    private final int REQUEST_CHOSE_FILE = 2;
    private MediaProjectionManager mMediaProjectionManager;
    private Spinner spinner;
    private ImageView iv_source;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        staticLoadCVLibraries();
        iv_source = findViewById(R.id.iv_source);
        iv_result = findViewById(R.id.iv_result);
        spinner = findViewById(R.id.spinner);

        String[] items = new String[]{"隐智", "弈客", "弈城", "99", "腾讯", "新博"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                int res = R.mipmap.yz;
//                switch (position) {
//                    case 0:
//                        res = R.mipmap.yz;
//                        break;
//                    case 1:
//                        res = R.mipmap.yk;
//                        break;
//                    case 2:
//                        res = R.mipmap.yc;
//                        break;
//                    case 3:
//                        res = R.mipmap.jj;
//                        break;
//                    case 4:
//                        res = R.mipmap.tx;
//                        break;
//                    case 5:
//                        res = R.mipmap.xb;
//                        break;
//                }
//                srcBitmap = BitmapFactory.decodeResource(getResources(), res);
////              srcBitmap = ratio(srcBitmap, 200, 200);
//                iv_source.setImageBitmap(srcBitmap);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        srcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.board);
        iv_source.setImageBitmap(srcBitmap);

        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public void startService(View view) {
        startIntent();
    }

    public void perClick(View view) {
        //生成点击坐标
//        int x = (int) (Math.random() * width * 0.6 + width * 0.2);
//        int y = (int) (Math.random() * height * 0.6 + height * 0.2);
        //利用ProcessBuilder执行shell命令
        String[] order = {
                "input",
                "tap",
                "" + 400,
                "" + 400
        };
        try {
            new ProcessBuilder(order).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onChessParse(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CHOSE_FILE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        Toast.makeText(this, "X at " + x + ";Y at " + y, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void startIntent() {
        if (intent != null && result != 0) {
            ((ShotApplication) getApplication()).setResult(result);
            ((ShotApplication) getApplication()).setIntent(intent);
            Intent intent = new Intent(getApplicationContext(), MyService.class);
            startService(intent);
        } else {
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            ((ShotApplication) getApplication()).setMediaProjectionManager(mMediaProjectionManager);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                switch (requestCode) {
                    case REQUEST_MEDIA_PROJECTION:
                        result = resultCode;
                        intent = data;
                        ((ShotApplication) getApplication()).setResult(resultCode);
                        ((ShotApplication) getApplication()).setIntent(data);
                        Intent intent = new Intent(getApplicationContext(), MyService.class);
                        startService(intent);

                        finish();
                        break;
                    case REQUEST_CHOSE_FILE:
                        try {
                            srcBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                          srcBitmap = ratio(srcBitmap, 200, 200);
                            iv_source.setImageBitmap(srcBitmap);
                            PaserUtil.parse(srcBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }

            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public Bitmap ratio(Bitmap image, float pixelW, float pixelH) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, os);
        if (os.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            os.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, os);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        is = new ByteArrayInputStream(os.toByteArray());
        bitmap = BitmapFactory.decodeStream(is, null, newOpts);
        //压缩好比例大小后再进行质量压缩
//      return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    //------------------------------------------------------------------------------//

    private ImageView iv_result;
    private Bitmap srcBitmap;

    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.i("MainActivity", "Open CV Libraries loaded...");
        } else {
            Log.i("MainActivity", "Open CV Libraries not loaded...");
        }
    }

    //边缘检测
    public void change1(View view) {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(srcBitmap, rgbMat);
        Imgproc.Canny(rgbMat, grayMat, 200, 240);
        Utils.matToBitmap(grayMat, grayBitmap);
        iv_result.setImageBitmap(grayBitmap);

    }

    //灰化
    public void change2(View view) {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(srcBitmap, src);
        System.out.println("源图：" + src);
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);
        Log.i("CV", "image type:" + (temp.type() == CvType.CV_8UC3));
        Imgproc.cvtColor(temp, dst, Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(dst, grayBitmap);
        System.out.println("目标图：" + dst);

        int numB = 0;
        int numW = 0;
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                int row1 = (int) ((i + 0.4) * dst.rows() / 19);
                int col1 = (int) ((j + 0.4) * dst.cols() / 19);
                double value1 = dst.get(row1, col1)[0];
                int row2 = (int) ((i + 0.6) * dst.rows() / 19);
                int col2 = (int) ((j + 0.4) * dst.cols() / 19);
                double value2 = dst.get(row2, col2)[0];
                int row3 = (int) ((i + 0.4) * dst.rows() / 19);
                int col3 = (int) ((j + 0.6) * dst.cols() / 19);
                double value3 = dst.get(row3, col3)[0];
                int row4 = (int) ((i + 0.6) * dst.rows() / 19);
                int col4 = (int) ((j + 0.6) * dst.cols() / 19);
                double value4 = dst.get(row4, col4)[0];
                List<Double> values = new ArrayList<>();
                values.add(value1);
                values.add(value2);
                values.add(value3);
                values.add(value4);
                Collections.sort(values);
                double value = (values.get(1) + values.get(2)) / 2;
                if (value < 80)
                    numB++;
                if (value > 200)
                    numW++;

                System.out.println("i=" + i + ",j=" + j + ":" + value);
            }
        }
        System.out.println("黑子：" + numB + ";白子：" + numW);
        iv_result.setImageBitmap(grayBitmap);
    }

    //还原
    public void init(View view) {
        iv_result.setImageBitmap(null);
    }

    //二值化
    public void thres(View view) {
        Mat src = PaserUtil.bitmapToMat(srcBitmap);
        Mat gray = PaserUtil.gray(src);
        Mat thres = PaserUtil.threshold(gray,80,255,Imgproc.THRESH_BINARY);
        iv_result.setImageBitmap(PaserUtil.matToBitmap(thres));
    }

    //霍夫直线
    public void hfLine(View view) {
        Mat src = new Mat();
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat lines = new Mat();

        Utils.bitmapToMat(srcBitmap, src);
        //将图像转换为灰度
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGRA2GRAY);

        Imgproc.Canny(grayMat, cannyEdges, 200, 240);
//        Imgproc.threshold(grayMat, cannyEdges, 80, 255, Imgproc.THRESH_BINARY);

        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, 200, 5, 1);
//        Imgproc.HoughLines(cannyEdges, lines, 1, Math.PI / 180, 50);
        PaserUtil.cout(lines);
        System.out.println("霍夫直线：" + lines);

        Mat hfLines = new Mat();
        hfLines.create(cannyEdges.rows(), cannyEdges.cols(), CvType.CV_8UC1);

        //在图像上绘制直线
        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i, 0);
            double x1, y1, x2, y2;

            x1 = points[0];
            y1 = points[1];
            x2 = points[2];
            y2 = points[3];

            if (x1 == x2){
                int min = (int) Math.min(y1,y2);
                int max = (int) Math.max(y1,y2);
                for (int j = min;j<=max;j++){
                    for (int k = 0;k<5;k++){
                        grayMat.put((int)x1+k,j,150);
                        grayMat.put((int)x1-k,j,150);
                    }
                }
            }
            if (y1 == y2){
                int min = (int) Math.min(x1,x2);
                int max = (int) Math.max(x1,x2);
                for (int j = min;j<=max;j++){
                    for (int k = 0;k<5;k++){
                        grayMat.put(j,(int)y1+k,150);
                        grayMat.put(j,(int)y1-k,150);
                    }

                }
            }

            Point pt1 = new Point(x1, y1);
            Point pt2 = new Point(x2, y2);

            //在一副图像上绘制直线
            Imgproc.line(hfLines, pt1, pt2, new Scalar(255, 0, 0), 1);
        }

        //将Mat转换为位图
        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grayMat, resultBitmap);
        iv_result.setImageBitmap(resultBitmap);
    }

    //轮廓
    public void lunKuo(View view) {
//        List<MatOfPoint> contourList = PaserUtil.yiCheng(srcBitmap);
        List<MatOfPoint> contourList = PaserUtil.jiuJiu(srcBitmap);


        //在新的图像上绘制轮廓
        Mat contours = new Mat();
        contours.create(srcBitmap.getHeight(), srcBitmap.getWidth(), CvType.CV_8UC3);

        Random random = new Random();
        for (int i = 0; i < contourList.size(); i++) {
            MatOfPoint mp = contourList.get(i);
            Rect rect = Imgproc.boundingRect(mp);
            double ratio = rect.width / (double) rect.height;
            if (ratio > 0.95 && ratio < 1.05 && rect.width > 300) {
                Imgproc.drawContours(contours, contourList, i, new Scalar(random.nextInt(255), random.nextInt(255), random.nextInt(255)), 2);
//                System.out.println(mp);
//                System.out.println(Arrays.toString(mp.toArray()));
                System.out.println(rect);
            }
        }

//        //1.获取源Mat
//        Mat src = PaserUtil.bitmapToMat(srcBitmap);
//        //2.灰化
//        Mat gray = PaserUtil.gray(src);
//        PaserUtil.cout(gray);
//        //3.二值
//        Mat contours = PaserUtil.threshold(gray, 80, 255, Imgproc.THRESH_BINARY);
//        PaserUtil.cout(contours);
//        //将Mat转换为位图
        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(contours, resultBitmap);
        iv_result.setImageBitmap(resultBitmap);

    }

    //测试
    public void test(View view) {
//        Mat src = PaserUtil.bitmapToMat(srcBitmap);
//        PaserUtil.cout(src);
//        System.out.println("通道数：" + src.channels());
//        Mat grayMat = PaserUtil.gray(src);
//        PaserUtil.cout(grayMat);
//        System.out.println("通道数：" + grayMat.channels());+

//        Mat dst = PaserUtil.parseTest(srcBitmap);
//        iv_result.setImageBitmap(PaserUtil.matToBitmap(dst));

        Mat gray = PaserUtil.gray(PaserUtil.bitmapToMat(srcBitmap));
        Mat threshold = PaserUtil.threshold(gray,80,255,Imgproc.THRESH_BINARY);
        List<MatOfPoint> contourList = new ArrayList<>();
        Mat dst = new Mat();
        Imgproc.findContours(threshold, contourList, dst, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("测试-轮廓数量：" + contourList.size());
//        System.out.println("测试-轮廓[0]：" + contourList.get(0));
//        System.out.println("测试-轮廓[1]：" + contourList.get(1));
//        System.out.println("测试-轮廓[2]：" + contourList.get(2));
        System.out.println("测试-hierarchy：" + dst);
        System.out.println("测试-hierarchy[0]：" + Arrays.toString(dst.get(0,0)));
        System.out.println("测试-hierarchy[1]：" + Arrays.toString(dst.get(0,1)));
        System.out.println("测试-hierarchy[2]：" + Arrays.toString(dst.get(0,2)));
        int size = contourList.size();
        int num = 0;
        for (int i=0;i<size;i++){
            if (dst.get(0,i)[2]>=0||dst.get(0,i)[3]>=0){
                System.out.println("测试-hierarchy[" + i +"]：" + Arrays.toString(dst.get(0,i)));
                num++;
            }
        }
        System.out.println("测试-num：" + num);
    }

    //霍夫圆
    public void hfCircle(View view) {
        Mat src = new Mat();
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat cricles = new Mat();

        Utils.bitmapToMat(srcBitmap, src);
        //将图像转换为灰度
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGRA2GRAY);

        Imgproc.Canny(grayMat, cannyEdges, 200, 240);

//        Imgproc.HoughCircles(cannyEdges,cricles,Imgproc.CV_HOUGH_GRADIENT,1,5);
        Imgproc.HoughCircles(cannyEdges,cricles,Imgproc.CV_HOUGH_GRADIENT,1,5,240,10,cannyEdges.rows()/25/2,cannyEdges.rows()/15/2);
        PaserUtil.cout(cricles);
        System.out.println(cricles);

        Mat hfCircle = new Mat();
        hfCircle.create(cannyEdges.rows(), cannyEdges.cols(), CvType.CV_8UC1);

        //在图像上绘制圆
        for (int i = 0; i < cricles.cols(); i++) {
            double[] points = cricles.get(0, i);
            double x,y;
            int r;

            x = points[0];
            y = points[1];
            r = (int) points[2];

            Point pt1 = new Point(x, y);

            //在一副图像上绘制圆
            Imgproc.circle(hfCircle, pt1, r, new Scalar(255, 255, 255), 1);
        }

        //将Mat转换为位图
        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(hfCircle, resultBitmap);
        iv_result.setImageBitmap(resultBitmap);
    }
}
