package lxf.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * 登陆按钮
 * Created by lxf on 2017/3/20.
 */
public class LoginButton extends View {
    private final int duration = 300;
    private Paint paintBg;
    private Paint paintText;
    private Paint paintCircle;
    private int startWidth;//初始宽度
    private int startHeight;//初始高度
    private int marginLength;//缩进距离
    private boolean drawCircle = false;//绘制圆还是文本

    private int bgColor;
    private int bgColorFocused;
    private int textColor;
    private String text;
    private int textSize;

    private int textWidth;
    private int textHeight;

    private Loading loading;

    public void setLoading(Loading loading) {
        this.loading = loading;
    }

    public LoginButton(Context context) {
        super(context, null);
    }

    public LoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoginButton);

        bgColor = array.getColor(R.styleable.LoginButton_bgColor, Color.RED);
        bgColorFocused = array.getColor(R.styleable.LoginButton_bgColorFocused, 0);
        textColor = array.getColor(R.styleable.LoginButton_textColor, Color.WHITE);
        text = array.getString(R.styleable.LoginButton_text);
        textSize = array.getDimensionPixelSize(R.styleable.LoginButton_textSize, 24);

        array.recycle();
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);

        paintBg = new Paint();
        paintBg.setColor(bgColor);
        paintBg.setStyle(Paint.Style.FILL);
        paintBg.setAntiAlias(true);


        paintText = new Paint();
        paintText.setColor(textColor);
        paintText.setTextSize(textSize);
        Rect rect = new Rect();
        paintText.getTextBounds(text, 0, text.length(), rect);
        textWidth = rect.width();
        textHeight = rect.height();


        paintCircle = new Paint();
        paintCircle.setColor(textColor);
        paintCircle.setStrokeWidth(3);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setAntiAlias(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            startWidth = getMeasuredWidth();
            startHeight = getMeasuredHeight();
            invalidate();
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && bgColorFocused != 0)
            paintBg.setColor(bgColorFocused);
        else
            paintBg.setColor(bgColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);
        if (drawCircle)
            drawCircle(canvas);
        else
            drawText(canvas);
    }

    //绘制圆，动画
    private void drawCircle(Canvas canvas) {
        RectF oval = new RectF();
        oval.left = startWidth / 2 - startHeight / 2 + startHeight / 5;
        oval.right = oval.left + startHeight / 5 * 3;
        oval.top = startHeight / 5;
        oval.bottom = oval.top + startHeight / 5 * 3;
        canvas.drawArc(oval, 0, 120, false, paintCircle);
    }

    //绘制文本
    private void drawText(Canvas canvas) {
        canvas.drawText(text, startWidth / 2 - textWidth / 2, startHeight / 2 + textHeight / 2, paintText);
    }

    //绘制背景
    private void drawBg(Canvas canvas) {
        int left = marginLength;
        int top = 0;
        int right = startWidth - marginLength;
        int bottom = startHeight;
        RectF rectF = new RectF(left, top, right, bottom);
        //绘制圆角矩形  1.范围  2.x方向的圆角半径  3.y方向的圆角半径  4.画笔
        canvas.drawRoundRect(rectF, startHeight / 2, startHeight / 2, paintBg);
    }

    /**
     * 点击按钮后启动动画
     */
    public void click() {
        setClickable(false);

        float scale = startHeight / (float) startWidth;
        ValueAnimator animator = ValueAnimator.ofFloat(1, scale);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float flag = (float) animation.getAnimatedValue();
                marginLength = (int) (startWidth * (1 - flag)) / 2;

                if ((startWidth - marginLength * 2) < textWidth * 1.5)
                    drawCircle = true;

                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                startCircleAnim();

                if (loading != null)
                    loading.startLoading();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    /**
     * 登陆失败后回置
     */
    public void reset() {
        clearAnimation();

        float scale = startHeight / (float) startWidth;
        ValueAnimator animator = ValueAnimator.ofFloat(scale, 1);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float flag = (float) animation.getAnimatedValue();
                marginLength = (int) (startWidth * (1 - flag)) / 2;
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                drawCircle = false;
                invalidate();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setClickable(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void startCircleAnim() {
        RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(duration * 2);
        ra.setInterpolator(new LinearInterpolator());
        ra.setRepeatCount(-1);
        ra.setFillAfter(true);
        startAnimation(ra);
    }

    public interface Loading {
        void startLoading();
    }
}
