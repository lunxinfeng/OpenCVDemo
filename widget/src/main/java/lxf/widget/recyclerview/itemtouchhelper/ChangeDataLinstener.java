package lxf.widget.recyclerview.itemtouchhelper;

/**
 * Created by lxf on 2016/5/10.
 */
public interface ChangeDataLinstener {
    /**
     * 交换两个item对象
     * @param from
     * @param to
     */
    void swap(int from, int to);

    /**
     * 删除item
     * @param position
     */
    void del(int position);
}
