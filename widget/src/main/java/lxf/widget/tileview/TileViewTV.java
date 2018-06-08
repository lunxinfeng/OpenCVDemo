package lxf.widget.tileview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import static lxf.widget.R.mipmap.focuse;

/**
 * tv版棋盘
 * Created by lxf on 2017/3/21.
 */
public class TileViewTV extends TileView {
    /**
     * 是否绘制焦点框
     */
    private boolean drawFocusFlag = false;
    /**
     * 是否绘制abc标记
     */
    public boolean drawABCFlag = false;
    private Bitmap bitmapFocuse;//焦点框
    /**
     * 焦点框的x路
     */
    public int focuseX = Board.n;
    /**
     * 焦点框的y路
     */
    public int focuseY = Board.n;
    /**
     * 存储abc标记的集合
     */
    private List<ABCFlag> abcFlags;
    private int abcFlag = 65;
    /**
     * 绘制abc标记的画笔
     */
    private Paint paintABCFlag;

    public TileViewTV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TileViewTV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TileViewTV(Context context) {
        super(context);
        init();
    }

    private void init() {
        abcFlags = new ArrayList<>();
        paintABCFlag = new Paint();
        paintABCFlag.setColor(Color.RED);
        paintABCFlag.setStrokeWidth(DensityUtils.dp2px(getContext(),2f));
        paintABCFlag.setTextSize(Math.round(tileSize * 0.7));
    }

    @Override
    public void initStone() {
        super.initStone();
        Bitmap tempFocuse = ExpressionUtil.scaleBitmapFix(context, focuse, 1);
        Matrix matrix = new Matrix();
        matrix.postScale((float) (tileSize / tempFocuse.getWidth()),
                (float) (tileSize / tempFocuse.getHeight()));

        bitmapFocuse = Bitmap.createBitmap(tempFocuse, 0, 0, tempFocuse.getWidth(),
                tempFocuse.getHeight(), matrix, true);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        drawFocusFlag = gainFocus;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (drawFocusFlag)
            drawFocuseFlag(canvas);

        if (drawABCFlag)
            drawABCFlag(canvas);
    }

    //绘制焦点框
    private void drawFocuseFlag(Canvas canvas) {
        canvas.drawBitmap(bitmapFocuse,
                x2Screen(focuseX) - bmpBlack.getWidth() / 2,
                y2Screen(focuseY) - bmpBlack.getHeight() / 2, null);
    }

    //绘制abc标记
    private void drawABCFlag(Canvas canvas) {
        paintABCFlag.setTextSize(Math.round(tileSize));
        if (abcFlags == null || abcFlags.size() == 0)
            return;
        for (ABCFlag flag : abcFlags) {
            char text = (char) flag.flag;
            String s = String.valueOf(text);
            Rect rect = new Rect();
            paintABCFlag.getTextBounds(s, 0, s.length(), rect);
            canvas.drawText(s, x2Screen(flag.x) - rect.width() / 2, y2Screen(flag.y) + rect.height() / 2, paintABCFlag);
        }
    }

    /**
     * 是否获得了焦点
     */
    public boolean hasFocuse() {
        return drawFocusFlag;
    }

    public boolean isLeftEdge() {
        return focuseX == 1;
    }

    public boolean isRightEdge() {
        return focuseX == Board.n;
    }

    public boolean isTopEdge() {
        return focuseY == Board.n;
    }

    public boolean isBottomEdge() {
        return focuseY == 1;
    }

    public void left() {
        focuseX = focuseX == 1 ? 1 : focuseX - 1;
        invalidate();
    }

    public void right() {
        focuseX = focuseX == Board.n ? Board.n : focuseX + 1;
        invalidate();
    }

    public void top() {
        focuseY = focuseY == Board.n ? Board.n : focuseY + 1;
        invalidate();
    }

    public void bottom() {
        focuseY = focuseY == 1 ? 1 : focuseY - 1;
        invalidate();
    }

    public void putChess() {
        doPutPiece(focuseX, focuseY);
    }

    /**
     * 初始化标记位
     */
    public void initABCFlag() {
        abcFlag = 65;
        abcFlags.clear();
        invalidate();
    }

    /**
     * 添加abc标记
     */
    public void drawABCFlag() {
//        //该位置有棋子，直接返回
//        if (getBoard().currentGrid.a[focuseX-1][focuseY-1]!=0)
//            return;

        //如果该位置有标记，则移除
        for (ABCFlag flag : abcFlags) {
            if (flag.x == focuseX && flag.y == focuseY) {
                abcFlags.remove(flag);
                invalidate();
                return;
            }
        }

        //没有则添加
        ABCFlag flag = new ABCFlag();
        flag.x = focuseX;
        flag.y = focuseY;
        flag.flag = abcFlag;
        abcFlags.add(flag);

        abcFlag++;

        invalidate();
    }

    private class ABCFlag {
        int x;//标记的x坐标
        int y;//标记的y坐标
        int flag;//标记的内容
    }
}
