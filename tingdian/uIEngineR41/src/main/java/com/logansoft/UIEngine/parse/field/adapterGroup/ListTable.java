package com.logansoft.UIEngine.parse.field.adapterGroup;

import java.util.HashMap;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.Base.Interface.AdapterInterface;
import com.logansoft.UIEngine.Base.Interface.verticalScrollableInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.field.RefreshableListener;
import com.logansoft.UIEngine.parse.field.adapterGroup.adapter.XBaseAdapter;
import com.logansoft.UIEngine.parse.field.adapterGroup.adapter.adapterItemView;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.anim.AnimInterface;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.view.pullToRefresh.PullToRefreshListView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ListTable extends AdapterGroup implements
		RefreshableListener.refreshable, verticalScrollableInterface {
	public static final String ATTR_ON_INTERCEPT_TOUCH_EVENT = "interceptTouchEvent";
	private ListView mListView;
	//在xml中通过给isDividerHeight属性设置true,来隐藏分割线
	public static final String IS_DIVIDERHEIGHT = "isDividerHeight";
	
	public ListTable(BaseFragment fragment, GroupView parentView,
			Element element) {
		super(fragment, parentView, element);
	}

	@Override
	protected void createMyView() {
		mView = new PullToRefreshListView(mContext) {
			@Override
			public void draw(Canvas canvas) {
				bdraw(canvas);
				super.draw(canvas);
				adraw(canvas);
			}
		};
	}

	@Override
	protected void parseView() {
		super.parseView();
		PullToRefreshListView mPullToRefreshBase = (PullToRefreshListView) mView;
		mListView = mPullToRefreshBase.getRefreshableView();
		mListView.setVerticalScrollBarEnabled(false);
		mListView.setDivider(new ColorDrawable(0xffcccccc));
		mListView.setDividerHeight(1);
		String dividerHeight = attrMap.get(IS_DIVIDERHEIGHT);
		if ("true".equals(dividerHeight)) {
			mListView.setDividerHeight(0);
			LogUtil.e("取消ListView的分割线");
		}
		
		mListView.setCacheColorHint(0x00000000);
		mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		String ScrollBar = attrMap.get(ATTR_ON_INTERCEPT_TOUCH_EVENT);
		mListView.setVerticalScrollBarEnabled(BaseView.BOOLEANTRUE
				.equals(ScrollBar));
		mListView.setOnScrollListener(new ScrollListener());

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				parent.getItemAtPosition(position);
			}
		});

		new RefreshableListener(this);
		
		if(adapter != null&&((XBaseAdapter) adapter).getCount()>0){
			LogUtil.d("cyt", "count =- "+((XBaseAdapter) adapter).getCount());
			LogUtil.d("cyt", "itemtemplates =- "+((XBaseAdapter) adapter).getItemTemplates(0));
			LogUtil.d("cyt", "getitem =- "+((XBaseAdapter) adapter).getItem(0));
		}else{
			LogUtil.d("cyt", "count =- 0000000000000");
		}
	}

	@Override
	protected void parseAttributes() {
		super.parseAttributes();
		setDivisionColor(attrMap.get("divisionColor"));
		setDivisionHeight(attrMap.get("divisionHeight"));
	}

	@Override
	public AdapterInterface createAdapter() {
		return new XBaseAdapter(baseFragment, this);
	}

	@Override
	public void setAdapter(AdapterInterface adapter) {
		PullToRefreshListView mPullToRefreshBase = (PullToRefreshListView) mView;
		mPullToRefreshBase.setAdapter((XBaseAdapter) adapter);
		setOnDelete();
		if(adapter != null&&((XBaseAdapter) adapter).getCount()>0){
			LogUtil.d("cyt", "count =- "+((XBaseAdapter) adapter).getCount());
			LogUtil.d("cyt", "itemtemplates =- "+((XBaseAdapter) adapter).getItemTemplates(0));
			LogUtil.d("cyt", "getitem =- "+((XBaseAdapter) adapter).getItem(0));
		}else{
			LogUtil.d("cyt", "count =- 0000000000000");
		}
	}

	private void setOnDelete() {
		String onDelete = attrMap.get(GlobalConstants.ATTR_DELETE);
		if (!TextUtils.isEmpty(onDelete)) {
			((XBaseAdapter) adapter).setOnDelete(onDelete);
		}
	}

	public void setDivisionHeight(String divisionHeight) {
		if (!TextUtils.isEmpty(divisionHeight)) {
			int h = Integer.parseInt(divisionHeight);
			mListView.setDividerHeight(DisplayUtil.dip2px(mContext, h));
		}
	}

	private void setDivisionColor(String divisionColor) {
		if (!TextUtils.isEmpty(divisionColor)) {
			int c = UIEngineColorParser.getColor(divisionColor);
			ColorDrawable color = new ColorDrawable((int) c);
			mListView.setDivider(color);
		}
	}

	@Override
	public void setOnClick(final String luaStr) {
	}

	public void performItemClick(View convertView, int position, long id) {
		mListView.performItemClick(convertView, position, id);
	}

	@Override
	public void scrollToTop() {
		mListView.post(new Runnable() {
			@Override
			public void run() {
				if (mListView.getAdapter() != null
						&& mListView.getAdapter().getCount() > 0) {
					mListView.setSelection(0);
					mListView.setSelected(true);
				}
			}
		});
	}

	@Override
	public void scrollToBottom() {
		mListView.post(new Runnable() {
			@Override
			public void run() {
				if (mListView.getAdapter() != null
						&& mListView.getAdapter().getCount() > 0) {
					mListView
							.setSelection(mListView.getAdapter().getCount() - 1);
					mListView.setSelected(true);
				}
			}
		});
	}

	public boolean isDelete() {
		return ((XBaseAdapter) adapter).isDelete();
	}

	public void setDelete(boolean isDelete) {
		((XBaseAdapter) adapter).setDelete(isDelete);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		for (int i = 0; i < mListView.getChildCount(); i++) {
			View v = mListView.getChildAt(i);
			if (v instanceof adapterItemView) {
				((adapterItemView) v).onLowMemory();
			}
		}
	}

	@Override
	public void viewWillAppear() {
		super.viewWillAppear();
		for (int i = 0; i < mListView.getChildCount(); i++) {
			View v = mListView.getChildAt(i);
			if (v instanceof adapterItemView) {
				((adapterItemView) v).viewWillAppear();
			}
		}
	}

	@Override
	public void viewDidDisappear() {
		super.viewDidDisappear();
		for (int i = 0; i < mListView.getChildCount(); i++) {
			View v = mListView.getChildAt(i);
			if (v instanceof adapterItemView) {
				((adapterItemView) v).viewDidDisappear();
			}
		}
	}

	@Override
	public void Destory() {
		for (int i = 0; i < mListView.getChildCount(); i++) {
			View v = mListView.getChildAt(i);
			if (v instanceof adapterItemView) {
				((adapterItemView) v).Destory();
			}
		}
		mListView = null;
		super.Destory();
	}

	public void scrollToSelect(final int position) {
		mListView.post(new Runnable() {
			@Override
			public void run() {
				if (mListView.getAdapter() != null
						&& mListView.getAdapter().getCount() > 0) {
					mListView.setSelection(position);
					mListView.setSelected(true);
				}
			}
		});
	}

	private boolean scrollFlag = false;// 标记是否滑动
	private int lastVisibleItemPosition = 0;// 标记上次滑动位置

	class ScrollListener implements AbsListView.OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			switch (scrollState) {
			// 当不滚动时
			case OnScrollListener.SCROLL_STATE_IDLE:// 是当屏幕停止滚动时
				scrollFlag = false;
				// 判断滚动到底部
				if (mListView.getLastVisiblePosition() == (mListView.getCount() - 1)) {
				}
				// 判断滚动到顶部
				if (mListView.getFirstVisiblePosition() == 0) {
				}

				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 滚动时
				scrollFlag = true;
				break;
			case OnScrollListener.SCROLL_STATE_FLING:// 是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
				scrollFlag = false;
				break;
			}
		}

		/**
		 * firstVisibleItem：当前能看见的第一个列表项ID（从0开始）
		 * visibleItemCount：当前能看见的列表项个数（小半个也算） totalItemCount：列表项共数
		 */
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

			if (lastVisibleItemPosition != mListView.getFirstVisiblePosition()) {
				String luaStr = attrMap.get("onScrollStateChanged");
				if (!StringUtil.isEmpty(luaStr)) {

					String position = mListView.getFirstVisiblePosition() + "";
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("position", position);

					baseFragment.loadLua(null, luaStr, map);
				}

				lastVisibleItemPosition = mListView.getFirstVisiblePosition();
			}

		}

	}

	public XBaseAdapter getAdapter() {
		return (XBaseAdapter) adapter;
	}

	public Object getItemByPosition(int position) {
		return ((XBaseAdapter) adapter).getItem(position);
	}
}
