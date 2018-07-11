package com.izis.yzext.base;

import android.graphics.Color;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.izis.yzext.pl2303.SnackUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lxf.widget.tileview.Board;
import lxf.widget.tileview.Coordinate;
import lxf.widget.tileview.SgfHelper;
import lxf.widget.tileview.TileView;

import static lxf.widget.tileview.ThumbTileViewNew.coordinateSgfToYzX;
import static lxf.widget.tileview.ThumbTileViewNew.coordinateSgfToYzY;


public class TileViewUtils {

    /**
     * 让子操作
     */
    public static String doLet(int let) {

        String str = "";

        switch (let) {
            case 2:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707";
                        break;
                    case 13:
                        str = "+0404-0000+1010";
                        break;
                    case 19:
                        str = "+0404-0000+1616";
                        break;
                }
                break;

            case 3:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707-0000+0703";
                        break;
                    case 13:
                        str = "+0404-0000+1010-0000+1004";
                        break;
                    case 19:
                        str = "+0404-0000+1616-0000+1604";
                        break;
                }
                break;

            case 4:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707-0000+0703-0000+0307";
                        break;
                    case 13:
                        str = "+0404-0000+1010-0000+1004-0000+0410";
                        break;
                    case 19:
                        str = "+0404-0000+1616-0000+1604-0000+0416";
                        break;
                }

                break;

            case 5:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707-0000+0703-0000+0307-0000+0505";
                        break;
                    case 13:
                        str = "+0404-0000+1010-0000+1004-0000+0410-0000+0707";
                        break;
                    case 19:
                        str = "+0404-0000+1616-0000+1604-0000+0416-0000+1010";
                        break;
                }

                break;

            case 6:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707-0000+0703-0000+0307-0000+0505-0000+0503";
                        break;
                    case 13:
                        str = "+0404-0000+1010-0000+1004-0000+0410-0000+0707-0000+0704";
                        break;
                    case 19:
                        str = "+0404-0000+1616-0000+1604-0000+0416-0000+1016-0000+1004";
                        break;
                }

                break;

            case 7:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707-0000+0703-0000+0307-0000+0505-0000+0503-0000+0507";
                        break;
                    case 13:
                        str = "+0404-0000+1010-0000+1004-0000+0410-0000+0707-0000+0704-0000+0407";
                        break;
                    case 19:
                        str = "+0404-0000+1616-0000+1604-0000+0416-0000+1016-0000+1004-0000+1010";
                        break;
                }

                break;

            case 8:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707-0000+0703-0000+0307-0000+0505-0000+0503-0000+0507-0000+0705";
                        break;
                    case 13:
                        str = "+0404-0000+1010-0000+1004-0000+0410-0000+0707-0000+0704-0000+0407-0000+1007";
                        break;
                    case 19:
                        str = "+0404-0000+1616-0000+1604-0000+0416-0000+1016-0000+1004-0000+0410-0000+1610";
                        break;
                }

                break;

            case 9:
                switch (Board.n) {
                    case 9:
                        str = "+0303-0000+0707-0000+0703-0000+0307-0000+0505-0000+0503-0000+0507-0000+0705-0000+0305";
                        break;
                    case 13:
                        str = "+0404-0000+1010-0000+1004-0000+0410-0000+0707-0000+0704-0000+0407-0000+1007-0000+0710";
                        break;
                    case 19:
                        str = "+0404-0000+1616-0000+1604-0000+0416-0000+1016-0000+1004-0000+0410-0000+1610-0000+1010";
                        break;
                }

                break;

            default:
                Log.i("mygo", "什么也没有运行");
                break;
        }

        return str;
    }

    /**
     * 让子操作
     */
    public static String doLetRandom(int let) {
        String str = "";
        Random random = new Random();
        List<Integer> list = new ArrayList<>();
        int randomNum;
        while (list.size() < let) {
            randomNum = random.nextInt(361) + 1;
            if (!list.contains(randomNum))
                list.add(randomNum);
        }

        String black;
        int m;
        int n;
        for (int i = 0; i < let * 2 - 1; i++) {
            black = "+";
            if (i % 2 == 0) {
                randomNum = list.get(i / 2);
                m = randomNum / 19 + 1;
                n = randomNum % 19;

                if (n == 0) {
                    m--;
                    n = 19;
                }

                if (m < 10) {
                    black = black + "0" + m;
                } else {
                    black = black + "" + m;
                }

                if (n < 10) {
                    black = black + "0" + n;
                } else {
                    black = black + "" + n;
                }

                str += black;
            } else {
                str += "-0000";
            }
        }

        return str;
    }

    /**
     * 在棋盘上显示棋子
     */
    public static void shownetsgf(TileView tileView, Board board, String sgfStr) {
        try {
            List<Coordinate> cs = SgfHelper.getCoordListformStr(sgfStr);
            for (Coordinate c : cs) {
                board.put(c.x, c.y);
            }

            if (tileView!=null)
                tileView.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示棋子
     *
     * @param sgfStr   棋子坐标
     * @param tileView 棋盘
     * @param board    网格盘面
     */
    public static void shownetsgf_noRe(String sgfStr, TileView tileView, Board board) {
        try {
            List<Coordinate> cs = SgfHelper.getCoordListformStr(sgfStr);
            for (Coordinate c : cs) {
                board.continueputNoPick(c.x, c.y, c.bw);
            }

            if (cs.size() == 1) {
                tileView.sound();
            }
            tileView.invalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 带缩放棋盘的显示棋谱
     *
     * @param string 题目字符串
     * @param list   答案字符串集合
     */
    public static void shownetsgf_scale(String string, List<String> list, TileView tileView, Board board) {
        try {
            List<Coordinate> cs = SgfHelper.getCoordListformStr(string);
            List<Float> xs = new ArrayList<>();
            List<Float> ys = new ArrayList<>();
            for (Coordinate c : cs) {
                board.continueput(c.x, c.y, c.bw);
                if (c.bw == 1) {
                    board.setCurBW(2);
                } else if (c.bw == 2) {
                    board.setCurBW(1);
                }

                xs.add(tileView.x2Screen(c.x));
                ys.add(tileView.y2Screen(c.y));
            }

            List<Float> xs2 = new ArrayList<>();
            List<Float> ys2 = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                List<Coordinate> cs2 = SgfHelper.getCoordListformStr(string +
                        list.get(i));
                for (Coordinate c : cs2) {
                    xs2.add(tileView.x2Screen(c.x));
                    ys2.add(tileView.y2Screen(c.y));
                }
            }
            if (tileView.isCanScale) {
                tileView.ScaleCenter(xs2, ys2);
            }

//            if (tileView.isCanScale) {
//                tileView.ScaleCenter(xs, ys);
//            }
            tileView.invalidate();
            tileView.isCanScale = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sgf坐标转成隐智格式，用于把坐标显示在棋盘上(带+ -号)
    public static String coordenateSgfToYzBandSymbol(String sgfCoordinate) {
        String x = coordinateSgfToYzX(sgfCoordinate.substring(3, 4)
                .toCharArray()[0]);
        String y = coordinateSgfToYzY(sgfCoordinate.substring(2, 3)
                .toCharArray()[0]);
        char letter = sgfCoordinate.substring(0, 1).toCharArray()[0];
        String symbol;
        if (letter == 'B') {
            symbol = "+";
        } else {
            symbol = "-";
        }
        return symbol + x + y;
    }


//    public static void count(Activity activity, final TileView tileView) {
//        Observable.create(new ObservableOnSubscribe<Score>() {
//            @Override
//            public void subscribe(ObservableEmitter<Score> e) throws Exception {
//                Score score = new ScoreUtil().Final_score3(tileView.getBoard().currentGrid.getA(), Board.n);
//                if (score != null) {
//                    e.onNext(score);
//                    e.onComplete();
//                } else {
//                    e.onError(new Throwable("数子失败"));
//                }
//            }
//        })
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new ProgressSubscriber<Score>(activity, "数子计算中...") {
//                    @Override
//                    public void _onNext(Score score) {
//                        tileView.is_judge = false;
//                        int[][] TileScore = score.getDlist();//死子位置
//
//                        String tileViewString = "";
//                        for (int[] aTileScore : TileScore) {
//                            for (int anATileScore : aTileScore) {
//                                tileViewString += anATileScore;
//                            }
//                            tileViewString = tileViewString + ",";
//                        }
//                        tileView.pieces = TileScore;
//                        tileView.invalidate();
//
//                        String WIN = score.getScore().split(" ")[0];
//                        String StrResult = score.getScore().split(" ")[1];
//
////                        if ((WIN.equalsIgnoreCase("W") && !useBlack) ||
////                                (WIN.equalsIgnoreCase("B") && useBlack)) {
////                            finishGame(true, StrResult);
////                        } else {
////                            finishGame(false, StrResult);
////                        }
////                        sethint(StrResult);
//
//                        showResult(tileView, StrResult);
//                    }
//
//                    @Override
//                    public void _onError(String error) {
//                        showResult(tileView, error);
//                    }
//                });
//    }

    private static void showResult(final TileView tileView, String strResult) {
        final Snackbar snackbar = Snackbar.make(tileView, strResult, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();

            }
        });
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                tileView.is_judge = true;
                tileView.invalidate();
            }
        });
        SnackUtil.setSnackbarColor(snackbar, Color.BLACK, Color.WHITE, Color.RED, Color.GREEN);
        snackbar.show();
    }
}
