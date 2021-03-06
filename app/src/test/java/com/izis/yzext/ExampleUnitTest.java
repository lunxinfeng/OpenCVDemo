package com.izis.yzext;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import lxf.widget.tileview.Board;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void sgf(){
//        String sgf = "+0102";
//        String sgf = "+0102-0203";
//        String sgf = "+0102-0203+0506";
//        String sgf = "+0102-0203+0506-0606";
//        String sgf = "+0102-0203+0506-0606+0809";
//        String sgf = "+0102-0203+0506-0606+0809-0105";
        String sgf = "+0102+0201-0203+0506-0606+0809-0105+0603";
        Pattern compile = Pattern.compile("(\\+[0-9]{4}-[0-9]{4})+(?:\\+[0-9]{4})?");//(?:\+[0-9]{4})?
        Matcher matcher = compile.matcher(sgf);
        if (matcher.find()){
            System.out.println(matcher.group());
            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
//            System.out.println(matcher.group(2));
        }
    }

    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
        System.out.println(System.currentTimeMillis());
        Single.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .repeat(4)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println(System.currentTimeMillis());
                    }
                });

        Thread.sleep(100000);
    }

    @Test
    public void WritesingleGoCoodinate() {
        String singleGoCoodinate = "+0102";
        boolean isbremove = false;
        int rotate = 270;


        String curBW;
        String GoCoodinate;
        String WritesingleGoCoordinate = "";
        if (singleGoCoodinate.length() == 5) {
            curBW = singleGoCoodinate.substring(0, 1);



            int CoordinateXY = 0;
            String CoordinateX = singleGoCoodinate.substring(3, 5);
            String CoordinateY = singleGoCoodinate.substring(1, 3);
            int x = Integer.valueOf(CoordinateX);
            int y = Integer.valueOf(CoordinateY);

            switch (rotate) {
                case 0:
                    CoordinateXY = (Board.n - y) * Board.n + (Board.n - x + 1);
                    break;
                case 90:
                    CoordinateXY = (Board.n - x) * Board.n + y;
                    break;
                case 180:
                    CoordinateXY = (y - 1) * Board.n + x;
                    break;
                case 270:
                    CoordinateXY = (x - 1) * Board.n + (Board.n - y + 1);
                    break;
            }

            String singleGoCoordinate = String.valueOf(CoordinateXY);
            if (CoordinateXY < 10) {
                singleGoCoordinate = "00" + singleGoCoordinate;
            } else if (CoordinateXY >= 10 && CoordinateXY < 100) {
                singleGoCoordinate = "0" + singleGoCoordinate;
            }
            GoCoodinate = singleGoCoordinate;



//            GoCoodinate = GoCoordinateTransition(singleGoCoodinate, rotate);
            String colorWitle = "r000g000b000";
            if ("+".equals(curBW)) {
                String colorRead = "r255g000b000";
                WritesingleGoCoordinate = "~SHP" + GoCoodinate + ","
                        + (isbremove ? colorWitle : colorRead) + ",1#";
            } else if ("-".equals(curBW)) {
                String colorBlue = "r000g000b255";
                WritesingleGoCoordinate = "~SHP" + GoCoodinate + ","
                        + (isbremove ? colorWitle : colorBlue) + ",1#";
            }
        }
        System.out.println(WritesingleGoCoordinate);

    }
}