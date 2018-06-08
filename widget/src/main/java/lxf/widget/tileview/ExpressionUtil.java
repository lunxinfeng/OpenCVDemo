package lxf.widget.tileview;

/**
 * 表情工具类，用来处理表情信息
 李朦利

 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lxf.widget.R;


public class ExpressionUtil
{

    private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

    private static void addPattern(Map<Pattern, Integer> map, String smile,
                                   int resource) {
        map.put(Pattern.compile(Pattern.quote(smile)), resource);
    }


    // public static String pattern = "\\[f\\d{3}\\]";
    // public static String pattern = "f0[0-9]{2}|f10[0-7]";
    public static void dealExpression(Context context,
                                      SpannableString spannableString, Pattern patten, int start,TextView tv)
            throws SecurityException, NoSuchFieldException,
            NumberFormatException, IllegalArgumentException,
            IllegalAccessException
    {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find())
        {
            String key = matcher.group();
            if (matcher.start() < start)
            {
                continue;
            }
            Field field = R.drawable.class.getDeclaredField(key);
            int resId = Integer.parseInt(field.get(null).toString()); // 通过上面匹配得到的字符串来生成图片资源id
            if (resId != 0)
            {

                int size = ( int) tv.getTextSize();
//                 bitmap = Bitmap.createScaledBitmap(bitmap, size+20, size+20, true);
//                 ImageSpan imageSpan = new ImageSpan(bitmap); //
                // 通过图片资源id来得到bitmap，用一个ImageSpan来包装
                int end = matcher.start() + key.length(); //
//                // 计算该图片名字的长度，也就是要替换的字符串的长度
//                 spannableString.setSpan(imageSpan, matcher.start(), end,
//                 Spanned.SPAN_INCLUSIVE_EXCLUSIVE); // 将该图片替换字符串中规定的位置中
                Drawable drawable = context.getResources().getDrawable(resId);
                drawable.setBounds(0, 0, size+20, size+20);// 这里设置图片的大小
                ImageSpan imageSpan = new ImageSpan(drawable,
                        ImageSpan.ALIGN_BOTTOM);
                spannableString.setSpan(imageSpan, matcher.start(), end,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE); // 将该图片替换字符串中规定的位置中

                if (end < spannableString.length())
                { // 如果整个字符串还未验证完，则继续。。
                    dealExpression(context, spannableString, patten, end,tv);
                }
            }
        }
    }

    /**
     * 将传入的String转换成SpannableString
     * @param context
     * @param str
     * @param tv
     * @return
     */

    public static SpannableString getSmiledText(Context context,String str,TextView tv) {
        SpannableString spannableString = new SpannableString(str);
        addSmiles(context, spannableString,tv);//替换成表情
        return spannableString;
    }
    /**
     * 循环判断是否在emoticons中存在，存在则转换成表情
     * @param context
     * @param spannable
     * @param tv
     * @return
     */
    public static boolean addSmiles(Context context, SpannableString spannable,TextView tv) {
        boolean hasChanges = false;
        int size = (int) tv.getTextSize();
        for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(),
                        matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    Drawable drawable = context.getResources().getDrawable(
                            entry.getValue());
                    drawable.setBounds(0, 0, size+20, size+20);// 这里设置图片的大小
                    ImageSpan imageSpan = new ImageSpan(drawable,
                            ImageSpan.ALIGN_BOTTOM);
                    spannable.setSpan(imageSpan, matcher.start(),
                            matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//替换原来文字的位置
                }
            }
        }
        return hasChanges;
    }


    /**
     * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
     *
     * @param context
     * @param str
     * @return
     */
    public static SpannableString getExpressionString(Context context,
                                                      String str, String pattern,TextView tv)
    {
        SpannableString spannableString = new SpannableString(str);
        Pattern sinaPatten = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        try
        {
            dealExpression(context, spannableString, sinaPatten, 0,tv);
        }
        catch (Exception e)
        {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }

    /**
     * 保存所有表情图片的资源ID
     */
    public static int[] getExpressRcIds()
    {
        int imageIds[] = new int[SystemConstant.express_counts];
        int resourceId = 0;
        String fieldName;
        for (int i = 0; i < SystemConstant.express_counts; i++)
        {
            try
            {
                if (i < 10)
                {
                    fieldName = "f00" + i;
                }
                else if (i < 100)
                {
                    fieldName = "f0" + i;
                }
                else
                {
                    fieldName = "f" + i;
                }
                Field field = R.drawable.class.getDeclaredField(fieldName);
                resourceId = Integer.parseInt(field.get(null).toString());
                imageIds[i] = resourceId;
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (NoSuchFieldException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        return imageIds;
    }


    /**
     * Bitmap图像压缩二次取样
     * @param
     * @return
     */
    public static Bitmap getBitmapByBytes(Context context,int resId){

        int width = 0;
        int height = 0;
        int sampleSize = 1;
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        int be = (int) ((options.outHeight > options.outWidth ? options.outHeight / 150 : options.outWidth / 200));
        if (be <= 0) // 判断200是否超过原始图片高度
            be = 1; // 如果超过，则不进行缩放
        options.inSampleSize = be;
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }


    public static Bitmap scaleBitmap(Context context,int resId)
    {
        Options newOpts = new Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.wood1, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.wood1, newOpts);
        return bitmap;

    }

    /**
     * 固定缩放比
     * @param context
     * @param resId 资源ID
     * @param mul 倍数
     * @return
     * 李朦利
     */
    public static Bitmap scaleBitmapFix(Context context,int resId,int mul)
    {
        Options newOpts = new Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resId , newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;

        newOpts.inSampleSize = mul;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeResource(context.getResources(), resId, newOpts);
        return bitmap;

    }

    /**
     * 以最节省内存方式读取本地资源图片
     */
    public static Bitmap readBitmap(Context context,int resid){
        Options opts = new Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resid);

        return BitmapFactory.decodeStream(is,null,opts);
    }

}
