package com.logansoft.UIEngine.parse.field;

import org.w3c.dom.Element;

import com.logansoft.UIEngine.fragment.BaseFragment;
import com.logansoft.UIEngine.parse.xmlview.GroupView;
import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

import android.graphics.Canvas;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

/**
 * Provide an expandable layout.
 * 
 * @author Reginald
 */
public class FolderView extends GroupView {
	private RelativeLayout mFolderLayout;
	private ValueAnimator mAnimator;
	private AnimatorUpdateListener mUnfoldAnimatorUpdateListener;
	private AnimatorUpdateListener mFoldAnimatorUpdateListener;
	private int mExpandableHeight = Integer.MAX_VALUE;
	private int mExpandableIndex;
	private static final long ANITMATION_DURATION = 500;

	public FolderView(BaseFragment fragment,GroupView parentView,Element element) {
        super(fragment,parentView,element);
		mAnimator = ValueAnimator.ofInt(0, 100);
		mAnimator.setDuration(ANITMATION_DURATION);
		mUnfoldAnimatorUpdateListener = new AnimatorUpdateListener() {
			private DecelerateInterpolator mInterpolator = new DecelerateInterpolator();
			private IntEvaluator mIntEvaluator = new IntEvaluator();
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				int animatedValue = (Integer) animator.getAnimatedValue();
				final float interpolation = mInterpolator.getInterpolation((float) (animatedValue / 100.0));
				View expandableView = mFolderLayout.getChildAt(mExpandableIndex);
				expandableView.getLayoutParams().height = mIntEvaluator.evaluate(interpolation, 0, mExpandableHeight);
				expandableView.requestLayout();
			}
		};
		mFoldAnimatorUpdateListener = new AnimatorUpdateListener() {
			private DecelerateInterpolator mInterpolator = new DecelerateInterpolator();
			private IntEvaluator mIntEvaluator = new IntEvaluator();
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				int animatedValue = (Integer) animator.getAnimatedValue();
				float interpolation = mInterpolator.getInterpolation((float) (animatedValue / 100.0));
				View expandableView = mFolderLayout.getChildAt(mExpandableIndex);
				expandableView.getLayoutParams().height = mIntEvaluator.evaluate(interpolation, mExpandableHeight, 0);
				expandableView.requestLayout();
			}
		};
	}
	@Override
    protected void createMyView(){
		mView = mFolderLayout = new RelativeLayout(mContext){
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
		mExpandableIndex = 1; // As default, the second group is the expandable part.
		mFolderLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mExpandableHeight == Integer.MAX_VALUE) {
					mExpandableHeight = mFolderLayout.getChildAt(mExpandableIndex).getMeasuredHeight();
				}
			}
		});
		setOrientation("VERTICAL");
	}

	/**
	 * Unfold the expandable area.
	 */
	public void unfold() {
		if (mExpandableHeight == Integer.MAX_VALUE) {
			mView.postDelayed(new Runnable() {
				@Override
				public void run() {
					unfold();
				}
			}, 50);
		} else {
			mView.post(new Runnable() {
				@Override
				public void run() {
					mAnimator.removeAllUpdateListeners();
					mAnimator.addUpdateListener(mUnfoldAnimatorUpdateListener);
					mAnimator.start();
				}
			});
		}
	}

	/**
	 * Fold the expandable area.
	 */
	public void fold() {
		if (mExpandableHeight == Integer.MAX_VALUE) {
			mView.postDelayed(new Runnable() {
				@Override
				public void run() {
					fold();
				}
			}, 50);
		} else {
			mView.post(new Runnable() {
				@Override
				public void run() {
					mAnimator.removeAllListeners();
					mAnimator.addUpdateListener(mFoldAnimatorUpdateListener);
					mAnimator.start();
				}
			});
		}
	}
	 
    @Override
    public void Destory(){
    	mFolderLayout=null;
    	mAnimator=null;
    	mUnfoldAnimatorUpdateListener=null;
    	mFoldAnimatorUpdateListener=null;
    	super.Destory();
    }
}
