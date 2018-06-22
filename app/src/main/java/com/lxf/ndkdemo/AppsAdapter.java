package com.lxf.ndkdemo;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * 应用图标适配器
 * Created by lxf on 18-4-22.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.MyViewHolder> {
    private List<ResolveInfo> mApps;
    private PackageManager pm;
    private OnItemClickListener onItemClickListener;

    AppsAdapter(List<ResolveInfo> mApps, PackageManager pm) {
        this.mApps = mApps;
        this.pm = pm;
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_apps, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(mApps.get((Integer) v.getTag()));
            }
        });
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ResolveInfo info = mApps.get(position);
        switch (info.activityInfo.packageName) {
//            case "com.lxf.folder":
//                holder.icon.setImageResource(R.mipmap.icon_folder);
//                break;
//            case "com.android.settings":
//                holder.icon.setImageResource(R.mipmap.icon_setting);
//                break;
//            case "com.android.browser":
//                holder.icon.setImageResource(R.mipmap.icon_browser);
//                break;
//            case "com.android.providers.downloads.ui":
//                holder.icon.setImageResource(R.mipmap.icon_download);
//                break;
//            case "com.estrongs.android.pop":
//                holder.icon.setImageResource(R.mipmap.icon_files);
//                break;
            default:
                holder.icon.setImageDrawable(info.loadIcon(pm));
                break;
        }
//        if (info.activityInfo.packageName.equals("com.lxf.folder"))
//            holder.name.setText("系统工具");
//        else
            holder.name.setText(info.loadLabel(pm));

        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mApps == null ? 0 : mApps.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        MyViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ResolveInfo resolveInfo);
    }
}
