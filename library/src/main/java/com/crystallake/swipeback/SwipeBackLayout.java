/**
 * Created by : yds
 * Time: 2020-11-15 7:45 PM
 */
package com.crystallake.swipeback;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.List;

public class SwipeBackLayout extends FrameLayout {

    public static final String TAG = SwipeBackLayout.class.getSimpleName();

    /**
     * 拖拽边缘
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DragEdge.FROM_LEFT, DragEdge.FROM_TOP, DragEdge.FROM_RIGHT, DragEdge.FROM_BOTTOM})
    public @interface DragEdge {
        int FROM_LEFT = 0;
        int FROM_TOP = 1;
        int FROM_RIGHT = 2;
        int FROM_BOTTOM = 3;
    }

    //视图拖拽帮助类
    private ViewDragHelper mViewDragHelper;

    private View mPreviousDecorView;
    //前一个子视图
    private View mPreviousChildView;
    //
    private boolean mSwipeBackEnable = true;

    private boolean mActivitySwiping = false;

    private boolean mBackSuccess = false;

    private boolean mTakeOverActivityExitAnimRunning = false;

    /**
     * 距离左边的位移
     */
    private int mLeftOffset = 0;
    /**
     * 距离右边的位移
     */
    private int mTopOffset = 0;

    /**
     * 当前activity
     */
    private Activity mCurrentActivity;
    /**
     * 当前子视图
     */
    private View mCurrentChildView;
    /**
     * activity是否已经透明
     */
    private boolean mActivityIsAlreadyTranslucent = false;

    private SwipeBackTransformer mSwipeBackTransformer;
    private SwipeBackListener mSwipeBackListener;

    /**
     * activity是否透明
     */
    private boolean mActivityTranslucent = true;

    /**
     * 是否只从边缘返回
     */
    private boolean mSwipeBackOnlyEdge = false;
    /**
     * 是否强制从边缘返回
     */
    private boolean mSwipeBackForceEdge = true;
    /**
     * 返回方向
     */
    @DragEdge
    private int mSwipeBackDirection = DragEdge.FROM_LEFT;
    private int mTouchedEdge = ViewDragHelper.INVALID_POINTER;

    private GradientDrawable mShadowDrawable = null;

    private boolean mShadowEnable = true;
    private int mShadowStartColor = 0;
    private int mShadowSize = 0;
    private int mMaskAlpha = 150;


    private float mAutoFinishedVelocityLimit = 2000f;

    private List<View> mInnerScrollViews;
    private boolean mTouchInnerScrollView = false;

    private int mTouchSlop;

    /**
     * 手势按下时距离屏幕坐标轴原点的x轴距离
     */
    private float mDownX;
    /**
     * 手势按下时距离屏幕坐标轴原点的y轴距离
     */
    private float mDownY;
    private int mWidth;
    private int mHeight;

    private Rect mShadowRect = new Rect();

    private float mFraction;
    private float mSwipeBackFactor = 0.5f;
    private long mTakeOverActivityEnterExitAnimDuration = 300;

    private boolean mTakeOverActivityEnterExitAnim = false;

    private ValueAnimator mTakeOverActivityEnterAnimator;
    private ValueAnimator mTakeOverActivityExitAnimator;

