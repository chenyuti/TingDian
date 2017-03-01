package com.logansoft.UIEngine.parse.xmlview.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.BaseView;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.logansoft.UIEngine.parse.xmlview.anim.AnimInterface;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;

import android.R.integer;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

public class MLabel extends BaseView {
	public static final String ATTR_FONT_COLOR = "fontColor";
	public static final String ATTR_FONT_SELECTED_COLOR = "selectedFontColor";
	public static final String ATTR_FONT_PRESS_COLOR = "pressFontColor";
	public static final String ATTR_FONT_DISABLE_COLOR = "disabledFontColor";

	public static final String INPUTTYP_PASSWORD = "password";
	public static final String INPUTTYP_SYS_PASSWORD = "syspassword";
	public static final String INPUTTYP_NUMBER = "number";
	public static final String ATTR_SINGLELINE = "singleLine";
	public static final String LINE_SPACING = "LineSpacing";

	public static final int CORRECT_KEY = 1;

	public static final String ATTR_TEXT_CHANGE = "onTextChanged";
	public static final String ATTR_FONT_SIZE = "fontSize";
	public static final String ATTR_INPUTTYPE = "inputType";
	public static final String ATTR_ENCRYPTION_TYPE = "encryptionType";
	public static final String ATTR_HINT = "placeHolder";
	public static final String ATTR_MAX_LENGTH = "maxLength";
	public static final String ATTR_TEXT_ALIGNMENT = "textAlignment";
	public static final String ATTR_LABEL = "label";
	public static final String ATTR_RETURN_KEYTYPE = "returnKeyType";
	public static final String ATTR_RETURN_KEYONCLICK = "returnKeyOnClick";
	public static final String ATTR_MAX_LINES = "maxLines";
	public static final String ATTR_ELLIPSIZE = "ellipsize";
	public static final String ATTR_TEXTSTYLE = "textStyle";
	public String inputType = "text";

	// 是否支持颜色变换,值：true或者false
	public static final String ATTR_COLOR_CHANGE = "colorChange";

	// 支持按顺序播放动画，值：true或者false
	public static final String ANIM_SUPPORT = "SimpleAnimSupport";
	// 顺序，必须是整形数
	public static final String INDEX = "index";
	// 初始化页面后默认动画控件，取值：default 设置后词控件的动画会在页面加载后开启
	public static final String DEFAULT_ANIM = "default_anim";

	private boolean isThreadRun = true;

	private boolean isColorChange = false;
	private TextView chageTv;
	// 轮转
	private int[] colors = new int[] { Color.YELLOW, Color.BLUE, Color.GREEN };

