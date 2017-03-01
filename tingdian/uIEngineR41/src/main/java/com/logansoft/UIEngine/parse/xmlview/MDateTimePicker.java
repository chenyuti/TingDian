package com.logansoft.UIEngine.parse.xmlview;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.logansoft.UIEngine.R;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.Text.MLabel;
import com.logansoft.UIEngine.view.wheelView.OnWheelChangedListener;
import com.logansoft.UIEngine.view.wheelView.OnWheelScrollListener;
import com.logansoft.UIEngine.view.wheelView.WheelView;
import com.logansoft.UIEngine.view.wheelView.adapter.NumericWheelAdapter;

public class MDateTimePicker extends MLabel {
	public static final String ATTR_FORMAT = "timeFormat"; //时间格式
	public static final String ATTR_DONE = "doneClick";
	public static final String ATTR_BEFORENOW = "BeforeNow"; //before为可以选择以前的时间，now为只能选择当前时间，默认效果为before

	private WheelView year;
	private WheelView month;
	private WheelView day;
	private WheelView hour;
	private WheelView min;
	int beforenowYear = 100;
	private PopupWindow menuWindow;
	private View view;

	public MDateTimePicker(BaseFragment fragment, GroupView parentView, Element element) {
		// TODO Auto-generated constructor stub
		super(fragment, parentView, element);
		initView();
		onClick();
	}