    public SwipeBackLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);//重写onDraw方法
        //1.0f 乘数，用于确定帮助者对检测拖动开始的敏感程度。 值越大，越敏感。 1.0f是正常的。
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        mViewDragHelper.setEdgeTrackingEnabled(mSwipeBackDirection);
        mTouchSlop = mViewDragHelper.getTouchSlop();
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final int shadowStartColor = Color.argb(30, 0, 0, 0);
        final int shadowWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, context.getResources().getDisplayMetrics());
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeBackLayout);
        mSwipeBackEnable = typedArray.getBoolean(R.styleable.SwipeBackLayout_sbl_enable, mSwipeBackEnable);
        mActivityIsAlreadyTranslucent = typedArray.getBoolean(R.styleable.SwipeBackLayout_sbl_activityIsAlreadyTranslucent, mActivityIsAlreadyTranslucent);
        mTakeOverActivityEnterExitAnim = typedArray.getBoolean(R.styleable.SwipeBackLayout_sbl_takeOverActivityAnim, mTakeOverActivityEnterExitAnim);
        mTakeOverActivityEnterExitAnimDuration = typedArray.getInteger(R.styleable.SwipeBackLayout_sbl_takeOverActivityAnimDuration, (int) mTakeOverActivityEnterExitAnimDuration);
        mSwipeBackForceEdge = typedArray.getBoolean(R.styleable.SwipeBackLayout_sbl_forceEdge, mSwipeBackForceEdge);
        mSwipeBackOnlyEdge = typedArray.getBoolean(R.styleable.SwipeBackLayout_sbl_onlyEdge, mSwipeBackOnlyEdge);
        mShadowEnable = typedArray.getBoolean(R.styleable.SwipeBackLayout_sbl_shadowEnable, mShadowEnable);
        mShadowStartColor = typedArray.getColor(R.styleable.SwipeBackLayout_sbl_shadowStartColor, shadowStartColor);
        mShadowSize = (int) typedArray.getDimension(R.styleable.SwipeBackLayout_sbl_shadowSize, shadowWidth);
        mMaskAlpha = typedArray.getInteger(R.styleable.SwipeBackLayout_sbl_maskAlpha, mMaskAlpha);
        mAutoFinishedVelocityLimit = typedArray.getFloat(R.styleable.SwipeBackLayout_sbl_autoFinishedVelocityLimit, mAutoFinishedVelocityLimit);
        mSwipeBackFactor = typedArray.getFloat(R.styleable.SwipeBackLayout_sbl_factor, mSwipeBackFactor);
        mSwipeBackDirection = typedArray.getInt(R.styleable.SwipeBackLayout_sbl_direction, mSwipeBackDirection);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSwipeBackEnable()) {
            canvas.drawARGB(mMaskAlpha - (int) (mMaskAlpha * mFraction), 0, 0, 0);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (!isSwipeBackEnable()) {
            return super.drawChild(canvas, child, drawingTime);
        }
        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (child == mCurrentChildView) {
            drawShadow(canvas, child);
        }
        return ret;
    }

    @NonNull
    private GradientDrawable getNonNullShadowDrawable() {
        if (mShadowDrawable == null) {
            int[] colors = new int[]{mShadowStartColor, Color.TRANSPARENT};
            if (mSwipeBackDirection == DragEdge.FROM_LEFT) {
                mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
                mShadowDrawable.setSize(mShadowSize, 0);
            } else if (mSwipeBackDirection == DragEdge.FROM_RIGHT) {
                mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                mShadowDrawable.setSize(mShadowSize, 0);
            } else if (mSwipeBackDirection == DragEdge.FROM_TOP) {
                mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
                mShadowDrawable.setSize(0, mShadowSize);
            } else if (mSwipeBackDirection == DragEdge.FROM_BOTTOM) {
                mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                mShadowDrawable.setSize(0, mShadowSize);
            } else {
                mShadowDrawable = new GradientDrawable();
                mShadowDrawable.setSize(0, 0);
            }
        }
        return mShadowDrawable;
    }

    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mShadowRect;
        child.getHitRect(childRect);
        if (mShadowEnable) {
            final Drawable shadow = getNonNullShadowDrawable();
            if (mSwipeBackDirection == DragEdge.FROM_LEFT) {
                shadow.setBounds(childRect.left - shadow.getIntrinsicWidth(), childRect.top, childRect.left, childRect.bottom);
            } else if (mSwipeBackDirection == DragEdge.FROM_RIGHT) {
                shadow.setBounds(childRect.left, childRect.top, childRect.left, childRect.bottom);
            } else if (mSwipeBackDirection == DragEdge.FROM_TOP) {
                shadow.setBounds(childRect.left, childRect.top - shadow.getIntrinsicHeight(), childRect.left, childRect.bottom);
            } else if (mSwipeBackDirection == DragEdge.FROM_BOTTOM) {
                shadow.setBounds(childRect.left, childRect.top, childRect.left, childRect.bottom);
            }
            mShadowDrawable.setAlpha((int) ((1 - mFraction) * 255));
            mShadowDrawable.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            if (!isSwipeBackEnable()) {
                super.onLayout(changed, left, top, right, bottom);
                return;
            }
            int l = getPaddingLeft() + mLeftOffset;
            int t = getPaddingTop() + mTopOffset;
            int r = l + mCurrentChildView.getMeasuredWidth();
            int b = t + mCurrentChildView.getMeasuredHeight();
            mCurrentChildView.layout(l, t, r, b);
            if (changed) {
                mWidth = getWidth();
                mHeight = getHeight();
            }
        } catch (Exception e) {
            super.onLayout(changed, left, top, right, bottom);
        }
    }

    public interface SwipeBackListener {
        void onSwiping(Activity currentActivity, View currentView, View previousView, float swipeBackFraction, float swipeBackFactor, @DragEdge int swipeDirection);

        void onFinish(Activity currentActivity, View currentView, View previousView, boolean backSuccess, @DragEdge int swipeDirection);
    }

    public void attachTo(Activity activity) {
        setVisibility(INVISIBLE);
        mCurrentActivity = activity;
        activity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        activity.getWindow().getDecorView().setBackground(null);
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
        int background = a.getResourceId(0, 0);
        a.recycle();
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View decorChild = decorView.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decorView.removeAllViews();
        addViewInLayout(decorChild, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mCurrentChildView = decorChild;
        decorView.addView(this);
        findPreviousChildView();
        if (mPreviousDecorView != null && mTakeOverActivityEnterExitAnim) {
            setActivityTranslucent(true);
            activity.overridePendingTransition(0, 0);
        }
        startEnterAnim();
    }

    private void startEnterAnim() {
        if (mPreviousDecorView != null) {
            if (mTakeOverActivityEnterExitAnim) {
                if (mTakeOverActivityExitAnimator != null) {
                    mTakeOverActivityExitAnimator.pause();
                    mTakeOverActivityExitAnimator = null;
                }
                mFraction = 1;
                mTakeOverActivityEnterAnimator = ValueAnimator.ofFloat(mFraction, 0);
                if (mTakeOverActivityEnterExitAnimDuration < 0L) {
                    mTakeOverActivityEnterExitAnimDuration = mPreviousDecorView.getResources().getInteger(R.integer.swipeback_activity_duration);
                }
                mTakeOverActivityEnterAnimator.setDuration(mTakeOverActivityEnterExitAnimDuration);
                mTakeOverActivityEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        if (getVisibility() != View.VISIBLE) {
                            setVisibility(View.VISIBLE);
                        }
                        mLeftOffset = 0;
                        mTopOffset = 0;
                        mFraction = (float) animation.getAnimatedValue();
                        if (mSwipeBackDirection == DragEdge.FROM_LEFT) {
                            mLeftOffset = (int) (mWidth - mWidth * (1 - mFraction));
                        } else if (mSwipeBackDirection == DragEdge.FROM_RIGHT) {
                            mLeftOffset = (int) -(mWidth - mWidth * (1 - mFraction));
                        } else if (mSwipeBackDirection == DragEdge.FROM_TOP) {
                            mTopOffset = (int) (mHeight - mHeight * (1 - mFraction));
                        } else if (mSwipeBackDirection == DragEdge.FROM_BOTTOM) {
                            mTopOffset = (int) -(mHeight - mHeight * (1 - mFraction));
                        }
                        getNonNullSwipeBackTransformer().transform(mCurrentChildView, mPreviousDecorView, mFraction, mSwipeBackDirection);
                        requestLayout();
                    }
                });
                post(new Runnable() {
                    @Override
                    public void run() {
                        mTakeOverActivityEnterAnimator.start();
                    }
                });
            } else {
                setVisibility(View.VISIBLE);
                mFraction = 0;
                mLeftOffset = 0;
                mTopOffset = 0;
                getNonNullSwipeBackTransformer().transform(mCurrentChildView, mPreviousDecorView, 1, mSwipeBackDirection);
                requestLayout();
            }
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    public void startExitAnim() {
        if (mPreviousDecorView != null) {
            if (mTakeOverActivityEnterExitAnim) {
                if (mTakeOverActivityEnterAnimator != null) {
                    mTakeOverActivityEnterAnimator.pause();
                    mTakeOverActivityEnterAnimator = null;
                }
                if (mTakeOverActivityExitAnimRunning) {
                    return;
                }
                mTakeOverActivityExitAnimRunning = true;
                mTakeOverActivityExitAnimator = ValueAnimator.ofFloat(mFraction, 1);
                if (mTakeOverActivityEnterExitAnimDuration < 0L) {
                    mTakeOverActivityEnterExitAnimDuration = mPreviousDecorView.getResources().getInteger(R.integer.swipeback_activity_duration);
                }
                mTakeOverActivityExitAnimator.setDuration(mTakeOverActivityEnterExitAnimDuration);
                mTakeOverActivityExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mLeftOffset = 0;
                        mTopOffset = 0;
                        mFraction = (float) animation.getAnimatedValue();
                        if (mSwipeBackDirection == DragEdge.FROM_LEFT) {
                            mLeftOffset = (int) (mWidth - mWidth * (1 - mFraction));
                        } else if (mSwipeBackDirection == DragEdge.FROM_RIGHT) {
                            mLeftOffset = (int) -(mWidth - mWidth * (1 - mFraction));
                        } else if (mSwipeBackDirection == DragEdge.FROM_TOP) {
                            mTopOffset = (int) (mHeight - mHeight * (1 - mFraction));
                        } else if (mSwipeBackDirection == DragEdge.FROM_BOTTOM) {
                            mTopOffset = (int) -(mHeight - mHeight * (1 - mFraction));
                        }
                        getNonNullSwipeBackTransformer().transform(mCurrentChildView, mPreviousDecorView, mFraction, mSwipeBackDirection);
                        requestLayout();
                    }
                });
                mTakeOverActivityExitAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mCurrentActivity != null) {
                            mCurrentActivity.finish();
                            mCurrentActivity.overridePendingTransition(0, 0);
                        }
                        mTakeOverActivityExitAnimRunning = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                mTakeOverActivityExitAnimator.start();
            } else {
                mFraction = 1;
                mLeftOffset = 0;
                mTopOffset = 0;
                getNonNullSwipeBackTransformer().transform(mCurrentChildView, mPreviousDecorView, 1, mSwipeBackDirection);
                requestLayout();
            }
        }
    }


    public ViewDragHelper getViewDragHelper() {
        return mViewDragHelper;
    }

    public void setViewDragHelper(ViewDragHelper viewDragHelper) {
        mViewDragHelper = viewDragHelper;
    }

    public View getPreviousDecorView() {
        return mPreviousDecorView;
    }

    public void setPreviousDecorView(View previousDecorView) {
        mPreviousDecorView = previousDecorView;
    }

    public View getPreviousChildView() {
        return mPreviousChildView;
    }

    public void setPreviousChildView(View previousChildView) {
        mPreviousChildView = previousChildView;
    }

    public boolean isActivitySwiping() {
        return mActivitySwiping;
    }

    public void setActivitySwiping(boolean activitySwiping) {
        mActivitySwiping = activitySwiping;
    }

    public boolean isBackSuccess() {
        return mBackSuccess;
    }

    public void setBackSuccess(boolean backSuccess) {
        mBackSuccess = backSuccess;
    }

    public boolean isTakeOverActivityExitAnimRunning() {
        return mTakeOverActivityExitAnimRunning;
    }

    public void setTakeOverActivityExitAnimRunning(boolean takeOverActivityExitAnimRunning) {
        mTakeOverActivityExitAnimRunning = takeOverActivityExitAnimRunning;
    }

    public int getLeftOffset() {
        return mLeftOffset;
    }

    public void setLeftOffset(int leftOffset) {
        mLeftOffset = leftOffset;
    }

    public int getTopOffset() {
        return mTopOffset;
    }

    public void setTopOffset(int topOffset) {
        mTopOffset = topOffset;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        mCurrentActivity = currentActivity;
    }

    public View getCurrentChildView() {
        return mCurrentChildView;
    }

    public void setCurrentChildView(View currentChildView) {
        mCurrentChildView = currentChildView;
    }

    public boolean isActivityIsAlreadyTranslucent() {
        return mActivityIsAlreadyTranslucent;
    }

    public void setActivityIsAlreadyTranslucent(boolean activityIsAlreadyTranslucent) {
        mActivityIsAlreadyTranslucent = activityIsAlreadyTranslucent;
    }

    public SwipeBackTransformer getSwipeBackTransformer() {
        return mSwipeBackTransformer;
    }

    public void setSwipeBackTransformer(SwipeBackTransformer swipeBackTransformer) {
        mSwipeBackTransformer = swipeBackTransformer;
    }

    public SwipeBackListener getSwipeBackListener() {
        return mSwipeBackListener;
    }

    public void setSwipeBackListener(SwipeBackListener swipeBackListener) {
        mSwipeBackListener = swipeBackListener;
    }

    public boolean isActivityTranslucent() {
        if (mCurrentActivity == null) {
            return true;
        }
        return mActivityTranslucent;
    }

    public boolean isSwipeBackOnlyEdge() {
        return mSwipeBackOnlyEdge;
    }

    public void setSwipeBackOnlyEdge(boolean swipeBackOnlyEdge) {
        mSwipeBackOnlyEdge = swipeBackOnlyEdge;
    }

    public boolean isSwipeBackForceEdge() {
        return mSwipeBackForceEdge;
    }

    public void setSwipeBackForceEdge(boolean swipeBackForceEdge) {
        mSwipeBackForceEdge = swipeBackForceEdge;
    }

    public int getSwipeBackDirection() {
        return mSwipeBackDirection;
    }

    public void setSwipeBackDirection(@DragEdge int swipeBackDirection) {
        mSwipeBackDirection = swipeBackDirection;
        mViewDragHelper.setEdgeTrackingEnabled(swipeBackDirection);
    }

    public int getTouchedEdge() {
        return mTouchedEdge;
    }

    public void setTouchedEdge(int touchedEdge) {
        mTouchedEdge = touchedEdge;
    }

    public boolean isShadowEnable() {
        return mShadowEnable;
    }

    public void setShadowEnable(boolean shadowEnable) {
        mShadowEnable = shadowEnable;
    }

    public int getShadowStartColor() {
        return mShadowStartColor;
    }

    public void setShadowStartColor(@ColorInt int shadowStartColor) {
        mShadowStartColor = shadowStartColor;
    }

    public int getShadowSize() {
        return mShadowSize;
    }

    public void setShadowSize(int shadowSize) {
        mShadowSize = shadowSize;
    }

    public int getMaskAlpha() {
        return mMaskAlpha;
    }

    public void setMaskAlpha(@IntRange(from = 0, to = 255) int maskAlpha) {
        if (maskAlpha > 255) {
            maskAlpha = 255;
        } else if (maskAlpha < 0) {
            maskAlpha = 0;
        }
        mMaskAlpha = maskAlpha;
    }

    public float getAutoFinishedVelocityLimit() {
        return mAutoFinishedVelocityLimit;
    }

    public void setAutoFinishedVelocityLimit(@FloatRange(from = 0.0f, to = 1.0f) float autoFinishedVelocityLimit) {
        mAutoFinishedVelocityLimit = autoFinishedVelocityLimit;
    }

    public List<View> getInnerScrollViews() {
        return mInnerScrollViews;
    }

    public void setInnerScrollViews(List<View> innerScrollViews) {
        mInnerScrollViews = innerScrollViews;
    }

    public boolean isTouchInnerScrollView() {
        return mTouchInnerScrollView;
    }

    public void setTouchInnerScrollView(boolean touchInnerScrollView) {
        mTouchInnerScrollView = touchInnerScrollView;
    }

    public int getTouchSlop() {
        return mTouchSlop;
    }

    public void setTouchSlop(int touchSlop) {
        mTouchSlop = touchSlop;
    }

    public float getDownX() {
        return mDownX;
    }

    public void setDownX(float downX) {
        mDownX = downX;
    }

    public float getDownY() {
        return mDownY;
    }

    public void setDownY(float downY) {
        mDownY = downY;
    }

    public float getFraction() {
        return mFraction;
    }

    public void setFraction(float fraction) {
        mFraction = fraction;
    }

    public float getSwipeBackFactor() {
        return mSwipeBackFactor;
    }

    public void setSwipeBackFactor(@FloatRange(from = 0.0f, to = 1.0f) float swipeBackFactor) {
        if (swipeBackFactor > 1) {
            swipeBackFactor = 1;
        } else if (swipeBackFactor < 0) {
            swipeBackFactor = 0;
        }
        this.mSwipeBackFactor = swipeBackFactor;
    }

    public long getTakeOverActivityEnterExitAnimDuration() {
        return mTakeOverActivityEnterExitAnimDuration;
    }

    public void setTakeOverActivityEnterExitAnimDuration(long takeOverActivityEnterExitAnimDuration) {
        mTakeOverActivityEnterExitAnimDuration = takeOverActivityEnterExitAnimDuration;
    }

    public boolean isTakeOverActivityEnterExitAnim() {
        return mTakeOverActivityEnterExitAnim;
    }

    public void setTakeOverActivityEnterExitAnim(boolean takeOverActivityEnterExitAnim) {
        mTakeOverActivityEnterExitAnim = takeOverActivityEnterExitAnim;
    }

    public ValueAnimator getTakeOverActivityEnterAnimator() {
        return mTakeOverActivityEnterAnimator;
    }

    public void setTakeOverActivityEnterAnimator(ValueAnimator takeOverActivityEnterAnimator) {
        mTakeOverActivityEnterAnimator = takeOverActivityEnterAnimator;
    }

    public ValueAnimator getTakeOverActivityExitAnimator() {
        return mTakeOverActivityExitAnimator;
    }

    public void setTakeOverActivityExitAnimator(ValueAnimator takeOverActivityExitAnimator) {
        mTakeOverActivityExitAnimator = takeOverActivityExitAnimator;
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            //TODO
            findPreviousChildView();
            if (isSwipeBackEnable()) {
                mActivitySwiping = true;
                setActivityTranslucent(true);
                return child == mCurrentChildView;
            }
            return false;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            mLeftOffset = getPaddingLeft();
            if (isSwipeEnabled()) {
                if (mSwipeBackDirection == DragEdge.FROM_LEFT) {
                    if (!SwipeBackCompat.canViewScrollLeft(mInnerScrollViews, mDownX, mDownY, false)) {
                        mLeftOffset = Math.min(Math.max(left, getPaddingLeft()), mWidth);
                    } else {
                        if (mSwipeBackForceEdge && mTouchedEdge == ViewDragHelper.EDGE_LEFT) {
                            mLeftOffset = Math.min(Math.max(left, getPaddingLeft()), mWidth);
                        }
                    }
                } else if (mSwipeBackDirection == DragEdge.FROM_RIGHT) {
                    if (!SwipeBackCompat.canViewScrollRight(mInnerScrollViews, mDownX, mDownY, false)) {
                        mLeftOffset = Math.min(Math.max(left, -mWidth), getPaddingRight());
                    } else {
                        if (mSwipeBackForceEdge && mTouchedEdge == ViewDragHelper.EDGE_RIGHT) {
                            mLeftOffset = Math.min(Math.max(left, -mWidth), getPaddingRight());
                        }
                    }
                }
            }
            return mLeftOffset;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            mTopOffset = getPaddingTop();
            if (isSwipeEnabled()) {
                if (mSwipeBackDirection == DragEdge.FROM_TOP) {
                    if (!SwipeBackCompat.canViewScrollUp(mInnerScrollViews, mDownX, mDownY, false)) {
                        mTopOffset = Math.min(Math.max(top, getPaddingTop()), mHeight);
                    } else {
                        if (mSwipeBackForceEdge && mTouchedEdge == ViewDragHelper.EDGE_TOP) {
                            mTopOffset = Math.min(Math.max(top, getPaddingTop()), mHeight);
                        }
                    }
                } else if (mSwipeBackDirection == DragEdge.FROM_BOTTOM) {
                    if (!SwipeBackCompat.canViewScrollDown(mInnerScrollViews, mDownX, mDownY, false)) {
                        mTopOffset = Math.min(Math.max(top, -mHeight), getPaddingBottom());
                    } else {
                        if (mSwipeBackForceEdge && mTouchedEdge == ViewDragHelper.EDGE_BOTTOM) {
                            mTopOffset = Math.min(Math.max(top, -mHeight), getPaddingBottom());
                        }
                    }
                }
            }
            Log.i(TAG, "clampViewPositionVertical -> " + "mTopOffset=" + mTopOffset);
            return mTopOffset;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            left = Math.abs(left);
            top = Math.abs(top);
            switch (mSwipeBackDirection) {
                case DragEdge.FROM_LEFT:
                case DragEdge.FROM_RIGHT:
                    mFraction = 1.0f * left / mWidth;
                    break;
                case DragEdge.FROM_TOP:
                case DragEdge.FROM_BOTTOM:
                    mFraction = 1.0f * top / mHeight;
                    break;
                default:
                    break;
            }
            onSwiping();

        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            mLeftOffset = mTopOffset = 0;
            if (!isSwipeEnabled()) {
                mTouchedEdge = ViewDragHelper.INVALID_POINTER;
                return;
            }
            mTouchedEdge = ViewDragHelper.INVALID_POINTER;
            boolean isBackToEnd = backJudgeBySpeed(xvel, yvel) || mFraction >= mSwipeBackFactor;
            if (isBackToEnd) {
                switch (mSwipeBackDirection) {
                    case DragEdge.FROM_LEFT:
                        smoothScrollToX(mWidth);
                        break;
                    case DragEdge.FROM_TOP:
                        smoothScrollToY(mHeight);
                        break;
                    case DragEdge.FROM_RIGHT:
                        smoothScrollToX(-mWidth);
                        break;
                    case DragEdge.FROM_BOTTOM:
                        smoothScrollToY(-mHeight);
                        break;
                    default:
                        break;

                }
            } else {
                switch (mSwipeBackDirection) {
                    case DragEdge.FROM_LEFT:
                    case DragEdge.FROM_RIGHT:
                        smoothScrollToX(getPaddingLeft());
                        break;
                    case DragEdge.FROM_TOP:
                    case DragEdge.FROM_BOTTOM:
                        smoothScrollToY(getPaddingTop());
                        break;
                    default:
                        break;
                }
            }

        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE) {
                if (mFraction == 0) {
                    onFinish(false);
                } else if (mFraction == 1) {
                    onFinish(true);
                }
            }
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            Log.i(TAG, "getViewHorizontalDragRange -> " + "mWidth" + mWidth);
            return mWidth;
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            Log.i(TAG, "getViewVerticalDragRange -> " + "mHeight" + mHeight);
            return mHeight;
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
            //边缘Touch状态 开始滑动
            mTouchedEdge = edgeFlags;
            Log.i(TAG, "onEdgeTouched -> " + "mTouchedEdge" + mTouchedEdge);
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
            Log.i(TAG, "onEdgeDragStarted -> " + "mTouchedEdge" + mTouchedEdge);
        }
    }


    private void smoothScrollToX(int finalLeft) {
        if (mViewDragHelper.settleCapturedViewAt(finalLeft, getPaddingTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    @SuppressLint("NewApi")
    protected void onSwiping() {
        if (mTakeOverActivityEnterAnimator != null) {
            mTakeOverActivityEnterAnimator.pause();
        }
        if (mTakeOverActivityExitAnimator != null) {
            mTakeOverActivityExitAnimator.pause();
        }
        invalidate();
        if (mPreviousDecorView != null) {
            getNonNullSwipeBackTransformer().transform(mCurrentChildView, mPreviousDecorView, mFraction, mSwipeBackDirection);
        }
        if (mSwipeBackListener != null) {
            mSwipeBackListener.onSwiping(mCurrentActivity, mCurrentChildView, mPreviousDecorView, mFraction, mSwipeBackFactor, mSwipeBackDirection);
        }
    }


    @NonNull
    public SwipeBackTransformer getNonNullSwipeBackTransformer() {
        if (mSwipeBackTransformer == null) {
            mSwipeBackTransformer = new ParallaxSwipeBackTransformer();
        }
        return mSwipeBackTransformer;
    }

    public interface SwipeBackTransformer {
        void transform(View currentView, View previousView, float fraction, @DragEdge int swipeDirection);
    }

    private void smoothScrollToY(int finalTop) {
        if (mViewDragHelper.settleCapturedViewAt(getPaddingLeft(), finalTop)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private boolean backJudgeBySpeed(float xVal, float yVal) {
        switch (mSwipeBackDirection) {
            case DragEdge.FROM_LEFT:
                return xVal > mAutoFinishedVelocityLimit;
            case DragEdge.FROM_TOP:
                return yVal > mAutoFinishedVelocityLimit;
            case DragEdge.FROM_RIGHT:
                return xVal < -mAutoFinishedVelocityLimit;
            case DragEdge.FROM_BOTTOM:
                return yVal < -mAutoFinishedVelocityLimit;
            default:
                break;

        }
        return false;
    }

    protected void onFinish(boolean backSuccess) {
        mActivitySwiping = false;
        mBackSuccess = backSuccess;
        if (mBackSuccess) {
            Log.i(TAG, "onFinish -> " + "mBackSuccess=" + mBackSuccess);
            finish();
        } else {
            Log.i(TAG, "onFinish -> " + "mBackSuccess=" + mBackSuccess + ",mFraction=" + mFraction);
            if (!mTakeOverActivityEnterExitAnim) {
                setActivityTranslucent(false);
            }
            mFraction = 0;
            mLeftOffset = 0;
            mTopOffset = 0;
            getNonNullSwipeBackTransformer().transform(mCurrentChildView, mPreviousDecorView, 1, mSwipeBackDirection);
            requestLayout();
        }
        if (mSwipeBackListener != null) {
            mSwipeBackListener.onFinish(mCurrentActivity, mCurrentChildView, mPreviousDecorView, backSuccess, mSwipeBackDirection);
        }
    }

    private void finish() {
        mTakeOverActivityExitAnimRunning = false;
        mCurrentActivity.finish();
        mCurrentActivity.overridePendingTransition(0, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isSwipeBackEnable()) {
            return false;
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSwipeBackEnable()) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getRawX();
                mDownY = ev.getRawY();
                mInnerScrollViews = SwipeBackCompat.findAllScrollViews(this);
                mTouchInnerScrollView = SwipeBackCompat.contains(mInnerScrollViews, mDownX, mDownY) != null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mInnerScrollViews != null && mTouchInnerScrollView) {
                    float distanceX = Math.abs(ev.getRawX() - mDownX);
                    float distanceY = Math.abs(ev.getRawY() - mDownY);
                    if (mSwipeBackDirection == DragEdge.FROM_LEFT || mSwipeBackDirection == DragEdge.FROM_RIGHT) {
                        if (distanceY > mTouchSlop && distanceY > distanceX) {
                            return super.onInterceptTouchEvent(ev);
                        }
                    } else if (mSwipeBackDirection == DragEdge.FROM_TOP || mSwipeBackDirection == DragEdge.FROM_BOTTOM) {
                        if (distanceX > mTouchSlop && distanceX > distanceY) {
                            return super.onInterceptTouchEvent(ev);
                        }
                    }
                }
                break;
            default:
                break;
        }
        boolean handled = mViewDragHelper.shouldInterceptTouchEvent(ev);
        return handled || super.onInterceptTouchEvent(ev);
    }

    private boolean isSwipeEnabled() {
        if (mSwipeBackOnlyEdge) {
            switch (mSwipeBackDirection) {
                case DragEdge.FROM_LEFT:
                    return mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT);
                case DragEdge.FROM_TOP:
                    return mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_TOP);
                case DragEdge.FROM_RIGHT:
                    return mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_RIGHT);
                case DragEdge.FROM_BOTTOM:
                    return mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_BOTTOM);
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * 设置activity是否透明
     *
     * @param activityTranslucent true为透明，false不透明
     */
    public void setActivityTranslucent(boolean activityTranslucent) {
        if (mActivityIsAlreadyTranslucent) {
            mActivityTranslucent = true;
        } else {
            mActivityTranslucent = activityTranslucent;
        }
        if (mCurrentActivity != null) {
            if (mActivityTranslucent) {
                SwipeBackCompat.convertActivityToTranslucent(mCurrentActivity);
            } else {
                SwipeBackCompat.convertActivityFromTranslucent(mCurrentActivity);
            }
        }

    }

    private void findPreviousChildView() {
        mPreviousDecorView = null;
        mPreviousChildView = null;
        Activity preActivity = SwipeBackManager.getInstance().getPreviousActivity();
        if (preActivity != null) {
            ViewGroup preDecorView = (ViewGroup) preActivity.getWindow().getDecorView();
            mPreviousDecorView = preDecorView;
            View preDecorChild = preDecorView.getChildAt(0);
            if (preDecorChild instanceof SwipeBackLayout) {
                SwipeBackLayout preSwipeBackLayout = (SwipeBackLayout) preDecorChild;
                mPreviousChildView = preSwipeBackLayout.getChildAt(0);
            }
        }
    }

    public boolean isSwipeBackEnable() {
        return mSwipeBackEnable;
    }

    public void setSwipeBackEnable(boolean swipeBackEnable) {
        mSwipeBackEnable = swipeBackEnable;
    }
}
