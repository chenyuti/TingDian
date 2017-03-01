package com.logansoft.UIEngine.parse.xmlview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.w3c.dom.Element;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.logansoft.UIEngine.Base.UIEngineColorParser;
import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.Base.Entity.UIEngineEntity;
import com.logansoft.UIEngine.Base.Interface.EntityInterface;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.XmlParser;
import com.logansoft.UIEngine.parse.field.adapterGroup.ListTable;
import com.logansoft.UIEngine.parse.xmlview.Text.MLabel;
import com.logansoft.UIEngine.parse.xmlview.anim.AnimInterface;
import com.logansoft.UIEngine.parse.xmlview.anim.SimpleAnim;
import com.logansoft.UIEngine.provider.LuaProvider;
import com.logansoft.UIEngine.utils.GlobalConstants;
import com.logansoft.UIEngine.utils.LogUtil;
import com.logansoft.UIEngine.utils.StringUtil;
import com.logansoft.UIEngine.utils.http.RequestListener;
import com.logansoft.UIEngine.utils.http.RequestManager;
import com.logansoft.UIEngine.utils.http.RequestOptions;
import com.logansoft.UIEngine.utils.ninePatch.NinePatchChunk;
import com.logansoft.UIEngine.view.RotateImage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class BaseView implements EntityInterface {
	public static final String BOOLEANTRUE = GlobalConstants.BOOLEANTRUE;
	public static final String BOOLEANFALSE = GlobalConstants.BOOLEANFALSE;

	public static final String ATTR_ID = "id";
	public static final String ATTR_VALUE = "value";

	public static final String ATTR_MINWIDTH = "minWidth";
	public static final String ATTR_MAXWIDTH = "maxWidth";
	public static final String ATTR_MINHEIGHT = "minHeight";
	public static final String ATTR_MAXHEIGHT = "maxHeight";

	public static final String ATTR_SIZE = "size";
	public static final String FILL = "fill";
	public static final String WRAP = "wrap";
	public static final String ATTR_MARGIN = "margin";
	public static final String ATTR_PADDING = "padding";
	public static final String ATTR_GRAVITY = "gravity";

	public static final String ATTR_IMAGE = "image";
	public static final String ATTR_PRESSIMAGE = "pressImage";
	public static final String ATTR_SELECTEDIMAGE = "selectedImage";
	public static final String ATTR_DISABLEIMAGE = "disableImage";

	public static final String ATTR_IMAGE_SIZE = "imageSize";

	public static final String ATTR_ALPHA = "alpha";

	public static final String ATTR_CLICK = "onClick";
	public static final String ATTR_LONG_CLICK = "onLongClick";

	public static final String ATTR_SELECTED = "selected";
	public static final String ATTR_DISABLED = "disabled";
	public static final String ATTR_HIDDEN = "hidden";

	public static final String ATTR_GROUP_DASH_PATTERN = "dashPattern"; // 虚线
																		// (虚线,间隙)
	public static final String ATTR_GROUP_BORDER_WIDTH = "borderWidth"; // 边框宽度
	public static final String ATTR_GROUP_BORDER_AGULAR = "cornerRadius"; // 边框圆角半径
	public static final String ATTR_GROUP_BORDER_COLOR = "borderColor"; // 边框颜色

	public static final String ATTR_BACKGROUND = "background";
	public static final String ATTR_BACKGROUND_PRESS = "pressBackground";
	public static final String ATTR_BACKGROUND_SELECTED = "selectedBackground";
	public static final String ATTR_BACKGROUND_DISABLE = "disabledBackground";

	public static final String ATTR_CLIPSTOBOUNDS = "clipsToBounds";

	public static final int state_background = -1;
	public static final int pressed = android.R.attr.state_pressed;
	public static final int selected = android.R.attr.state_selected;
	public static final int disable = -android.R.attr.state_enabled;

	// 用于listviewitem内部的点击事件获取以及数据的存储
	public static final String ON_CHILD_ITEM_CLICK = "onChildItemClick";

	// 清空缓存（true，false）
	public static final String RESET_DATA = "resetData";

	public Context mContext;
	public Element mElement;
	public View mView;
	protected UIEngineLayoutParams mLayoutParams;
	protected UIEngineDrawable mDrawable;
	private GroupView mParent; // 父对象
	protected BaseView rootView;
	public String mId; // 控件id
	public int mLayoutId; // 布局使用的id;
	protected HashMap<String, String> attrMap;
	protected UIEngineEntity entity;
	protected BaseFragment baseFragment;
	protected String value;

	protected Bitmap.Config ARGBMode;
	protected int[] mPadding;
	protected boolean isCache; // 是否使用缓存图片
	protected boolean clipsToBounds;// 使子view的不会超界，尤其是圆角位置，但是内存消耗大，能不用就不用

	// private float dashWidth = 0;
	// private float dashGap = 0;
	protected boolean defaultClickable;

	// 顺序list，用于播放的顺序控制
	protected List<Integer> sortIndex = new ArrayList<Integer>();
	// 控件动画
	protected AnimInterface animInterface;

	public BaseView(BaseFragment fragment, GroupView parentView, Element element) {
		// 默认动画
		animInterface = new SimpleAnim();

		baseFragment = fragment;
		mContext = fragment.getActivity();
		mElement = element;
		attrMap = XmlParser.getParser().parseElementAttr(element);
		mParent = parentView;
		if ("true".equals(attrMap.get("rootElement")) || mParent == null)
			rootView = this;
		else
			rootView = mParent.rootView;
		setViewId(attrMap.get(ATTR_ID));
		mLayoutParams = new UIEngineLayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		mLayoutParams.defaultHeight = LayoutParams.WRAP_CONTENT;
		mLayoutParams.defaultWidth = LayoutParams.WRAP_CONTENT;
		createMyView();
		mView.setLayoutParams(mLayoutParams);
		parseView();
		parseAttributes();
	}

	// 设置动画，用户可以自定义，只要调用此方法，动画就会生效
	public void setAnim(AnimInterface anim) {
		if (anim == null) {
			anim = new SimpleAnim();
		}
		this.animInterface = anim;
	}

	public void start(View view) {
		animInterface.startAni(view);
	}

	public void stop(View view) {
		animInterface.stopAni(view);
	}

	public void start(BaseView view) {
		animInterface.startAni(view);
	}

	public void stop(BaseView view) {
		animInterface.stopAni(view);
	}

	protected void createMyView() {
		mView = new View(mContext) {
			@Override
			public void draw(Canvas canvas) {
				bdraw(canvas);
				super.draw(canvas);
				adraw(canvas);
			}
		};
	}

	protected void parseView() {
		mDrawable = new UIEngineDrawable(mContext);
		mView.setBackgroundDrawable(mDrawable);
		defaultClickable = false;
		ARGBMode = Config.RGB_565;
	}

	protected void parseAttributes() {
		setCache(attrMap.get("cache"));
		setARGB(attrMap.get("ARGB"));
		setBorderColorString(attrMap.get(ATTR_GROUP_BORDER_COLOR));
		setCornerRadiusString(attrMap.get(ATTR_GROUP_BORDER_AGULAR));
		setBorderWidthString(attrMap.get(ATTR_GROUP_BORDER_WIDTH));
		setSize(attrMap.get(ATTR_SIZE));
		setLimitSizeString();
		parseBackgroundForState(attrMap.get(ATTR_BACKGROUND),
				UIEngineDrawable.StateNormal);
		parseBackgroundForState(attrMap.get(ATTR_BACKGROUND_PRESS),
				UIEngineDrawable.StatePressed);
		parseBackgroundForState(attrMap.get(ATTR_BACKGROUND_SELECTED),
				UIEngineDrawable.StateSelected);
		parseBackgroundForState(attrMap.get(ATTR_BACKGROUND_DISABLE),
				UIEngineDrawable.StateDisabled);
		setPadding(attrMap.get(ATTR_PADDING));
		setMargin(attrMap.get(ATTR_MARGIN));
		setLayoutGravity(attrMap.get(ATTR_GRAVITY));
		setAlpha(attrMap.get(ATTR_ALPHA));
		setDisabled(attrMap.get(ATTR_DISABLED));
		setHidden(attrMap.get(ATTR_HIDDEN));
		setSelectedString(attrMap.get(ATTR_SELECTED));
		setOnClick(attrMap.get(ATTR_CLICK));
		setOnLongClick(attrMap.get(ATTR_LONG_CLICK));
		setValue(attrMap.get(ATTR_VALUE));
		setClipsToBounds(BOOLEANTRUE.equals(attrMap.get(ATTR_CLIPSTOBOUNDS)));
	}

	// EntityInterface
	public void setAttributes(Map<String, String> updateAttributeMap) {
		if (updateAttributeMap != null && updateAttributeMap instanceof Map) {
			attrMap.putAll(updateAttributeMap);
			if (updateAttributeMap.containsKey("cache"))
				setCache(attrMap.get("cache"));
			if (updateAttributeMap.containsKey(ATTR_GROUP_BORDER_COLOR))
				setBorderColorString(attrMap.get(ATTR_GROUP_BORDER_COLOR));
			if (updateAttributeMap.containsKey(ATTR_GROUP_BORDER_AGULAR))
				setCornerRadiusString(attrMap.get(ATTR_GROUP_BORDER_AGULAR));
			if (updateAttributeMap.containsKey(ATTR_GROUP_BORDER_WIDTH))
				setBorderWidthString(attrMap.get(ATTR_GROUP_BORDER_WIDTH));
			if (updateAttributeMap.containsKey(ATTR_SIZE))
				setSize(attrMap.get(ATTR_SIZE));
			if (updateAttributeMap.containsKey(ATTR_BACKGROUND))
				parseBackgroundForState(attrMap.get(ATTR_BACKGROUND),
						UIEngineDrawable.StateNormal);
			if (updateAttributeMap.containsKey(ATTR_BACKGROUND_PRESS))
				parseBackgroundForState(attrMap.get(ATTR_BACKGROUND_PRESS),
						UIEngineDrawable.StatePressed);
			if (updateAttributeMap.containsKey(ATTR_BACKGROUND_SELECTED))
				parseBackgroundForState(attrMap.get(ATTR_BACKGROUND_SELECTED),
						UIEngineDrawable.StateSelected);
			if (updateAttributeMap.containsKey(ATTR_BACKGROUND_DISABLE))
				parseBackgroundForState(attrMap.get(ATTR_BACKGROUND_DISABLE),
						UIEngineDrawable.StateDisabled);
			if (updateAttributeMap.containsKey(ATTR_MINWIDTH)
					|| updateAttributeMap.containsKey(ATTR_MAXWIDTH)
					|| updateAttributeMap.containsKey(ATTR_MINHEIGHT)
					|| updateAttributeMap.containsKey(ATTR_MAXHEIGHT))
				setLimitSizeString();
			if (updateAttributeMap.containsKey(ATTR_PADDING))
				setPadding(attrMap.get(ATTR_PADDING));
			if (updateAttributeMap.containsKey(ATTR_MARGIN))
				setMargin(attrMap.get(ATTR_MARGIN));
			if (updateAttributeMap.containsKey(ATTR_GRAVITY))
				setLayoutGravity(attrMap.get(ATTR_GRAVITY));
			if (updateAttributeMap.containsKey(ATTR_ALPHA))
				setAlpha(attrMap.get(ATTR_ALPHA));
			if (updateAttributeMap.containsKey(ATTR_DISABLED))
				setDisabled(attrMap.get(ATTR_DISABLED));
			if (updateAttributeMap.containsKey(ATTR_HIDDEN))
				setHidden(attrMap.get(ATTR_HIDDEN));
			if (updateAttributeMap.containsKey(ATTR_SELECTED))
				setSelectedString(attrMap.get(ATTR_SELECTED));
			if (updateAttributeMap.containsKey(ATTR_CLICK))
				setOnClick(attrMap.get(ATTR_CLICK));
			if (updateAttributeMap.containsKey(ATTR_LONG_CLICK))
				setOnLongClick(attrMap.get(ATTR_LONG_CLICK));
			if (updateAttributeMap.containsKey(ATTR_VALUE))
				setValue(attrMap.get(ATTR_VALUE));
		}
	}

	public void upDateEntity(Object entityData) {
		if (entityData instanceof LuaObject) {
			try {
				entityData = LuaProvider.toObject((LuaObject) entityData);
			} catch (LuaException e) {
				e.printStackTrace();
				return;
			}
		}
		if (entityData == entity)
			return;
		if (entity instanceof UIEngineEntity
				&& (entityData == null || entityData instanceof UIEngineEntity || entityData instanceof String)) {
			entity.setBaseView(null, null);
		}
		if (entityData instanceof UIEngineEntity)
			entity = (UIEngineEntity) entityData;
		else if (entity == null)
			entity = new UIEngineEntity(entityData);
		else
			entity.setEntityData(entityData);
		entity.setBaseView(this, entity);
	}

	public UIEngineEntity getEntity() {
		return entity;
	}

	public BaseView getElementById(String id) {
		if (!StringUtil.isEmpty(id) && id.equals(mId))
			return this;
		return null;
	}

	@Override
	public void setValueForKey(String key, String value) {
		BaseView bv = getElementById(key);
		if (bv != null)
			bv.getEntityInterface().setValue(value);
	}

	@Override
	public String getValueForKey(String key) {
		BaseView bv = getElementById(key);
		if (bv != null)
			return bv.getEntityInterface().getValue();
		return null;
	}

	public Builder getBuilder(boolean isCache) {
		Builder builder = new DisplayImageOptions.Builder();
		builder.resetViewBeforeLoading(true);
		// 设置下载的图片是否缓存在内存中
		builder.cacheInMemory(isCache);
		// 设置下载的图片是否缓存在SD卡中
		builder.cacheOnDisk(isCache);
		builder.bitmapConfig(ARGBMode);
		builder.displayer(new FadeInBitmapDisplayer(300));
		builder.imageScaleType(ImageScaleType.EXACTLY);
		return builder;
	}

	/**
	 * 开始执行动画
	 * 
	 * @param id
	 *            动画的id
	 * @param animStart
	 *            动画开始回调
	 * @param animRepeat
	 *            动画重复回调
	 * @param animEnd
	 *            动画结束回调
	 */
	public void startAnimation(int id, final String animStart,
			final String animRepeat, final String animEnd) {
		Animation animation = AnimationUtils.loadAnimation(mContext, id);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (!TextUtils.isEmpty(animStart)) {
					executeLua(animStart);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				if (!TextUtils.isEmpty(animRepeat)) {
					executeLua(animRepeat);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (!TextUtils.isEmpty(animEnd)) {
					executeLua(animEnd);
				}
			}
		});
		mView.startAnimation(animation);
	}

	public void setCache(String cacheString) {
		isCache(!BOOLEANFALSE.equals(cacheString));
	}

	public void setSelectedString(String selectedString) {
		boolean selected = false;
		if (!TextUtils.isEmpty(selectedString))
			selected = Boolean.parseBoolean(selectedString);
		setSelected(selected);
	}

	/**
	 * 设置view的隐藏,如果传入的为true隐藏,默认隐藏
	 * 
	 * @param b
	 */
	public void setHidden(String hiddenString) {
		boolean hidden = false;
		if (!TextUtils.isEmpty(hiddenString))
			hidden = Boolean.parseBoolean(hiddenString);
		setHidden(hidden);
	}

	public void setHidden(boolean hidden) {
		RunableHelper r = new RunableHelper() {
			@Override
			public void run() {
				mView.setVisibility(booleanValue ? View.GONE : View.VISIBLE);
			}
		};
		r.booleanValue = hidden;
		if (Looper.myLooper() != Looper.getMainLooper())
			mView.post(r);
		else
			r.run();

	}

	public void setHidden(boolean hidden, boolean anim) {
		RunableHelper r = new RunableHelper() {
			public void run() {
				ScaleAnimation mShowAction = new ScaleAnimation(1f, 1f, 0.0f,
						1f, Animation.RELATIVE_TO_SELF, 0f,
						Animation.RELATIVE_TO_SELF, 0f);
				mShowAction.setDuration(300);
				ScaleAnimation mHiddenAction = new ScaleAnimation(1f, 1f, 1f,
						0.0f, Animation.RELATIVE_TO_SELF, 0f,
						Animation.RELATIVE_TO_SELF, 0f);
				mHiddenAction.setDuration(300);
				mView.setAnimation(booleanValue ? mHiddenAction : mShowAction);
				mView.setVisibility(booleanValue ? View.GONE : View.VISIBLE);
			}
		};
		r.booleanValue = hidden;
		mView.post(r);
	}

	public void setHiddenUseAnim(boolean hidden, final int showanim,
			final int hiddenanim) {
		RunableHelper r = new RunableHelper() {
			public void run() {
				Animation showanimation = AnimationUtils.loadAnimation(
						mContext, showanim);
				Animation hiddenanimation = AnimationUtils.loadAnimation(
						mContext, hiddenanim);
				mView.startAnimation(booleanValue ? hiddenanimation
						: showanimation);
				mView.setVisibility(booleanValue ? View.GONE : View.VISIBLE);
			}
		};
		r.booleanValue = hidden;
		mView.post(r);
	}

	public void setOnClick(String onClickString) {
		attrMap.put(ATTR_CLICK, onClickString);
		if (onClickString != null && onClickString.length() != 0)
			mView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					executeLua(attrMap.get(ATTR_CLICK), null);
				}
			});
		else {
			mView.setOnClickListener(null);
			mView.setClickable(defaultClickable);
		}
	}

	private void setOnLongClick(String longClick) {
		if (longClick != null && longClick.length() != 0)
			mView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					executeLua(attrMap.get(ATTR_LONG_CLICK), null);
					return true;
				}
			});
		else {
			mView.setOnLongClickListener(null);
			mView.setLongClickable(false);
		}
	}

	public void executeLua(String function) {
		executeLua(function, null);
	}

	public void executeLua(String function, Map<String, Object> params) {
		if (function == null || function.length() == 0)
			return;
		if (params == null)
			params = new HashMap<String, Object>();
		params.put("value", getValue());
		params.put("id", this.mId);
		params.put("parent", this.mParent);
		baseFragment.loadLua(BaseView.this, function, params);
	}

	protected void setImage(ImageView view, HashMap<String, String> attr) {
		parseImage(view, UIEngineDrawable.StateNormal, attr.get(ATTR_IMAGE));
		parseImage(view, UIEngineDrawable.StatePressed,
				attr.get(ATTR_PRESSIMAGE));
		parseImage(view, UIEngineDrawable.StateSelected,
				attr.get(ATTR_SELECTEDIMAGE));
		parseImage(view, UIEngineDrawable.StateDisabled,
				attr.get(ATTR_DISABLEIMAGE));
	}

	protected void parseImage(ImageView imageView, int state, String imageString) {
		Drawable drawable = imageView.getDrawable();
		if (!(drawable instanceof UIEngineDrawable))
			return;
		UIEngineDrawable imageDrawable = (UIEngineDrawable) drawable;
		String oldImageURL = imageDrawable.getImageURLForState(state);
		if (oldImageURL != null && oldImageURL.equals(imageString))
			return;
		imageDrawable.setColorForState(state, -2);
		if (imageString == null || imageString.length() == 0)
			return;
		int color = UIEngineColorParser.getColor(imageString);
		if (color != -2)
			imageDrawable.setColorForState(state, color);
		else {
			Builder builder = getBuilder(isCache);
			RequestListener rl = new RequestListener() {
				@Override
				public void DidCanceled() {
					Object imageView = params
							.get(RequestManager.REQUESTCALLBACK);
					if (imageView instanceof RotateImage)
						((RotateImage) imageView).setLoading(false);
				}

				@Override
				public void exception(Exception e) {
					Object imageView = params
							.get(RequestManager.REQUESTCALLBACK);
					if (imageView instanceof RotateImage)
						((RotateImage) imageView).setLoading(false);
				}

				@Override
				public void DidLoad(Object result,
						Map<String, Object> responseLuaArgs, int callBackType) {
					if (callBackType == CacheDidUpdateCallBackType)
						return;
					int state = (Integer) params
							.get(RequestManager.IMAGEREQUESTSTATE);
					String RequestURL = (String) params
							.get(RequestOptions.REQUESTURL);
					ImageView imageView = (ImageView) params
							.get(RequestManager.REQUESTCALLBACK);
					if (imageView instanceof RotateImage)
						((RotateImage) imageView).setLoading(false);
					Drawable drawable = imageView.getDrawable();
					UIEngineDrawable imageDrawable = null;
					if (drawable != null
							&& drawable instanceof UIEngineDrawable) {
						imageDrawable = (UIEngineDrawable) drawable;
						String oldURL = imageDrawable
								.getImageURLForState(state);
						if (!RequestURL.equals(oldURL))
							return;
					} else
						return;
					imageView.setImageDrawable(null);
					Bitmap loadedImage = null;
					if (result instanceof Bitmap)
						loadedImage = (Bitmap) result;
					else
						return;
					if (RequestURL.endsWith(".9.png")) {
						loadedImage.setDensity(NinePatchChunk.DEFAULT_DENSITY);
						imageDrawable.setDrawableForState(state, NinePatchChunk
								.create9PatchDrawable(mContext, loadedImage,
										RequestURL));
					} else
						imageDrawable.setBitmapForState(state, loadedImage);
					imageView.setImageDrawable(imageDrawable);
				}
			};
			imageDrawable.setImageURLForState(state, imageString);
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(RequestManager.IMAGEREQUESTTARGETWIDTH,
					mLayoutParams.width);
			params.put(RequestManager.IMAGEREQUESTTARGETHEIGHT,
					mLayoutParams.height);
			params.put(RequestManager.REQUESTCALLBACK, imageView);
			params.put(RequestOptions.REQUESTURL, imageString);
			params.put(RequestOptions.RESPONSETYPE, "IMAGE");
			params.put(RequestManager.REQUESTLISTENER, rl);
			params.put(RequestManager.IMAGEREQUESTSTATE, state);
			params.put(RequestManager.IMAGEREQUESTOPTIONS, builder.build());
			RequestManager.request(params);
		}
	}

	public void parseBackgroundForState(String backgroundString, final int state) {
		mDrawable.setColorForState(state, -2);// clear old
		if (backgroundString == null || backgroundString.length() == 0)
			return;
		int color = UIEngineColorParser.getColor(backgroundString);
		if (color != -2)
			mDrawable.setColorForState(state, color);
		else {
			Builder builder = getBuilder(isCache);
			RequestListener rl = new RequestListener() {
				@Override
				public void DidLoad(Object result,
						Map<String, Object> responseLuaArgs, int callBackType) {
					if (callBackType == CacheDidUpdateCallBackType)
						return;
					int state = (Integer) params
							.get(RequestManager.IMAGEREQUESTSTATE);
					String RequestURL = (String) params
							.get(RequestOptions.REQUESTURL);
					if (!mDrawable.getImageURLForState(state)
							.equals(RequestURL))
						return;
					if (!(result instanceof Bitmap))
						return;
					Bitmap loadedImage = (Bitmap) result;
					if (RequestURL.endsWith(".9.png")) {
						loadedImage.setDensity(NinePatchChunk.DEFAULT_DENSITY);
						mDrawable.setDrawableForState(state, NinePatchChunk
								.create9PatchDrawable(mContext, loadedImage,
										null));
					} else
						mDrawable.setBitmapForState(state, loadedImage);
				}
			};
			mDrawable.setImageURLForState(state, backgroundString);
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(RequestManager.IMAGEREQUESTTARGETWIDTH,
					mLayoutParams.width);
			params.put(RequestManager.IMAGEREQUESTTARGETHEIGHT,
					mLayoutParams.height);
			params.put(RequestManager.REQUESTCALLBACK, mView);
			params.put(RequestOptions.REQUESTURL, backgroundString);
			params.put(RequestOptions.RESPONSETYPE, "IMAGE");
			params.put(RequestManager.REQUESTLISTENER, rl);
			params.put(RequestManager.IMAGEREQUESTSTATE, state);
			params.put(RequestManager.IMAGEREQUESTOPTIONS, builder.build());
			RequestManager.request(params);
		}
	}

	/**
	 * 刷新背景
	 * 
	 * @param view
	 */
	public void refreshBackground(View view) {
	}

	@SuppressLint("NewApi")
	public void setAlpha(String alpha) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			float alphaF = 1;
			if (alpha != null && alpha.length() != 0) {
				alphaF = Float.parseFloat(alpha);
			}
			mView.setAlpha(alphaF);
		} else
			mDrawable.setAlphaString(alpha);
	}

	public void setSize(String sizeString) {
		mLayoutParams.setSizeString(sizeString, mContext);
	}

	public void setLimitSizeString() {
		mLayoutParams.setLimitSizeString(mContext, attrMap.get(ATTR_MINWIDTH),
				attrMap.get(ATTR_MAXWIDTH), attrMap.get(ATTR_MINHEIGHT),
				attrMap.get(ATTR_MAXHEIGHT));
	}

	/**
	 * 设置ImageView 的大小
	 * 
	 * @param view
	 * @param params
	 */
	public void setImageSize(final ImageView view, ViewGroup.LayoutParams params) {
		setImageSize(view, attrMap, params);
	}

	/**
	 * 设置ImageView 的大小
	 * 
	 * @param view
	 * @param params
	 */
	public void setImageSize(final ImageView view,
			HashMap<String, String> attrMap, ViewGroup.LayoutParams params) {
		if (attrMap.containsKey(ATTR_IMAGE_SIZE)) {
			String size = attrMap.get(ATTR_IMAGE_SIZE);
			String[] s = size.split(",");
			int width = UIEngineLayoutParams.parseSingleSizeString(s[0],
					mContext, 0);
			int height = UIEngineLayoutParams.parseSingleSizeString(s[1],
					mContext, 1);
			params.width = width;
			params.height = height;
		}
	}

	public void setARGB(String modeString) {
		ARGBMode = Config.RGB_565;
		if (BOOLEANTRUE.equals(modeString))
			ARGBMode = Config.ARGB_8888;
	}

	public void setViewId(String tag) {
		if (mId != null && mId.length() != 0) {
			if (rootView instanceof GroupView) {
				((GroupView) rootView).removeIdView(this);
			}
		}
		mId = tag;
		if (mId != null && mId.length() != 0) {
			if (rootView instanceof GroupView) {
				((GroupView) rootView).putIdView(this);
			}
		}
	}

	public String getViewId() {
		return mId;
	}

	protected int[] getPaddingOrMargin(String pm) {
		int[] result = new int[4];
		if (!StringUtil.isEmpty(pm)) {
			String[] s = pm.split(",");
			if (s.length != 4)
				return result;
			for (int i = 0; i < 4; i++) {
				result[i] = UIEngineLayoutParams
						.parseSingleMarginOrPaddingString(s[i], mContext, i);
			}
		}
		return result;
	}

	public void setPadding(String padding) {
		mPadding = getPaddingOrMargin(padding);
		mView.setPadding(mPadding[0], mPadding[1], mPadding[2], mPadding[3]);
	}

	public int[] getmPadding() {
		return mPadding;
	}

	public void setMargin(String margin) {
		mLayoutParams.setMarginString(margin, mContext);
	}

	public int[] getmMargin() {
		return mLayoutParams.getMargin();
	}

	public void setLayoutGravity(String gravity) {
		mLayoutParams.setGravityString(gravity);
	}

	public void setBorderColorString(String borderColorString) {
		mDrawable.setBorderColorString(borderColorString);
	}

	public void setBorderWidthString(String borderWidthString) {
		mDrawable.setBorderWidth(borderWidthString, mContext);
	}

	public void setCornerRadiusString(String cornerRadiusString) {
		mDrawable.setCornerRadius(cornerRadiusString, mContext);
	}

	public boolean isHidden() {
		return mView.getVisibility() != View.VISIBLE;
	}

	public View getView() {
		return mView;
	}

	public void isCache(boolean isCache) {
		this.isCache = isCache;
	}

	public void setDisabled(String disatbledString) {
		boolean disabled;
		if (!TextUtils.isEmpty(disatbledString))
			disabled = Boolean.parseBoolean(disatbledString);
		else
			disabled = false;
		mView.setEnabled(!disabled);
	}

	public void setDisable(boolean disable) {
		mView.setEnabled(!disable);
	}

	public boolean getDisable() {
		return mView.isEnabled();
	}

	public boolean isDisable() {
		return getDisable();
	}

	public void setOnTouchListener(OnTouchListener onTouchListener) {
		mView.setOnTouchListener(onTouchListener);
	}

	public int getmWidth() {
		return mLayoutParams.width;
	}

	public void setmWidth(int mWidth) {
		mLayoutParams.width = mWidth;
	}

	public int getmHeight() {
		return mLayoutParams.height;
	}

	public void setmHeight(int mHeight) {
		mLayoutParams.height = mHeight;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public int getmLayoutId() {
		return mLayoutId;
	}

	public void setmLayoutId(int mLayoutId) {
		this.mLayoutId = mLayoutId;
	}

	public BaseView getRootView() {
		return rootView;
	}

	public GroupView getParent() {
		return mParent;
	}

	// EntityInterface
	public EntityInterface getEntityInterface() {
		return entity != null ? entity : this;
	}

	@Override
	public void setSelected(boolean selected) {
		mView.setSelected(selected);
	}

	@Override
	public boolean isSelected() {
		return mView.isSelected();
	}

	@Override
	public void setAttributes(Object attributeMap) {
		if (attributeMap instanceof LuaObject) {
			try {
				attributeMap = LuaProvider.toObject((LuaObject) attributeMap);
			} catch (LuaException e) {
				e.printStackTrace();
			}
		}
		if (attributeMap instanceof Map)
			setAttributes((Map<String, String>) attributeMap);
	}

	@Override
	public HashMap<String, String> getAttributes() {
		return attrMap;
	}

	@Override
	public String getAttribute(String key) {
		return attrMap.get(key);
	}

	public void setClipsToBounds(boolean clipsToBounds) {
		this.clipsToBounds = clipsToBounds;
	}

	public boolean isClipsToBounds() {
		return clipsToBounds;
	}

	protected void bdraw(Canvas canvas) {
		float cornerRadius = mDrawable.getCornerRadius();
		if (cornerRadius > 0 && clipsToBounds)
			canvas.saveLayer(null, new Paint(), canvas.ALL_SAVE_FLAG);
	}

	protected void adraw(Canvas canvas) {
		float cornerRadius = mDrawable.getCornerRadius();
		if (cornerRadius > 0 && clipsToBounds) {
			int w = mView.getMeasuredWidth();
			int h = mView.getMeasuredHeight();
			RectF rf = new RectF(0, 0, w, h);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			Path clipPath = new Path();
			clipPath.addRoundRect(rf, cornerRadius, cornerRadius,
					Path.Direction.CW);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			clipPath.setFillType(FillType.INVERSE_WINDING);
			canvas.drawPath(clipPath, paint);
			canvas.restore();
		}
		float borderWidth = mDrawable.getBorderWidth();
		// if(borderWidth>0){
		// Rect r=new Rect(mDrawable.getBounds());
		// RectF rf=new RectF(r);
		// float bw=borderWidth/2;
		// if(bw>0){
		// r.left+=bw;r.top+=bw;r.right-=bw;r.bottom-=bw;
		// rf.left+=bw;rf.top+=bw;rf.right-=bw;rf.bottom-=bw;
		// }
		// Paint mStrokePaint=new Paint();
		// mStrokePaint.setAlpha(mDrawable.getALPHA());
		// mStrokePaint.setStyle(Paint.Style.STROKE);
		// mStrokePaint.setAntiAlias(true);
		// mStrokePaint.setStrokeWidth(borderWidth);
		// if(mDrawable.getBorderColor()!=-2)
		// mStrokePaint.setColor(mDrawable.getBorderColor());
		// else
		// mStrokePaint.setColor(mDrawable.defaultBorderColor);
		//
		// if(cornerRadius==0)
		// canvas.drawRect(r,mStrokePaint);
		// else
		// canvas.drawRoundRect(rf,cornerRadius,cornerRadius, mStrokePaint);
		// }
	}

	public void onLowMemory() {
	}

	public void viewWillAppear() {
	}

	public void viewDidDisappear() {
	}

	public void Destory() {
		if (entity != null) {
			entity.setBaseView(null, null);
			entity.Destory();
		}
		mElement = null;
		if (mView != null)
			mView.setBackgroundDrawable(null);
		mView = null;
		mId = null;
		mParent = null;
		attrMap = null;
		entity = null;
		baseFragment = null;
		mPadding = null;
		mDrawable = null;
		rootView = null;
		// LogUtil.i(this.toString()+" Destory()");
	}

	// protected void finalize(){
	// LogUtil.d("pageLife",this.toString()+" "+mId+" finalize()");
	// }
	/**
	 * 获得listview点击的lable值，即string 方法只支持最多两层嵌套， 例如：1 <groupView>
	 * <MImageButton></MImageButton></groupView>
	 * ,onclick时间只能放到groupview或者MImageButton上面，否则找不到值，因为方法不支持
	 * 类似于Android原生的duplicate属性
	 * 
	 * @return
	 */
	public String getLable() {
		if (mParent != null && !isSelected()) {
			LogUtil.d("mParent is not null");
			List<BaseView> childeViews = mParent.getChildViews();
			if (childeViews != null) {
				for (BaseView baseView : childeViews) {
					if (baseView instanceof GroupView) {
						List<BaseView> childeViews1 = ((GroupView) baseView)
								.getChildViews();
						if (childeViews1 != null && childeViews1.size() > 0) {
							for (BaseView bv : childeViews1) {
								if (bv instanceof MLabel && !bv.isSelected()) {
									MLabel label = (MLabel) bv;
									LogUtil.d("bv is not null = "
											+ label.getText());
									return label.getText();
								}
							}
						}

					} else {
						if (baseView instanceof MLabel) {
							MLabel label = (MLabel) baseView;
							LogUtil.d("baseView is not null = "
									+ label.getText());
							return label.getText();
						}
					}

				}
			} else {
				if (mParent instanceof ListTable) {
					ListTable lt = (ListTable) mParent;
					LogUtil.d("mParent is ListTable = "
							+ lt.getAdapter().getItem(0).getValue());
				}
			}
		}
		return "";
	}

	/**
	 * 给imageView加载网络图片
	 * 
	 * @param url
	 */
	public void loadImage(String url) {
		LogUtil.e("父类的loadImage执行了,开始加载网络图片");
	}

	// 服务器返回值，将值设置到控件上
	private String result = "";

	public String getResultFromServer() {
		return result;
	}

	public void setResultFromServer(String result) {
		this.result = result;
	}

	public void resetData(BaseView bv) {
		if (bv == null) {
			LogUtil.d("baseview is null");
			return;
		}

		BaseView root = bv.rootView;
		if (root == null) {
			LogUtil.d("rootView is null");
			return;
		}

		reset(root);

	}

	private void reset(BaseView root) {
		HashMap<String, String> attr = new HashMap<>();
		if (root == null) {
			return;
		}
		if (root instanceof GroupView) {
			attr = root.getAttributes();

			List<BaseView> list = ((GroupView) root).getChildViews();
			if (list != null) {
				for (BaseView baseView : list) {
					reset(baseView);
				}
			}

			if (!TextUtils.isEmpty(attr.get(BaseView.RESET_DATA))) {
				// if (!TextUtils.isEmpty(attr.get("position"))) {
				// try {
				// position = Integer.parseInt(attr.get("position"));
				// } catch (Exception e) {
				// LogUtil.d("numberformat exception");
				// }
				root.setSelected(false);
			}
		} else {
			attr = root.getAttributes();

			if (!TextUtils.isEmpty(attr.get(BaseView.RESET_DATA))) {
				// if (!TextUtils.isEmpty(attr.get("position"))) {
				// try {
				// position = Integer.parseInt(attr.get("position"));
				// } catch (Exception e) {
				// LogUtil.d("numberformat exception");
				// }
				// }
				root.setSelected(false);
			}
		}
	}

	public String getTag() {
		if (this instanceof MLabel) {
			return ((MLabel) this).getTag();
		}
		return "";
	}
}
