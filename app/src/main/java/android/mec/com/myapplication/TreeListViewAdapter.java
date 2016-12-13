package android.mec.com.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * http://blog.csdn.net/lmj623565791/article/details/40212367
 *
 * @param <T>
 * @author zhy
 */
public abstract class TreeListViewAdapter<T> extends BaseAdapter {

    protected Context mContext;
    /**
     * 存储所有可见的Node
     */
    protected List<Node> mNodes;
    protected LayoutInflater mInflater;
    /**
     * 存储所有的Node
     */
    protected List<Node> mAllNodes;

    /**
     * 点击的回调接口
     */
    private OnTreeNodeClickListener onTreeNodeClickListener;

    public interface OnTreeNodeClickListener {
        void onClick(Node node, int position);
    }

    public void setOnTreeNodeClickListener(OnTreeNodeClickListener onTreeNodeClickListener) {
        this.onTreeNodeClickListener = onTreeNodeClickListener;
    }

    /**
     * @param mTree
     * @param context
     * @param datas
     * @param defaultExpandLevel 默认展开几级树
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public TreeListViewAdapter(ListView mTree, Context context, List<T> datas, int defaultExpandLevel) throws IllegalArgumentException, IllegalAccessException {
        mContext = context;
        /**
         * 对所有的Node进行排序
         */
        mAllNodes = TreeHelper.getSortedNodes(datas, defaultExpandLevel);
        /**
         * 过滤出可见的Node
         */
        mNodes = TreeHelper.filterVisibleNode(mAllNodes);
        mInflater = LayoutInflater.from(context);

        /**
         * 设置节点点击时，可以展开以及关闭；并且将ItemClick事件继续往外公布
         */
        mTree.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                expandOrCollapse(position);

                if (onTreeNodeClickListener != null) {
                    onTreeNodeClickListener.onClick(mNodes.get(position), position);
                }
            }

        });
    }

    /**
     * 相应ListView的点击事件 展开或关闭某节点
     *
     * @param position
     */
    public void expandOrCollapse(int position) {
        Node n = mNodes.get(position);

        if (n != null)// 排除传入参数错误异常
        {
            if (!n.isLeaf()) {
                n.setExpand(!n.isExpand());
                mNodes = TreeHelper.filterVisibleNode(mAllNodes);
                notifyDataSetChanged();// 刷新视图
            }
        }
    }

    /*
    * 选择按钮点击
    *
    */
    public void SelectClick(Node node, int position) {
        //如果是跟节点
        if (node.isRoot()) {
            if (node.getIsSelected() == 0 || node.getIsSelected() == 1) {
                node.setIsSelected(2);
                SelectClickChild(node, node.getIsSelected());
            } else {
                if (node.getIsSelected() == 2) {
                    node.setIsSelected(0);
                    SelectClickChild(node, node.getIsSelected());
                }
            }
        } else {
            //如果是叶子节点
            if (node.isLeaf()) {
                //子节点未选中-选中,判断平级是否全部选中,设置父节点状态
                if (node.getIsSelected() == 0) {
                    node.setIsSelected(2);
                    //判断是否全部选中
                    if (isSelectAll(node)) {
                        SelectClickParent(node, 2);
                    } else {
                        SelectClickParent(node, 1);
                    }
                } else {
                    //子节点选中-未选中,判断平级是否全部未选中,设置父节点状态
                    if (node.getIsSelected() == 2) {
                        node.setIsSelected(0);
                        if (isSelectAll2(node)) {
                            SelectClickParent(node, 0);
                        } else {
                            SelectClickParent(node, 1);
                        }
                    }
                }
            } else {

                if (node.getIsSelected() == 0 || node.getIsSelected() == 1) {
                    node.setIsSelected(2);
                    SelectClickChild(node, node.getIsSelected());

                    //判断是否全部选中
                    if (isSelectAll(node)) {
                        SelectClickParent(node, 2);
                    } else {
                        SelectClickParent(node, 1);
                    }
                } else {
                    if (node.getIsSelected() == 2) {
                        node.setIsSelected(0);
                        SelectClickChild(node, node.getIsSelected());

                        if (isSelectAll2(node)) {
                            SelectClickParent(node, 0);
                        } else {
                            SelectClickParent(node, 1);
                        }
                    }
                }

            }


        }
        setRoot();
        notifyDataSetChanged();// 刷新视图
    }

    /*
    *设置父节点状态
    **/
    public void setRoot(){
        for (Node n1:mNodes){
            if(n1.isRoot()){

                //全部选中
                if(!isSeletLeaf() && !isSeletLeaf2()){
                    n1.setIsSelected(1);
                }else {
                    if(isSeletLeaf()){
                        n1.setIsSelected(2);
                    }
                    if(isSeletLeaf2()){
                        n1.setIsSelected(0);
                    }
                }
                break;
            }
        }
    }
    /**
     * 父节点
     */
    public void SelectClickParent(Node node, int selectTag) {
        if (node.isRoot()) {
            return;
        }
        Node n = node.getParent();
        if (n!= null && !n.isRoot())// 排除传入参数错误异常
        {
            n.setIsSelected(selectTag);

            //全部非未选中和非选中
            if (!isSelectAll2(n) && !isSelectAll(n)) {
                SelectClickParent(n, 1);
            } else {
                //全部选中
                if (isSelectAll(n)) {
                    SelectClickParent(n, 2);
                }
                //全部未选中
                if (isSelectAll2(n)) {
                    SelectClickParent(n, 0);
                }
            }
            //如果到根节点,遍历所有叶子节点
        }
    }



    /**
     * 相应ListView的点击事件 设置所有子节点选中/不选中
     */
    public void SelectClickChild(Node node, int selectTag) {
        List<Node> list = node.getChildren();
        for (int i = 0; i < node.getChildren().size(); i++) {
            Node n = list.get(i);
            n.setIsSelected(selectTag);
            if (!n.isLeaf()) {
                SelectClickChild(n, selectTag);
            }
        }
    }

    //判断所有的叶子节点 是否全部选中
    public boolean isSeletLeaf(){
        for (int i = 0; i < mAllNodes.size(); i++) {
            Node n1 = mAllNodes.get(i);
           int tag = n1.getIsSelected();
            if (tag == 0) {
                return false;
            }
        }
        return true;
    }


    //判断所有的叶子节点 是否全部未选中
    public boolean isSeletLeaf2(){

        for (int i = 0; i < mAllNodes.size(); i++) {
            Node n1 = mAllNodes.get(i);
            if (n1.getIsSelected() == 2) {
                return false;
            }
        }
        return true;
    }


    //判断平级是否全部选中
    public boolean isSelectAll(Node node) {
        List<Node> list = node.getParent().getChildren();
        for (int i = 0; i < list.size(); i++) {
            Node n1 = list.get(i);
            if (n1.getIsSelected() != 2) {
                return false;
            }
        }
        return true;
    }

    //判断平级是否全部未选中
    public boolean isSelectAll2(Node node) {
        List<Node> list = node.getParent().getChildren();
        for (int i = 0; i < list.size(); i++) {
            Node n1 = list.get(i);
            if (n1.getIsSelected() != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getCount() {
        return mNodes.size();
    }

    @Override
    public Object getItem(int position) {
        return mNodes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Node node = mNodes.get(position);
        convertView = getConvertView(node, position, convertView, parent);
        // 设置内边距
        convertView.setPadding(node.getLevel() * 30, 3, 3, 3);
        return convertView;
    }

    public abstract View getConvertView(Node node, int position,
                                        View convertView, ViewGroup parent);

}
