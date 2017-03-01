package com.logansoft.UIEngine.parse.xmlview;

import java.util.Map;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.Base.UIEngineDrawable;
import com.logansoft.UIEngine.Base.UIEngineGroupView;
import com.logansoft.UIEngine.Base.UIEngineGroupView.UIEngineLayoutParams;
import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.Text.MButton;
import com.logansoft.UIEngine.utils.DisplayUtil;
import com.logansoft.UIEngine.utils.GlobalConstants;

import android.graphics.Canvas;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class MImageButton extends MButton {
	public static final String IMAGE_GRAVITY_LEFT = "left";
	public static final String IMAGE_GRAVITY_TOP = "top";
	public static final String IMAGE_GRAVITY_RIGHT = "right";
	public static final String IMAGE_GRAVITY_BOTTOM = "bottom";
	public static final String ATTR_SPACE = "space";
	public static final String ATTR_IMAGEMARGIN = "imageMargin";
	private static final int ID_TV = 0x5000002;
	private static final int ID_IMAGE = 0x5000001;
	private OnCheckedChangeListener mSelectedChangerListener;
	ImageView iv;

	public MImageButton(BaseFragment baseFragment, GroupView parentView,
			Element mElement) {
		super(baseFragment, parentView, mElement);
	}

	@Override
	protected void createMyView() {
		mView = new UIEngineGroupView(mContext) {
			@Override
			public void draw(Canvas canvas) {
				bdraw(canvas);
				super.draw(canvas);
				adraw(canvas);
			}
		};
		mTextView = new TextView(mContext);
		iv = new ImageView(mContext);
	}

	@Override
	protected void parseView() {
		super.parseView();
		iv.setImageDrawable(new UIEngineDrawable(mContext));
		iv.setId(ID_IMAGE);
		mTextView.setId(ID_TV);
		UIEngineLayoutParams paramsTv = new UIEngineLayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		UIEngineLayoutParams paramsIv = new UIEngineLayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		mTextView.setLayoutParams(paramsTv);
		iv.setLayoutParams(paramsIv);
	}

	@Override
	protected void parseAttributes() {
		super.parseAttributes();
		UIEngineGroupView groupView = (UIEngineGroupView) mView;

		checked(BOOLEANTRUE.equals(attrMap.get(ATTR_SELECTED)));
		String imageGravity = attrMap.get(GlobalConstants.ATTR_IMAGE_GRAVITY);
		String space = attrMap.get(ATTR_SPACE);
		String imageMargin = attrMap.get(ATTR_IMAGEMARGIN);

		UIEngineLayoutParams textParams = (UIEngineLayoutParams) mTextView
				.getLayoutParams();
		UIEngineLayoutParams imageParams = (UIEngineLayoutParams) iv
				.getLayoutParams();
		imageParams.setMargin(getPaddingOrMargin(imageMargin));

		if (IMAGE_GRAVITY_TOP.equals(imageGravity)) {
			groupView.setOrientation(GlobalConstants.ATTR_VERTICAL);
			groupView.addView(iv);
			groupView.addView(mTextView);
			imageParams.setGravity(UIEngineGroupView.GravityCenter_horizontal);
			textParams.setGravity(UIEngineGroupView.GravityCenter_horizontal);
			if (!TextUtils.isEmpty(space)) {
				imageParams
						.setMargin(new int[] {
								0,
								0,
								0,
								DisplayUtil.dip2px(mContext,
										Float.parseFloat(space)) });
			}
		} else if (IMAGE_GRAVITY_BOTTOM.equals(imageGravity)) {
			groupView.addView(mTextView);
			groupView.addView(iv);
			imageParams.setGravity(UIEngineGroupView.GravityCenter_horizontal
					| UIEngineGroupView.GravityBottom);
			textParams.setGravity(UIEngineGroupView.GravityCenter_horizontal);
			groupView.setOrientation(GlobalConstants.ATTR_VERTICAL);
			if (!TextUtils.isEmpty(space)) {
				imageParams.setMargin(new int[] { 0,
						DisplayUtil.dip2px(mContext, Float.parseFloat(space)),
						0, 0 });
			}
		} else if (IMAGE_GRAVITY_RIGHT.equals(imageGravity)) {
			groupView.addView(mTextView);
			groupView.addView(iv);
			imageParams.setGravity(UIEngineGroupView.GravityCenter_vertical
					| UIEngineGroupView.GravityRight);
			textParams.setGravity(UIEngineGroupView.GravityCenter_vertical);
			groupView.setOrientation(GlobalConstants.ATTR_HORIZONTAL);
			if (!TextUtils.isEmpty(space)) {
				imageParams.setMargin(new int[] {
						DisplayUtil.dip2px(mContext, Float.parseFloat(space)),
						0, 0, 0 });
			}
		} else {
			groupView.addView(iv);
			groupView.addView(mTextView);
			imageParams.setGravity(UIEngineGroupView.GravityCenter_vertical);
			textParams.setGravity(UIEngineGroupView.GravityCenter_vertical);
			groupView.setOrientation(GlobalConstants.ATTR_HORIZONTAL);
			if (!TextUtils.isEmpty(space)) {
				imageParams.setMargin(new int[] { 0, 0,
						DisplayUtil.dip2px(mContext, Float.parseFloat(space)),
						0 });
			}
		}
		setImageSize(iv, imageParams);
		setImage(iv, attrMap);
		iv.setScaleType(ScaleType.FIT_XY);
	}

	@Override
	public void setAttributes(Map<String, String> updateAttributeMap) {
		super.setAttributes(updateAttributeMap);
		if (updateAttributeMap != null && updateAttributeMap instanceof Map) {
			if (updateAttributeMap.containsKey("image")
					|| updateAttributeMap.containsKey("selectedImage")
					|| updateAttributeMap.containsKey("pressImage")
					|| updateAttributeMap.containsKey("disabledImage"))
				setImage(iv, attrMap);
		}
	}

	public void setImage(String attr) {
		if (!TextUtils.isEmpty(attr)) {
			attrMap.put(ATTR_IMAGE, attr);
			setImage(iv, attrMap);
		}
	}

	public void setOnCheckedChangeListener(
			OnCheckedChangeListener selectedChangerListener) {
		mSelectedChangerListener = selectedChangerListener;
	}

	public void setSpace(String space) {
		// if (TextUtils.isEmpty(space)){
		// space="0";
		// }
		// boolean m= space.matches("\\d+");
		// if (m) {
		// int iSpace = Integer.parseInt(space);
		// String imageGravity =
		// attrMap.get(GlobalConstants.ATTR_IMAGE_GRAVITY);
		// LinearLayout. LayoutParams paramsTv =
		// (android.widget.LinearLayout.LayoutParams)mTextView.getLayoutParams();
		// if (IMAGE_GRAVITY_TOP.equals(imageGravity)) {
		// paramsTv.topMargin = iSpace;
		// } else if (IMAGE_GRAVITY_BOTTOM.equals(imageGravity)) {
		// paramsTv.bottomMargin = iSpace;
		// } else if (IMAGE_GRAVITY_RIGHT.equals(imageGravity)) {
		// paramsTv.rightMargin = iSpace;
		// } else {
		// paramsTv.leftMargin = iSpace;
		// }
		// mTextView.setLayoutParams(paramsTv);
		// }
	}

	// boolean isChecked =false ;
	public void checked(boolean isChecked) {
		setSelected(isChecked);
		iv.setSelected(isChecked);
		mTextView.setSelected(isChecked);
		if (mSelectedChangerListener != null) {
			mSelectedChangerListener.onCheckedChanged(this, isChecked);
		}
	}

	@Override
	public void setDisable(boolean disable) {
		super.setDisable(disable);
		iv.setEnabled(!disable);
		mTextView.setEnabled(!disable);
	}

	public interface OnCheckedChangeListener {
		public void onCheckedChanged(MImageButton xImageButton,
				boolean isChecked);
	}

	public void setImageAnim(float fromDegrees, float toDegrees) {
		RotateAnimation myAnimation_Rotate = null;
		myAnimation_Rotate = new RotateAnimation(fromDegrees, toDegrees,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		myAnimation_Rotate.setFillAfter(true);
		myAnimation_Rotate.setDuration(300);
		if (iv != null) {
			iv.startAnimation(myAnimation_Rotate);
		}
	}

	public String getText() {
		return mTextView == null ? "" : mTextView.getText().toString();
	}
}
