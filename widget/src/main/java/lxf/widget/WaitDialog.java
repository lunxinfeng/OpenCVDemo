package lxf.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import lxf.widget.avloadingview.AVLoadingIndicatorView;
import lxf.widget.avloadingview.AvLoadingIndicator;


/**
 * 网络加载数据时的等待对话框
 * Created by lxf on 2016/5/17.
 */
public class WaitDialog extends ProgressDialog {
    private TextView tv_msg;
    /**
     * 内容提示
     */
    private String msg;
    private AVLoadingIndicatorView avLoadingView;
    /**
     * 动画类别
     */
    private String loadingIndicator = AvLoadingIndicator.BallClipRotatePulseIndicator;

    public WaitDialog(Context context) {
        super(context);
        init();
    }


    public WaitDialog(Context context, int theme) {
        super(context, theme);
        init();
    }

    public WaitDialog(Context context, int theme, String msg) {
        super(context, theme);
        init();
        this.msg = msg;
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    public void setMsg(String msg) {
        this.msg = msg;
        tv_msg.setText(msg);
    }

    public void setLoadingIndicator(String loadingIndicator) {
        this.loadingIndicator = loadingIndicator;
        avLoadingView.setIndicator(loadingIndicator);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout_common);
        tv_msg = (TextView) findViewById(R.id.dialog_tv);
//        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/shaonv.ttc");
//        tv_msg.setTypeface(typeface);
        if (!TextUtils.isEmpty(msg)) {
            tv_msg.setText(msg);
        }
        avLoadingView = (AVLoadingIndicatorView) findViewById(R.id.avloadingview);
        avLoadingView.setIndicator(loadingIndicator);

        Window window = getWindow();
        WindowManager.LayoutParams params = null;
        if (window != null) {
            params = window.getAttributes();
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            params.width = (int) (metrics.widthPixels * 0.6);
            window.setAttributes(params);
        }

    }

    public void show() {
        super.show();
    }

    public void dismiss() {
        if (isShowing()) {
            super.dismiss();
        }
    }
}
