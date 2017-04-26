package com.gatsby.scratchcard.scratchcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 一个拥有易容术的view
 * Created by chenhuigu on 17/4/24.
 */

public class PolymorphView extends FrameLayout {
    private static final int DEFAULT_SIZE = 1;
    private int measuredHeight;
    private int measuredWidth;
    private ShadeView mShade;

    public PolymorphView(Context context) {
        this(context, null);
    }

    public PolymorphView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolymorphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mShade = new ShadeView(context);
        addView(mShade);
    }

    /**
     * 设置被遮罩层
     * 避免多次调用
     *
     * @param v
     */
    public void setInlayer(View v) {
        addView(v, 0);
    }

    public void setMeasureInfo(int measuredWidth, int measuredHeight) {
        this.measuredHeight = verifyMeasureInfo(measuredHeight);
        this.measuredWidth = verifyMeasureInfo(measuredWidth);
        mShade.setMeasureInfo(this.measuredWidth, this.measuredHeight);
    }

    private int verifyMeasureInfo(int val) {
        return val <= 0 ? DEFAULT_SIZE : val;
    }

    public void setGestureListener(ShadeView.IGesture mGestureListener) {
        mShade.setGestureListener(mGestureListener);
    }

    /**
     * 是否开启计算器
     *
     * @param usePercentCounter
     */
    public void setUsePercentCounter(boolean usePercentCounter) {
        mShade.setUsePercentCounter(usePercentCounter);
    }

    /**
     * 百分比阈值
     *
     * @param percentThreshold
     */
    public void setPercentThreshold(int percentThreshold) {
        mShade.setPercentThreshold(percentThreshold);
    }

    /**
     * 画笔粗细
     *
     * @param paintStroke
     */
    public void setPaintStroke(int paintStroke) {
        mShade.setPaintStroke(paintStroke);
    }

    /**
     * 移动超过某个距离才响应 可以避免频繁响应
     *
     * @param basisMove
     */
    public void setBasisMove(int basisMove) {
        mShade.setBasisMove(basisMove);
    }

    public void setSurfaceBitmap(Bitmap mSurfaceBitmap) {
        mShade.setSurfaceBitmap(mSurfaceBitmap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