	public void onClick(){
		mView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				menuWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
			}
		});
	}

	public void initView(){
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(baseFragment.getActivity().LAYOUT_INFLATER_SERVICE);
		view= inflater.inflate(R.layout.datepicker_wheelview, null);
		menuWindow = new PopupWindow(view,LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT); //后两个参数是width和height
		menuWindow.setAnimationStyle(R.style.popDownAnim);
		Calendar c = Calendar.getInstance();
		final int curYear = c.get(Calendar.YEAR);
		final int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
		final int curDay = c.get(Calendar.DAY_OF_MONTH);
		final int curHour = c.get(Calendar.HOUR_OF_DAY);
		final int curMin = c.get(Calendar.MINUTE);

		menuWindow.setFocusable(true);
		menuWindow.setOutsideTouchable(true);
		menuWindow.update();
		menuWindow.setBackgroundDrawable(new BitmapDrawable());

		String beforenow=attrMap.get(ATTR_BEFORENOW);
		if (beforenow != null) {
			if (beforenow.equals("before")) {
				beforenowYear=100;
			} else if (beforenow.equals("now")) {
				beforenowYear=0;
			}
		}

		year = (WheelView) view.findViewById(R.id.year);
		NumericWheelAdapter numericWheelAdapter1=new NumericWheelAdapter(mContext,curYear-beforenowYear, curYear+100);
		numericWheelAdapter1.setLabel("年");
		year.setViewAdapter(numericWheelAdapter1);
		year.setCyclic(true);//是否可循环滑动
		year.addScrollingListener(scrollListener);

		month = (WheelView) view.findViewById(R.id.month);
		NumericWheelAdapter numericWheelAdapter2=new NumericWheelAdapter(mContext,1, 12, "%02d");
		numericWheelAdapter2.setLabel("月");
		month.setViewAdapter(numericWheelAdapter2);
		month.setCyclic(true);
		month.addScrollingListener(scrollListener);

		day = (WheelView) view.findViewById(R.id.day);
		initDay(curYear,curMonth);
		day.setCyclic(true);

		hour = (WheelView) view.findViewById(R.id.hour);
		NumericWheelAdapter numericWheelAdapter3=new NumericWheelAdapter(mContext,1, 23, "%02d");
		numericWheelAdapter3.setLabel("时");
		hour.setViewAdapter(numericWheelAdapter3);
		hour.setCyclic(true);

		min = (WheelView) view.findViewById(R.id.min);
		NumericWheelAdapter numericWheelAdapter4=new NumericWheelAdapter(mContext,1, 59, "%02d");
		numericWheelAdapter4.setLabel("分");
		min.setViewAdapter(numericWheelAdapter4);
		min.setCyclic(true);

		//设置显示行数
		year.setVisibleItems(7);
		month.setVisibleItems(7);
		day.setVisibleItems(7);
		hour.setVisibleItems(7);
		min.setVisibleItems(7);

		year.setCurrentItem(curYear - curYear + beforenowYear);
		month.setCurrentItem(curMonth - 1);
		day.setCurrentItem(curDay - 1);
		hour.setCurrentItem(curHour - 1);
		min.setCurrentItem(curMin - 1);

		TextView btnSure = (TextView)view.findViewById(R.id.sure);
		btnSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Map<String,Object> params=new HashMap<String, Object>();
				params.put("year",(year.getCurrentItem()+curYear-beforenowYear));
				params.put("month",(month.getCurrentItem()+1) < 10 ? "0"+(month.getCurrentItem()+1):(month.getCurrentItem()+1));
				params.put("day",(day.getCurrentItem()+1) < 10 ? "0"+(day.getCurrentItem()+1):(day.getCurrentItem()+1));
				params.put("hour",((hour.getCurrentItem()+1) < 10 ? "0"+(hour.getCurrentItem()+1):(hour.getCurrentItem()+1)));
				params.put("minute",(min.getCurrentItem()+1) < 10 ? "0"+(min.getCurrentItem()+1):(min.getCurrentItem()+1));
				executeLua(attrMap.get(ATTR_DONE), params);
				menuWindow.dismiss();
			}
		});

		TextView btnCancle = (TextView)view.findViewById(R.id.cancle);
		btnCancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				menuWindow.dismiss();
			}
		});

		year.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				// TODO Auto-generated method stub
				int selYear=newValue+curYear-beforenowYear;
				Object selMonth=(month.getCurrentItem()+1) < 10 ? "0"+(month.getCurrentItem()+1):(month.getCurrentItem()+1);
				Object selDay=(day.getCurrentItem()+1) < 10 ? "0"+(day.getCurrentItem()+1):(day.getCurrentItem()+1);
				boolean is=isDate(selYear+"-"+selMonth+"-"+selDay);
				if(!is){
					int maxDay=getDay(selYear, month.getCurrentItem()+1);
					day.setCurrentItem(maxDay-1);
				}
			}
		});

		month.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				// TODO Auto-generated method stub
				int selYear=year.getCurrentItem()+curYear-beforenowYear;
				Object selMonth=(newValue+1) < 10 ? "0"+(newValue+1):(newValue+1);
				Object selDay=(day.getCurrentItem()+1) < 10 ? "0"+(day.getCurrentItem()+1):(day.getCurrentItem()+1);
				boolean is=isDate(selYear+"-"+selMonth+"-"+selDay);
				if(!is){
					int maxDay=getDay(selYear, newValue+1);
					day.setCurrentItem(maxDay-1);
				}
			}
		});

		String type=attrMap.get(ATTR_FORMAT);
		if (type.equals("yyyy-MM")) {
			day.setVisibility(View.GONE);
			hour.setVisibility(View.GONE);
			min.setVisibility(View.GONE);
		} else if (type.equals("yyyy-MM-dd")) {
			hour.setVisibility(View.GONE);
			min.setVisibility(View.GONE);
		} else if (type.equals("HH:mm")) {
			year.setVisibility(View.GONE);
			month.setVisibility(View.GONE);
			day.setVisibility(View.GONE);
		}
	}

	OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
		@Override
		public void onScrollingStarted(WheelView wheel) {

		}

		@Override
		public void onScrollingFinished(WheelView wheel) {
			int n_year = year.getCurrentItem() + Calendar.getInstance().get(Calendar.YEAR) + beforenowYear; //年
			int n_month = month.getCurrentItem() + 1; //月
			initDay(n_year,n_month);
		}
	};

	private void initDay(int arg1, int arg2) {
		NumericWheelAdapter numericWheelAdapter=new NumericWheelAdapter(mContext,1, getDay(arg1, arg2), "%02d");
		numericWheelAdapter.setLabel("日");
		day.setViewAdapter(numericWheelAdapter);
	}

	/**
	 * @param year
	 * @param month
	 * @return
	 */
	public int getDay(int year, int month) {
		int day = 30;
		boolean flag = false;
		switch (year % 4) {
			case 0:
				flag = true;
				break;
			default:
				flag = false;
				break;
		}
		switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				day = 31;
				break;
			case 2:
				day = flag ? 29 : 28;
				break;
			default:
				day = 30;
				break;
		}
		return day;
	}

	/**
	 * 判断日期格式和范围
	 */
	public boolean isDate(String date) {
		String rexp = "^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
		Pattern pat = Pattern.compile(rexp);
		Matcher mat = pat.matcher(date);
		boolean dateType = mat.matches();
		return dateType;
	}

}