	protected TextView mTextView;
	protected int defaultTextAlignmentX;
	protected int defaultTextAlignmentY;
	protected int defaultMaxLines;
	protected int defaultFontColor;
	protected float defaultFontSize;
	private Handler handler = new Handler(/* Looper.getMainLooper() */) {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				TextView tv = (TextView) msg.obj;
				tv.setTextColor(colors[i]);
			}
		}

	};

	public MLabel(BaseFragment fragment, GroupView parentView, Element element) {
		super(fragment, parentView, element);
		// Log.d("cyt", "size = " + sortIndex.size());
	}

	@Override
	protected void createMyView() {
		mView = mTextView = new TextView(mContext) {
			@Override
			public void draw(Canvas canvas) {
				bdraw(canvas);
				super.draw(canvas);
				adraw(canvas);
			}
		};

	}

	private int i = 0;

	private void changeTextColor(final TextView tv) {
		if (tv != null) {

			new Thread() {

				@Override
				public void run() {
					super.run();
					while (isThreadRun) {
						if (i >= colors.length) {
							i = 0;
						}
						Message msg = Message.obtain();
						msg.what = 1;
						msg.obj = tv;
						handler.sendMessage(msg);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						i++;
					}
				}
			}.start();

		}
	}

	@Override
	protected void parseView() {
		super.parseView();
		defaultTextAlignmentX = Gravity.LEFT;
		defaultTextAlignmentY = Gravity.CENTER_VERTICAL;
		defaultMaxLines = Integer.MAX_VALUE;
		defaultFontColor = Color.BLACK;
		defaultFontSize = mTextView.getTextSize() / mTextView.getResources().getDisplayMetrics().density;
	}

	@Override
	protected void parseAttributes() {
		super.parseAttributes();
		setValue(attrMap.get(ATTR_LABEL));
		setTextColor();
		setTextSize(attrMap.get(ATTR_FONT_SIZE));
		setTextAlignment(attrMap.get(ATTR_TEXT_ALIGNMENT));
		setMaxLength(attrMap.get(ATTR_MAX_LENGTH));
		setMaxLines(attrMap.get(ATTR_MAX_LINES));
		setEllipsize(attrMap.get(ATTR_ELLIPSIZE));
		setSingleLine(attrMap.get(ATTR_SINGLELINE));
		setLineSpacing(attrMap.get(LINE_SPACING));
		setTextStyle(attrMap.get(ATTR_TEXTSTYLE));

		String icc = attrMap.get(ATTR_COLOR_CHANGE);

		String anim = attrMap.get(ANIM_SUPPORT);
		String defAni = attrMap.get(DEFAULT_ANIM);

		if (!TextUtils.isEmpty(anim) && anim.equals("true") && !anim.equals("null") && !TextUtils.isEmpty(defAni)
				&& !defAni.equals("null")) {
			animInterface.startAni(mTextView);
		}
		if (TextUtils.isEmpty(icc)) {
			isColorChange = false;
		} else if (!TextUtils.isEmpty(icc) && icc.equals("true")) {
			isColorChange = true;
		} else {
			isColorChange = false;
		}
		// 是否支持颜色轮转
		if (isColorChange) {
			if (mTextView != null) {
				chageTv = mTextView;
				changeTextColor(chageTv);
			}
		}
	}

	protected void setLineSpacing(String string) {
		if (!StringUtil.isEmpty(string)) {
			String s[] = string.split(",");
			mTextView.setLineSpacing(DisplayUtil.dip2px(mContext, Float.parseFloat(s[1])), Float.parseFloat(s[0]));
		} else {
			mTextView.setLineSpacing(0f, 1f);
		}
	}

	@Override
	public void setAttributes(Map<String, String> updateAttributeMap) {
		super.setAttributes(updateAttributeMap);
		Log.e("eeeeeeeee11111", updateAttributeMap.containsKey(ATTR_COLOR_CHANGE) + " lable mTextView");

		if (updateAttributeMap.containsKey(ATTR_LABEL))
			setValue(attrMap.get(ATTR_LABEL));
		if (updateAttributeMap.containsKey(ATTR_FONT_PRESS_COLOR)
				|| updateAttributeMap.containsKey(ATTR_FONT_SELECTED_COLOR)
				|| updateAttributeMap.containsKey(ATTR_FONT_DISABLE_COLOR)
				|| updateAttributeMap.containsKey(ATTR_FONT_COLOR))
			setTextColor();
		if (updateAttributeMap.containsKey(ATTR_FONT_SIZE))
			setTextSize(attrMap.get(ATTR_FONT_SIZE));
		if (updateAttributeMap.containsKey(ATTR_TEXT_ALIGNMENT))
			setTextAlignment(attrMap.get(ATTR_TEXT_ALIGNMENT));
		if (updateAttributeMap.containsKey(ATTR_MAX_LENGTH))
			setMaxLength(attrMap.get(ATTR_MAX_LENGTH));
		if (updateAttributeMap.containsKey(ATTR_MAX_LINES))
			setMaxLines(attrMap.get(ATTR_MAX_LINES));
		if (updateAttributeMap.containsKey(ATTR_ELLIPSIZE))
			setEllipsize(attrMap.get(ATTR_ELLIPSIZE));
		if (updateAttributeMap.containsKey(ATTR_SINGLELINE))
			setSingleLine(attrMap.get(ATTR_SINGLELINE));
	}

	/**
	 * 设置样式
	 */
	public void setTextStyle(String textStyle) {
		if (TextUtils.isEmpty(textStyle))
			return;
		if (textStyle.equals("underline")) {
			mTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		}
		// 设置横线
		if (textStyle.equals("deleteline")) {
			mTextView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		}
		if (textStyle.equals("bold")) {
			mTextView.getPaint().setFakeBoldText(true);
		}
	}

	@Override
	public String getValue() {
		return mTextView.getText().toString();
	}

	@Override
	public void setValue(String value) {
		int length = mTextView.length();
		mTextView.setText(value == null ? "" : value);
		if (length == 0)
			mTextView.setLayoutParams(mTextView.getLayoutParams());
	}

	/**
	 * 将float格式化为字符串
	 * 
	 * @param format
	 *            格式
	 * @param value
	 * @return
	 */
	public static String roundString(String format, float value) {
		DecimalFormat decimalFormat = new DecimalFormat(format);
		String a = decimalFormat.format(value);
		return a;
	}

	protected void setTextAlignment(String textAlignment) {
		int alignmentX = 0, alignmentY = 0;
		if (textAlignment != null && textAlignment.length() != 0) {
			String[] grevities = textAlignment.split("\\|");
			for (int i = 0; i < grevities.length; i++) {
				if ("left".equals(grevities[i]))
					alignmentX = Gravity.LEFT;
				else if ("center_horizontal".equals(grevities[i]))
					alignmentX = Gravity.CENTER_HORIZONTAL;
				else if ("right".equals(grevities[i]))
					alignmentX = Gravity.RIGHT;
				else if ("top".equals(grevities[i]))
					alignmentY = Gravity.TOP;
				else if ("bottom".equals(grevities[i]))
					alignmentY = Gravity.BOTTOM;
				else if ("center_vertical".equals(grevities[i]))
					alignmentY = Gravity.CENTER_VERTICAL;
				else if ("center".equals(grevities[i])) {
					alignmentY = (alignmentY == 0 ? Gravity.CENTER_VERTICAL : alignmentY);
					alignmentX = (alignmentX == 0 ? Gravity.CENTER_HORIZONTAL : alignmentX);
				}
			}
		}
		alignmentX = (alignmentX == 0) ? defaultTextAlignmentX : alignmentX;
		alignmentY = (alignmentY == 0) ? defaultTextAlignmentY : alignmentY;
		mTextView.setGravity(alignmentX | alignmentY);
	}

	private void setMaxLines(String maxLinesString) {
		int maxLines = defaultMaxLines;
		if (!TextUtils.isEmpty(maxLinesString))
			maxLines = Integer.parseInt(maxLinesString);
		setMaxLines(maxLines);
	}

	public void setMaxLines(int maxLines) {
		int _maxLines = maxLines <= 0 ? defaultMaxLines : maxLines;
		mTextView.setSingleLine(_maxLines == 1 ? true : false);
		mTextView.setMaxLines(_maxLines);
	}

	public void setSingleLine(String singleLineString) {
		if (singleLineString != null && singleLineString.length() != 0) {
			if (BOOLEANTRUE.equals(singleLineString))
				setMaxLines(1);
		}
	}

	/**
	 * set the max length of the TextView
	 * 
	 * @param view
	 */
	protected void setMaxLength(String maxLengthString) {
		int maxLength = Integer.MAX_VALUE;
		if (!TextUtils.isEmpty(maxLengthString) && maxLengthString.matches("[1-9]+"))
			maxLength = Integer.parseInt(maxLengthString);
		mTextView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) });
	}

	public void setEllipsize(String Ellipsize) {
		TruncateAt ta = TextUtils.TruncateAt.END;
		if ("end".equals(Ellipsize)) {
			ta = TextUtils.TruncateAt.END;
		} else if ("middle".equals(Ellipsize)) {
			ta = TextUtils.TruncateAt.MIDDLE;
		} else if ("marquee".equals(Ellipsize)) {
			ta = TextUtils.TruncateAt.MARQUEE;
		} else if ("start".equals(Ellipsize)) {
			ta = TextUtils.TruncateAt.START;
		}
		mTextView.setEllipsize(ta);
	}

	protected void setTextColor() {
		setTextColor(getFontColorStateList(attrMap, defaultFontColor));
	}

	public void setTextColor(String color) {
		mTextView.setTextColor(Color.parseColor(color));
	}

	public void setTextColor(ColorStateList color) {
		mTextView.setTextColor(color);
	}

	/**
	 * 通过属性设置文字大小
	 */
	protected void setTextSize(String textSize) {
		setTextSize(textSize == null ? defaultFontSize : Float.parseFloat(textSize));
	}

	public void setTextSize(float size) {
		mTextView.setTextSize(size);
	}

	public int length() {
		return mTextView.length();
	}

	static ColorStateList getFontColorStateList(HashMap<String, String> attrMap, int defaultFontColor) {
		int[] colors = new int[4];
		int[][] states = new int[4][];
		int size = 0;
		String pressFontsColor = attrMap.get(ATTR_FONT_PRESS_COLOR);
		if (!StringUtil.isEmpty(pressFontsColor)) {
			states[size] = new int[] { pressed };
			colors[size] = UIEngineColorParser.getColor(pressFontsColor);
			size++;
		}
		String selectedFontsColor = attrMap.get(ATTR_FONT_SELECTED_COLOR);
		if (!StringUtil.isEmpty(selectedFontsColor)) {
			states[size] = new int[] { selected };
			colors[size] = UIEngineColorParser.getColor(selectedFontsColor);
			size++;
		}
		String disableFontsColor = attrMap.get(ATTR_FONT_DISABLE_COLOR);
		if (!StringUtil.isEmpty(disableFontsColor)) {
			states[size] = new int[] { disable };
			colors[size] = UIEngineColorParser.getColor(disableFontsColor);
			size++;
		}
		String fontColor = attrMap.get(ATTR_FONT_COLOR);
		int FontColor = -2;
		if (!StringUtil.isEmpty(fontColor))
			FontColor = UIEngineColorParser.getColor(fontColor);
		if (FontColor == -2)
			FontColor = defaultFontColor;
		states[size] = new int[] { state_background };
		colors[size] = FontColor;
		size++;
		int[] Colors = new int[size];
		System.arraycopy(colors, 0, Colors, 0, size);
		int[][] States = new int[size][];
		System.arraycopy(states, 0, States, 0, size);
		ColorStateList cs = new ColorStateList(States, Colors);
		return cs;
	}

	public String getText() {
		return mTextView == null ? "" : mTextView.getText().toString();
	}

	public void setTag(String tag) {
		mTextView.setTag(tag);
	}

	public String getTag() {
		if (mTextView.getTag() != null) {
			LogUtil.d("getTag in mlable" + mTextView.getTag().toString());
			return mTextView.getTag().toString();
		} else {
			return "";
		}
	}

}
