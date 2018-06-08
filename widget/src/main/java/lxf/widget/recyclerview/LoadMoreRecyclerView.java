package lxf.widget.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 自动加载更多的RecyclerView
 * Created by lxf on 2016/11/10.
 */
public class LoadMoreRecyclerView extends RecyclerView {

    public static final int LinearLayoutManager = 1;
    public static final int GridLayoutManager = 2;
    public static final int StaggeredGridLayoutManager = 3;

    /**
     * 布局类型
     */
    private int layoutManagerType;
    private int[] lastPositions;
    /**
     * 可见的最后一条item的position
     */
    private int lastVisibleItemPosition;

    private LoadMoreListener loadMoreListener;

    /**
     * 是否上滑
     */
    private boolean slideUp;
    private float startY;

    /**
     * 选中的item的position
     */
//    private int selectedPosition;

    public LoadMoreRecyclerView(Context context) {
        super(context);
        init();
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if (slideUp && (visibleItemCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE && (lastVisibleItemPosition) >= totalItemCount - 1)) {
                    if (loadMoreListener != null)
                        loadMoreListener.onLoadMore(recyclerView);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                if (layoutManagerType == 0) {
                    if (layoutManager instanceof android.support.v7.widget.GridLayoutManager) {
                        layoutManagerType = GridLayoutManager;
                    } else if (layoutManager instanceof android.support.v7.widget.StaggeredGridLayoutManager) {
                        layoutManagerType = StaggeredGridLayoutManager;
                    } else if (layoutManager instanceof android.support.v7.widget.LinearLayoutManager) {
                        layoutManagerType = LinearLayoutManager;
                    } else {
                        throw new RuntimeException("未知的LayoutManager");
                    }
                }

                switch (layoutManagerType) {
                    case LinearLayoutManager:
                        lastVisibleItemPosition = ((android.support.v7.widget.LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                        break;
                    case GridLayoutManager:
                        lastVisibleItemPosition = ((android.support.v7.widget.GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                        break;
                    case StaggeredGridLayoutManager:
                        android.support.v7.widget.StaggeredGridLayoutManager staggeredGridLayoutManager = (android.support.v7.widget.StaggeredGridLayoutManager) layoutManager;
                        if (lastPositions == null) {
                            lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                        }
                        staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                        lastVisibleItemPosition = findMax(lastPositions);
                        break;
                }
            }
        });

//        setChildrenDrawingOrderEnabled(true);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public interface LoadMoreListener {
        void onLoadMore(RecyclerView recyclerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = e.getY();
                break;
            case MotionEvent.ACTION_UP:
                slideUp = e.getY() < startY;
                break;
        }
        return super.onTouchEvent(e);
    }

//    //-----------------------------------------item放大相关逻辑
//    @Override
//    public void onDraw(Canvas c) {
//        selectedPosition = getChildAdapterPosition(getFocusedChild());
//        super.onDraw(c);
//    }
//
//    @Override
//    protected int getChildDrawingOrder(int childCount, int i) {
////        if (i == selectedPosition)
////            return childCount - 1;
////        if (i == childCount - 1)
////            return selectedPosition < 0 ? super.getChildDrawingOrder(childCount, i) : selectedPosition;
////        return super.getChildDrawingOrder(childCount, i);
//
//        int position = selectedPosition;
//        if (position < 0) {
//            return i;
//        } else {
//            if (i == childCount - 1) {
//                if (position > i) {
//                    position = i;
//                }
//                return position;
//            }
//            if (i == position) {
//                return childCount - 1;
//            }
//        }
//        return i;
//    }
}
