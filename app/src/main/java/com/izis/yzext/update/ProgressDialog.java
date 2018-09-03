package com.izis.yzext.update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.izis.yzext.R;

public class ProgressDialog extends Dialog {

    private ProgressBar progressBar;
    private TextView tv_title;

    public ProgressDialog(@NonNull Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dlg_progress);
        tv_title = findViewById(R.id.tv_title);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);

//        getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        Window window = getWindow();
        WindowManager.LayoutParams params = null;
        if (window != null) {
            params = window.getAttributes();
            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            params.width = (int) (metrics.widthPixels * 0.9);
            window.setAttributes(params);
        }
    }

    public void updateProgress(int progress){
        if (progressBar!=null)
            progressBar.setProgress(progress);
//        if (tv_title!=null)
//            tv_title.setText("下载进度：" + progress + "%");
    }

    public int getProgress(){
        return progressBar!=null?progressBar.getProgress():0;
    }
}
