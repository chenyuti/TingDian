package com.logansoft.UIEngine.parse.field.adapterGroup.adapter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.BaseViewFactory;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class XBaseAdapter extends BaseAdapter implements AdapterInterface {
	Context mContext;
	BaseFragment fragment;
	GroupView groupView;
	boolean isDelete = false;
	protected String onDelete = "";

	public XBaseAdapter(BaseFragment fragment, GroupView groupView) {
		this.fragment = fragment;
		mContext = groupView.mContext;
		this.groupView = groupView;
	}

	public void setOnDelete(String deleteLua) {
		onDelete = deleteLua;
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		UIEngineEntity entity = groupView.getEntity();
		if (entity != null)
			return entity.getTemplateData().size();
		return 0;
	}

	@Override
	public UIEngineEntity getItem(int position) {
		UIEngineEntity entity = groupView.getEntity();
		if (entity != null && entity.getTemplateData().size() > position)
			return entity.getTemplateData().get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		UIEngineEntity entity = getItem(position);
		Element templateXML = getItemTemplates(position);
		if (templateXML == null) {
			if (convertView == null) {
				convertView = new adapterItemView(mContext);
			}
			return convertView;
		}
		BaseView contentViewController = null;
		if (convertView == null || !(convertView instanceof adapterItemView)) {
			adapterItemView itemView = new adapterItemView(mContext);
			final BaseView bv = BaseViewFactory.newInstance(fragment,
					groupView, templateXML);
			contentViewController = bv;
			convertView = itemView;
			itemView.setBaseView(bv);

			convertView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (v instanceof adapterItemView) {
						adapterItemView aiv = (adapterItemView) v;
						BaseView bv = aiv.getBaseView();
						if (bv == null)
							return false;
						HashMap<String, String> att = bv.getAttributes();
						String onLongClick = att.get("onLongClick");
						if (onLongClick == null)
							return false;
						int position = Integer.parseInt(att.get("position"));
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("position", position);
						bv.executeLua(onLongClick, map);
						return true;
					}
					return false;
				}
			});
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v instanceof adapterItemView) {
						adapterItemView aiv = (adapterItemView) v;
						BaseView bv = aiv.getBaseView();
						if (bv == null)
							return;
						HashMap<String, String> att = bv.getAttributes();
						String onItemClick = att.get("onItemClick");
						// String onChildItemClick =
						// att.get("onChildItemClick");

						onItemClick(onItemClick, bv, att);
					}
				}
			});
			contentViewController.getView().setLongClickable(false);
		} else {
			contentViewController = ((adapterItemView) convertView)
					.getBaseView();
		}
		contentViewController.getAttributes().put("position", "" + position);
		contentViewController.upDateEntity(entity);
		contentViewController.setSelected(entity.isSelected());

		int[] margin = contentViewController.getmMargin();
		if (margin.length == 4)
			convertView.setPadding(margin[0], margin[1], margin[2], margin[3]);
		else
			convertView.setPadding(0, 0, 0, 0);

		/**
		 * 判断删除模式是不是true 如果为true 显示左侧删除按钮并且为它增加点击事件 点击事件-> 在holder内增加
		 * 移除标示,用来控制是否显示右边删除按钮,并增加或移除右侧删除按钮监听器 否则 隐藏删除按钮,并移除点击事件
		 */
		if (isDelete) {
			int DEL_TXET_ID = 0xff0002;
			LinearLayout linearLayout = (LinearLayout) convertView;
			TextView tv = (TextView) linearLayout.findViewById(DEL_TXET_ID);
			if (tv == null) {
				ImageView view = new ImageView(mContext);
				tv = new TextView(mContext);
				tv.setId(DEL_TXET_ID);

				view.setVisibility(View.VISIBLE);
				tv.setVisibility(View.GONE);

				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						contentViewController.getView().getLayoutParams());
				params.width = 0;
				params.weight = 1;
				contentViewController.getView().setLayoutParams(params);

				linearLayout.addView(view, 0);
				linearLayout.addView(tv);

				int p = DisplayUtil.dip2px(mContext, 10);

				LinearLayout.LayoutParams paramsView = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				view.setPadding(DisplayUtil.dip2px(mContext, 15), 0,
						DisplayUtil.dip2px(mContext, 5), 0);
				view.setLayoutParams(paramsView);
				view.setImageResource(R.drawable.list_item_del);

				LinearLayout.LayoutParams paramsTV = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				tv.setGravity(Gravity.CENTER);
				tv.setTextColor(Color.WHITE);
				tv.setTextSize(15);
				tv.setText("删除");
				tv.setBackgroundColor(0xFFFF3030);
				tv.setLayoutParams(paramsTV);
				tv.setPadding(p, 0, p, 0);
				view.setTag(tv);
				tv.setTag(entity);
				entity.setSubEntityValue("isOpen", false);
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						View view = (View) v.getTag();
						UIEngineEntity entity = (UIEngineEntity) view.getTag();
						if (view.getVisibility() == View.VISIBLE) {
							view.setVisibility(View.GONE);
							entity.setSubEntityValue("isOpen", false);
						} else {
							view.setVisibility(View.VISIBLE);
							entity.setSubEntityValue("isOpen", true);
						}
					}
				});
				tv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						UIEngineEntity entity = (UIEngineEntity) v.getTag();
						UIEngineEntity adapterEntity = groupView.getEntity();
						adapterEntity.getTemplateData().remove(entity);
						if (!TextUtils.isEmpty(onDelete)) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("element", entity);
							groupView.executeLua(onDelete, map);
						}
						adapterEntity.reloadTemplateData();
					}
				});
			} else {
				tv.setTag(entity);
			}

			Object isOpen = entity.getSubEntityValue("isOpen");
			if (isOpen != null && isOpen.equals(true)) {
				tv.setVisibility(View.VISIBLE);
			} else {
				tv.setVisibility(View.GONE);
			}
		} else {
			LinearLayout linearLayout = (LinearLayout) convertView;
			if (linearLayout.getChildCount() == 3) {
				linearLayout.removeViewAt(2);
				linearLayout.removeViewAt(0);
			}

		}

		ViewGroup.LayoutParams clp = contentViewController.getView()
				.getLayoutParams();
		ViewGroup.LayoutParams alp = convertView.getLayoutParams();
		if (alp == null) {
			alp = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			convertView.setLayoutParams(alp);
		}
		if (clp.width == LayoutParams.MATCH_PARENT)
			alp.width = LayoutParams.MATCH_PARENT;
		if (clp.height == LayoutParams.MATCH_PARENT)
			alp.height = groupView.getView().getHeight();

		return convertView;
	}

	public String getItemStyle(int position) {
		if (groupView.Templates != null) {
			UIEngineEntity entity = groupView.getEntity();
			UIEngineEntity entityItem = getItem(position);
			String style = entityItem.getStyle();
			if (style == null || style.length() == 0)
				style = entity.getTemplateDataStyle();
			if (style == null || style.length() == 0)
				style = GlobalConstants.ATTR_STYLE_DEFAULT;
			return style;
		}
		return null;
	}

	@Override
	public int getItemViewType(int position) {
		if (groupView.Templates != null) {
			String style = getItemStyle(position);
			if (StringUtil.isEmpty(style)) {
				return super.getItemViewType(position);
			} else {
				Iterator<Map.Entry<String, Element>> it = groupView.Templates
						.entrySet().iterator();
				int index = 0;
				while (it.hasNext()) {
					Map.Entry<String, Element> entry = it.next();
					String key = entry.getKey();
					if (style.equals(key)) {
						return index;
					}
					index++;
				}
			}
		}
		return super.getItemViewType(position);
	}

	public Element getItemTemplates(int position) {
		String style = getItemStyle(position);
		if (style != null && groupView.Templates != null) {
			return groupView.Templates.get(style);
		}
		return null;
	}

	/**
	 * 返回所有的layout的数量
	 */
	@Override
	public int getViewTypeCount() {
		if (groupView.Templates != null) {
			return groupView.Templates.size();
		} else {
			return super.getViewTypeCount();
		}
	};

	private void onItemClick(String s, BaseView bv, HashMap<String, String> att) {
		if (!TextUtils.isEmpty(s)) {
			int position = Integer.parseInt(att.get("position"));
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("position", position);
			bv.executeLua(s, map);
			LogUtil.d("cyt", "bv in item = " + bv.getEntity());
		} else {
			// 给整个item里面的内容的点击事件设置值
			// putAttrToChildView(bv);
		}
	}

}
