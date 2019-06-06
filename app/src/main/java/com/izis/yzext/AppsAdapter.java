package com.izis.yzext;

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
        if (info.activityInfo !=null){
            holder.icon.setImageDrawable(info.loadIcon(pm));
            holder.name.setText(info.loadLabel(pm));
        }else{
            switch (info.match){
                case 1:
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                    holder.name.setText("弈客围棋");
                    break;
                case 2:
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                    holder.name.setText("腾讯围棋");
                    break;
                case 3:
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                    holder.name.setText("弈城围棋");
                    break;
                case 4:
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                    holder.name.setText("99围棋");
                    break;
                case 5:
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                    holder.name.setText("新博围棋");
                    break;
                case 6:
                    holder.icon.setImageResource(R.mipmap.ic_launcher);
                    holder.name.setText("隐智围棋");
                    break;
            }
        }


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
