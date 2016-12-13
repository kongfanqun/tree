package android.mec.com.myapplication;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class GisTreeAdapter<T> extends TreeListViewAdapter<T>
{
	//定义选择按钮外调用
	private GisTreeSelectListener gisTreeSelectListener;
	public interface GisTreeSelectListener
	{
		void onClick(Node node, int position);
	}

	public void setGisTreeSelect(GisTreeSelectListener gisTreeSelectListener)
	{
		this.gisTreeSelectListener = gisTreeSelectListener;
	}

	public GisTreeAdapter(ListView mTree, Context context, List<T> datas,int defaultExpandLevel) throws IllegalArgumentException,IllegalAccessException
	{
		super(mTree, context, datas, defaultExpandLevel);
	}

	@Override
	public View getConvertView(Node node , int position, View convertView, ViewGroup parent)
	{
		final Node  node1 = node;
		final int  position1 = position;
		ViewHolder viewHolder = null;
		if (convertView == null)
		{
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView.findViewById(R.id.id_treenode_icon);
			viewHolder.label = (TextView) convertView.findViewById(R.id.id_treenode_label);
			viewHolder.isSelecte = (TextView) convertView.findViewById(R.id.SelectState);
			viewHolder.isSelecte.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (gisTreeSelectListener != null)
					{
						gisTreeSelectListener.onClick(node1,position1);
					}

					//调用父类中的方法,刷新数据在父类
					SelectClick(node1,position1);
				}
			});
			convertView.setTag(viewHolder);

		} else
		{
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (node.getIcon() == -1)
		{
			viewHolder.icon.setVisibility(View.INVISIBLE);
		} else
		{
			viewHolder.icon.setVisibility(View.VISIBLE);
			viewHolder.icon.setImageResource(node.getIcon());
		}

		switch (node.getIsSelected()){
			case 0:
				viewHolder.isSelecte.setBackground(ContextCompat.getDrawable(parent.getContext(),R.mipmap.select_no));
				break;
			case 1:
				viewHolder.isSelecte.setBackground(ContextCompat.getDrawable(parent.getContext(),R.mipmap.select));
				break;
			case 2:
				viewHolder.isSelecte.setBackground(ContextCompat.getDrawable(parent.getContext(),R.mipmap.checkmark));
				break;

		}
		viewHolder.label.setText(node.getName());
		
		
		return convertView;
	}

	private final class ViewHolder
	{
		ImageView icon;
		TextView label;
		TextView isSelecte;
	}


}
