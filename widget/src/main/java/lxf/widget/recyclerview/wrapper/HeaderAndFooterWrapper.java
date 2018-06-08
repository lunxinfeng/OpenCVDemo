package lxf.widget.recyclerview.wrapper;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import lxf.widget.recyclerview.adapter.RecyclerViewHolder;


/**
 * 使用方式：
 *      wrapper = new HeaderAndFooterWrapper(mAdapter);//传入原有的RecyclerView。Adapter
 *      wrapper.addHeaderView(tv);
 *      recyclerview.setAdapter(wrapper);
 *
 * Created by lxf2016 on 2016/8/25.
 */
public class HeaderAndFooterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int BASE_ITEM_HEADER = 10000;
    private static final int BASE_ITEM_FOTTER = 20000;
    private SparseArrayCompat<View> mHeaders = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFooters = new SparseArrayCompat<>();
    private RecyclerView.Adapter mInnerAdapter;

    public HeaderAndFooterWrapper(RecyclerView.Adapter innerAdapter) {
        mInnerAdapter = innerAdapter;
    }

    /**
     * 获取头部视图的数量
     */
    public int getHeadersCount() {
        return mHeaders.size();
    }

    /**
     * 获取底部视图的数量
     */
    public int getFootersCount() {
        return mFooters.size();
    }

    /**
     * 获取item的数量
     */
    private int getRealItemCount() {
        return mInnerAdapter.getItemCount();
    }

    private boolean isHeaderView(int position) {
        return position < getHeadersCount();
    }

    private boolean isFooterView(int position) {
        return position >= getHeadersCount() + getRealItemCount();
    }

    /**
     * 增加一个头部视图
     */
    public void addHeaderView(View view) {
        mHeaders.put(BASE_ITEM_HEADER + getHeadersCount(), view);
    }

    /**
     * 增加一个底部视图
     */
    public void addFooterView(View view) {
        mFooters.put(BASE_ITEM_FOTTER + getFootersCount(), view);
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)) {
            return mHeaders.keyAt(position);
        } else if (isFooterView(position)) {
            return mFooters.keyAt(position - getHeadersCount() - getRealItemCount());
        }
        return mInnerAdapter.getItemViewType(position - getHeadersCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaders.get(viewType) != null) {
            RecyclerView.ViewHolder holder = RecyclerViewHolder.createViewHolder(parent.getContext(),mHeaders.get(viewType));
            return holder;
        } else if (mFooters.get(viewType) != null) {
            RecyclerView.ViewHolder holder = RecyclerViewHolder.createViewHolder(parent.getContext(),mFooters.get(viewType));
            return holder;
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderView(position))
        {
            return;
        }
        if (isFooterView(position))
        {
            return;
        }
        mInnerAdapter.onBindViewHolder(holder, position - getHeadersCount());
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + getRealItemCount();
    }


    //重写该方法完成对GridLayoutManager的处理，使每行只有一个header或footer
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        WrapperUtils.onAttachedToRecyclerView(mInnerAdapter, recyclerView, new WrapperUtils.SpanSizeCallback()
        {
            @Override
            public int getSpanSize(GridLayoutManager layoutManager, GridLayoutManager.SpanSizeLookup oldLookup, int position)
            {
                int viewType = getItemViewType(position);
                if (mHeaders.get(viewType) != null)
                {
                    return layoutManager.getSpanCount();
                } else if (mFooters.get(viewType) != null)
                {
                    return layoutManager.getSpanCount();
                }
                if (oldLookup != null)
                    return oldLookup.getSpanSize(position);
                return 1;
            }
        });
    }

    //重写该方法完成对StaggeredGridLayoutManager的处理
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder)
    {
        mInnerAdapter.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isHeaderView(position) || isFooterView(position))
        {
            WrapperUtils.setFullSpan(holder);
        }
    }

}
