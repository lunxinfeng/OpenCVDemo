package com.izis.yzext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.izis.yzext.pl2303.ChessChange;

import java.util.ArrayList;
import java.util.List;

/**
 * 路径view，用来屏幕区域截图
 * Created by lxf on 18-5-25.
 */
public class PathView extends View {
    private Path mPath = new Path();
    private RectF mRectF;
    private int width;//棋盘尺寸
    //-----所选矩形区域左上点坐标
    private int left;
    private int top;

    private Paint mPaint;
    private float startX, startY;//手指按下时的坐标
    private float preX, preY;//手指滑动时上一帧的坐标

    public boolean moveable = true;
    private List<float[]> errorPoints = new ArrayList<>();
    private Paint paintError;

    public PathView(Context context) {
        super(context);
        init(context);
    }

    public PathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);

        paintError = new Paint();
        paintError.setColor(Color.RED);
//        mPaint.setStrokeWidth(1);
        paintError.setStyle(Paint.Style.FILL);
//        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        if (manager != null) {
//            manager.getDefaultDisplay().getMetrics(outMetrics);
//        }
//        int w = outMetrics.widthPixels;
//        int h = outMetrics.heightPixels;

//        SharedPreferences sharedPreferences = context.getSharedPreferences("lxf_path",Context.MODE_PRIVATE);
//        width = sharedPreferences.getInt("width",0);
//        left = sharedPreferences.getInt("left",0);
//        top = sharedPreferences.getInt("top",0);

        mRectF = new RectF(left, top, left + width, top + width);
        mPath.addRect(mRectF, Path.Direction.CW);
    }

    public void clearErrorPoint() {
        errorPoints.clear();
    }

    public void drawErrorPoint(List<ChessChange> chessChangeList, int boardSize) {
        for (ChessChange chessChange : chessChangeList) {
            drawErrorPoint(chessChange, boardSize);
        }
    }

    public void drawErrorPoint(ChessChange chessChange, int boardSize) {
        int x = chessChange.getX();
        int y = chessChange.getY();
        size = mRectF.width() / (boardSize - 1);
        errorPoints.add(new float[]{(y - 1) * size, (x - 1) * size});
        invalidate();
    }

    private float size;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);

        if (errorPoints.size() > 0) {
            for (float[] item : errorPoints) {
                canvas.drawCircle(mRectF.left + item[0], mRectF.top + item[1], 3, paintError);
//                canvas.drawRect(mRectF.left + item[0] - size / 2,mRectF.top + item[1] - size/2,mRectF.left + item[0] + size / 2,mRectF.top + item[1] + size/2,paintError);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (!mRectF.contains(event.getRawX(), event.getRawY())) return false;
        if (!moveable) return true;

        if (event.getRawX() > (mRectF.centerX() + mRectF.width() / 4) &&
                event.getRawY() > (mRectF.centerY() + mRectF.width() / 4))//右下角
            scale(event);
        else
            move(event);
        mPath.reset();
        mRectF = new RectF(left, top, left + width, top + width);
        mPath.addRect(mRectF, Path.Direction.CW);
        invalidate();
        return true;
    }

    private void scale(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                startY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (preX == 0)
                    preX = startX;
                if (preY == 0)
                    preY = startY;
                width = width + (int) (event.getRawX() - preX);
                preX = event.getRawX();
                preY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                preX = 0;
                preY = 0;
                break;
        }
    }

    private void move(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                startY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (preX == 0)
                    preX = startX;
                if (preY == 0)
                    preY = startY;
                left = left + (int) (event.getRawX() - preX);
                top = top + (int) (event.getRawY() - preY);
                preX = event.getRawX();
                preY = event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                preX = 0;
                preY = 0;
                break;
        }
    }

    public RectF getRectF() {
        return mRectF;
    }

    public void setRectF(RectF mRectF) {
        left = (int) mRectF.left;
        top = (int) mRectF.top;
        width = (int) mRectF.width();
        this.mRectF = new RectF(left, top, left + width, top + width);
        mPath.reset();
        mPath.addRect(mRectF, Path.Direction.CW);
        invalidate();
    }

    public void reset() {
        setRectF(new RectF());
    }
}
