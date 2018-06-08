package lxf.widget.tileview;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lxf.widget.R;


/**
 * 棋盘缩略图
 *
 * @author lxf 2016.8.26
 */
public class ThumbTileViewNew extends ViewGroup {
    /**
     * 上下左右地偏移量，用于缩放后的棋盘
     */
    private int mtop, mleft, mbottom, mright;
    /**
     * 棋盘尺寸
     */
    private int screenWidth = 0;
    /**
     * 棋子资源id
     */
    private int stone_new_w, stone_new_b;
    private Board board;
    private Paint paint;
    /**
     * 棋盘格子尺寸的一半，用于计算实际坐标
     */
    private double xOffset, yOffset;
    /**
     * 棋盘格子尺寸
     */
    private double tileSize;
    /**
     * 黑子
     */
    private Bitmap bmpBlack;
    /**
     * 白子
     */
    private Bitmap bmpWhite;
    /**
     * 上下文
     */
    Context context;
    /**
     * 棋盘路数
     */
    private int boardSize = 19;

    public ThumbTileViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);

        findTileView(context);
        setStone();
    }

    public ThumbTileViewNew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        findTileView(context);
        setStone();
    }

    public ThumbTileViewNew(Context context) {
        super(context);
        findTileView(context);
        setStone();
    }

    /**
     * 设置棋盘路数(需优先指定)
     */
    public void setSize(int boardSize) {
        this.boardSize = boardSize;
        // 是否留出显示坐标的位置
//        if (is_coord) {
        tileSize = screenWidth / (boardSize + 0.8);
        xOffset = tileSize;
        yOffset = tileSize;
//        } else {
//            tileSize = screenWidth / (double) boardSize;
//            xOffset = tileSize / 2;
//            yOffset = tileSize / 2;
//        }
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    /**
     * 设置棋盘总尺寸
     */
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    // 缩放
    public void ScaleTile(float x, float y) {
        CaculateMargins(x, y);
        MarginLayoutParams marginPar = (MarginLayoutParams) getLayoutParams();
        marginPar.setMargins(mleft, mtop, mright, mbottom);
        setLayoutParams(marginPar);
    }

    /**
     * 不缩放
     */
    public void noScale() {
        MarginLayoutParams marginPar = (MarginLayoutParams) getLayoutParams();
        marginPar.setMargins(0, 0, 0, 0);
        setLayoutParams(marginPar);

    }

    /**
     * 求得缩放中心点位置并缩放
     *
     * @param xs 所有点的x坐标集合
     * @param ys 所有点的y坐标集合
     */
    public void ScaleCenter(List<Float> xs, List<Float> ys) {

        float xMax = Collections.max(xs);
        float yMax = Collections.max(ys);
        float xMin = Collections.min(xs);
        float yMin = Collections.min(ys);
        float x = (xMax + xMin) / 2;
        float y = (yMax + yMin) / 2;
        ScaleTile(x, y);

    }

    // 落子
    private void CaculateMargins(float xf, float yf) {

        int x = x2Coordinate(xf);
        int y = y2Coordinate(yf);
        int FDivider = 0; // 第一界点
        int SDivider = 0; // 第二界点
        mleft = 0;
        mtop = 0;
        mright = 0;
        mbottom = 0;

        int scaleMargin = (int) (screenWidth - 2.75f * (xOffset + tileSize));

        if (boardSize > 9) {
            if (boardSize == 19) {
                FDivider = 8;
                SDivider = 11;

            } else if (boardSize == 13) {
                FDivider = 6;
                SDivider = 8;
            }

        } else {
            return;
        }

        if (x <= FDivider && y <= FDivider) {
            mleft = 0;
            mtop = -scaleMargin / 2;
            mright = -scaleMargin / 2;
            mbottom = 0;

        } else if (x > FDivider && x <= SDivider && y <= FDivider) {
            mleft = -scaleMargin / 4;
            mtop = -scaleMargin / 2;
            mright = -scaleMargin / 4;
            mbottom = 0;
        } else if (x > SDivider && y <= FDivider) {
            mleft = -scaleMargin / 2;
            mtop = -scaleMargin / 2;
            mright = 0;
            mbottom = 0;
        } else if (x <= FDivider && y > FDivider && y <= SDivider) {
            mleft = 0;
            mtop = -scaleMargin / 4;
            mright = -scaleMargin / 2;
            mbottom = -scaleMargin / 4;
        } else if (x > FDivider && x <= SDivider && y > FDivider && y <= SDivider) {
            mleft = -scaleMargin / 2;
            mtop = -scaleMargin / 2;
            mright = -scaleMargin / 2;
            mbottom = -scaleMargin / 2;
        } else if (x > SDivider && y > FDivider && y <= SDivider) {
            mleft = -scaleMargin / 2;
            mtop = -scaleMargin / 4;
            mright = 0;
            mbottom = -scaleMargin / 4;
        } else if (x <= FDivider && y > SDivider) {
            mleft = 0;
            mtop = 0;
            mright = -scaleMargin / 2;
            mbottom = -scaleMargin / 2;
        } else if (x > FDivider && x <= SDivider && y > SDivider) {

            mleft = -scaleMargin / 4;
            mtop = 0;
            mright = -scaleMargin / 4;
            mbottom = -scaleMargin / 2;

        } else if (x > SDivider && y > SDivider) {
            mleft = -scaleMargin / 2;
            mtop = 0;
            mright = 0;
            mbottom = -scaleMargin / 2;

        }

    }

    /**
     * 重新设置棋子大小
     */
    public void setStoneSize() {
        Bitmap tempBlack = ExpressionUtil.scaleBitmapFix(context, stone_new_b, 1);

        Bitmap tempWhite = ExpressionUtil.scaleBitmapFix(context,
                stone_new_w, 1);

        Matrix matrix = new Matrix();
        matrix.postScale((float) screenWidth / (boardSize + 1) / tempBlack.getWidth(),
                (float) screenWidth / (boardSize + 1) / tempBlack.getHeight());

        bmpBlack = Bitmap.createBitmap(tempBlack, 0, 0, tempBlack.getWidth(),
                tempBlack.getHeight(), matrix, true);

        bmpWhite = Bitmap.createBitmap(tempWhite, 0, 0, tempWhite.getWidth(),
                tempWhite.getHeight(), matrix, true);
    }
    // ------------------------------------------------------------------画图

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //抗锯齿设置
        canvas.setDrawFilter
                (new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        // 画棋盘
        drawLineGrid(canvas);
        drawStar(canvas);

        drawPiece(canvas);// 画棋子

    }

    // 画棋子
    private void drawPiece(Canvas canvas) {

        for (int x = 1; x <= boardSize; x += 1) {
            for (int y = 1; y <= boardSize; y += 1) {
                int bw = board.getValue(x, y);
                if (bw != Board.None) {

                    if (bw == Board.Black) {

                        canvas.drawBitmap(bmpBlack,
                                x2Screen(x) - bmpBlack.getWidth() / 2,
                                y2Screen(y) - bmpBlack.getHeight() / 2, null);

                    } else {
                        canvas.drawBitmap(bmpWhite,
                                x2Screen(x) - bmpWhite.getWidth() / 2,
                                y2Screen(y) - bmpWhite.getHeight() / 2, null);

                    }
                }
            }
        }
    }


    // 画棋盘的星
    private void drawStar(Canvas canvas) {
        paint.setColor(Color.BLACK);

        for (Coordinate c : Util.createStar(boardSize)) {
            if (c != null) {
                canvas.drawCircle(x2Screen(c.x), y2Screen(c.y), 3f, paint);
            }
        }
    }

    // 画棋盘网格线
    private void drawLineGrid(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        for (int i = 1; i <= boardSize; i++) {
            drawVLine(canvas, i);
            drawHLine(canvas, i);
        }
    }

    // 垂直线
    private void drawVLine(Canvas canvas, int i) {
        canvas.drawLine(x2Screen(i), y2Screen(1), x2Screen(i),
                y2Screen(boardSize), paint);
    }

    // 水平线
    private void drawHLine(Canvas canvas, int i) {
        canvas.drawLine(x2Screen(1), y2Screen(i), x2Screen(boardSize),
                y2Screen(i), paint);
    }

    // ------------------------------------------------------------------坐标变换
    // 虚拟坐标转化为屏幕坐标(虚拟坐标从1开始）
    public float x2Screen(int x) {
        return (float) ((x - 1) * tileSize + xOffset);
    }

    public float y2Screen(int y) {
        return (float) ((boardSize - y) * tileSize + yOffset);
    }

    private int x2Coordinate(float x) {

        return (int) Math.round((x - xOffset) / tileSize) + 1;
    }

    private int y2Coordinate(float y) {

        return boardSize - (int) Math.round((y - yOffset) / tileSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int m = Math.max(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(m, m);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {

    }


    public void setStone() {
        SharedPreferences sp = context.getSharedPreferences("setconfig",
                Context.MODE_PRIVATE);
        if (sp == null) {
            stone_new_b = R.mipmap.stone_b1;
            stone_new_w = R.mipmap.stone_w1;
        } else {
            String w = sp.getString("cmw", null);
            String b = sp.getString("cmb", null);
            if (b == null & w == null) {
                stone_new_b = R.mipmap.stone_b1;
                stone_new_w = R.mipmap.stone_w1;
            } else {
                int resId1 = getResources().getIdentifier(b, null, null);
                int resId2 = getResources().getIdentifier(w, null, null);
                stone_new_b = resId1;
                stone_new_w = resId2;
            }
        }

    }

    private void findTileView(Context context) {
        this.paint = new Paint();
        this.xOffset = 0.5;
        this.yOffset = 0.5;
        this.tileSize = 1;
        this.board = new Board();
        this.context = context;

    }

    @Override
    public void setBackground(Drawable background) {
        if (VERSION.SDK_INT >= 16) {
            super.setBackground(background);
        } else {
            setBackgroundDrawable(background);
        }
    }

    public static void setSgfGame(ThumbTileViewNew thumbTileView, String sgf, int boardSize, int bg,int screenWidth) {

        thumbTileView.setBackgroundResource(bg);

        thumbTileView.setScreenWidth(screenWidth);

        shownetsgfNoScale(sgf, thumbTileView, new Board(), boardSize);
    }

    public static void shownetsgfNoScale(String sgfStr, ThumbTileViewNew tileview, Board board, int boardSize) {
        try {
            List<Coordinate> cs = SgfHelper.getCoordListformStr(sgfStr);
            tileview.setSize(boardSize);
            for (Coordinate c : cs) {
                board.continueput(c.x, c.y, c.bw);
            }
            tileview.setStoneSize();
            tileview.setBoard(board);
            tileview.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter(value = {"sgf", "boardSize", "bg"})
    public static void setSgfGame(ThumbTileViewNew thumbTileView, String sgf, int boardSize, int bg) {
        StringBuffer goCoordiNateAB = new StringBuffer();
        StringBuffer goCoordiNateAW = new StringBuffer();
//        Board.n = boardSize;
        Board board = new Board();
        Parser parser = new Parser(sgf);
        // 解析sgfGame数据
        Game game = parser.parse();
        // 获取属性AB和AW的坐标值
        String stringAW = game.getProperty("AW");
        String stringAB = game.getProperty("AB");
        if (stringAB != null) {
            String[] stringarrayAB;
            // 设置属性的落子坐标以‘，’分隔
            if (stringAB.contains(",")) {
                stringarrayAB = stringAB.split(",");
            } else {
                stringarrayAB = new String[]
                        {stringAB};
            }
            for (String sb : stringarrayAB) {
                // 转成隐智平台的坐标用于显示在棋盘上
                goCoordiNateAB.append("+"
                        + coordinateSgfToYzX(sb.substring(1, 2)
                        .toCharArray()[0])
                        + coordinateSgfToYzY(sb.substring(0, 1)
                        .toCharArray()[0]));
            }
        }

        if (stringAW != null) {
            String[] stringarrayAW;
            if (stringAW.contains(",")) {
                stringarrayAW = stringAW.split(",");
            } else {
                stringarrayAW = new String[]
                        {stringAW};
            }
            for (String sw : stringarrayAW) {
                goCoordiNateAW.append("-"
                        + coordinateSgfToYzX(sw.substring(1, 2)
                        .toCharArray()[0])
                        + coordinateSgfToYzY(sw.substring(0, 1)
                        .toCharArray()[0]));
            }
        }

        String goCoordiNate = goCoordiNateAW.toString()
                + goCoordiNateAB.toString();

//        Bitmap bitmapBackgroud = ExpressionUtil.scaleBitmapFix(thumbTileView.getContext(), bg, 2);
//        thumbTileView.setBackground(new BitmapDrawable(thumbTileView.getResources(), bitmapBackgroud));
        thumbTileView.setBackgroundResource(bg);

        if (boardSize == 19)
            thumbTileView.setScreenWidth((int) DensityUtils.dp2px(thumbTileView.getContext(), 238f));
        else
            thumbTileView.setScreenWidth((int) DensityUtils.dp2px(thumbTileView.getContext(), 150f));

        shownetsgf(goCoordiNate, thumbTileView, board, boardSize);
    }

    public static void shownetsgf(String sgfStr, ThumbTileViewNew tileview, Board board, int boardSize) {
        try {
            List<Coordinate> cs = SgfHelper.getCoordListformStr(sgfStr);
            List<Float> xs = new ArrayList<Float>();
            List<Float> ys = new ArrayList<Float>();
            tileview.setSize(boardSize);
            for (Coordinate c : cs) {
                // board.put(c.x, c.y);
                board.continueput(c.x, c.y, c.bw);
                xs.add(tileview.x2Screen(c.x));
                ys.add(tileview.y2Screen(c.y));
            }

            if (boardSize == 19) {
                if (xs.size() != 0 && ys.size() != 0) {
                    tileview.ScaleCenter(xs, ys);
                }
            } else {
                tileview.noScale();
            }
            tileview.setStoneSize();
            tileview.setBoard(board);
            tileview.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sgf中的y坐标转隐智平台的x坐标
    public static String coordinateSgfToYzX(char substring) {
        int coordinate = 's' - substring + 1;
        if (coordinate < 10) {
            return "0" + coordinate;
        } else {
            return String.valueOf(coordinate);
        }
    }

    // sgf中的x坐标转隐智平台的y坐标
    public static String coordinateSgfToYzY(char substring) {
        int coordinate = substring - 'a' + 1;
        if (coordinate < 10) {
            return "0" + coordinate;
        } else {
            return String.valueOf(coordinate);
        }
    }
}
