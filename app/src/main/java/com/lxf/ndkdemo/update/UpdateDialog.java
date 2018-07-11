package com.lxf.ndkdemo.update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lxf.ndkdemo.R;


/**
 * app更新对话框
 * Created by lxf on 2016/8/17.
 */
public class UpdateDialog extends Dialog implements View.OnClickListener {
    private String info;
    private ClickListener mClickListener;

    public UpdateDialog(Context context, int themeResId,String info) {
        super(context, themeResId);
        setCancelable(false);
        this.info = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_update);
        TextView tv_update_info = findViewById(R.id.tvInfo);
        tv_update_info.setText(info);
        Button btnY = findViewById(R.id.btnY);
        btnY.setOnClickListener(this);
        btnY.requestFocus();
        Button btnN = findViewById(R.id.btnN);
        btnN.setOnClickListener(this);
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnY:
                dismiss();
                if (mClickListener!=null)
                    mClickListener.doUpdate();
                break;
            case R.id.btnN:
                dismiss();
                if (mClickListener!=null)
                    mClickListener.doCancel();
                break;
        }
    }

    public interface ClickListener{
        void doUpdate();
        void doCancel();
    }
}
