package lxf.widget.recyclerview.itemtouchhelper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * 长按交换item位置，滑动删除item
 * Created by lxf on 2016/5/10.
 */
public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private ChangeDataLinstener changeDataLinstener;

    public MyItemTouchHelperCallback(ChangeDataLinstener changeDataLinstener) {
        this.changeDataLinstener = changeDataLinstener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;//可以上下移动
        int swipeFlags = ItemTouchHelper.LEFT;//可以左滑
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    //监听上下移动
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        changeDataLinstener.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());

        //返回true表示已经响应了移动，若是返回false，则表示没有响应移动，选中的item会继续和下一个item交换位置，直到移不动为止。
        return true;
    }

    //监听左右滑动
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        changeDataLinstener.del(viewHolder.getAdapterPosition());
    }

}
