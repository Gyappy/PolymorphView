package com.gatsby.scratchcard.scratchcard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 遮罩层
 * Created by chenhuigu on 17/4/20.
 */

public class ShadeView extends View {
    private static final int DEFAULT_SIZE = 1;
    private int measuredHeight = 1;
    private int measuredWidth = 1;

    private IGesture mGestureListener;
    private boolean isInvokedGestureStart;
    private boolean isInit;

    private Path mPath;//手刮动的path，过程
    private Paint mOutterPaint;//绘制mPath的画笔
    private Canvas mCanvas;//临时画布
    private Bitmap mBitmap;//临时图片
    //path每次的开始坐标值
    private int mLastX;
    private int mLastY;
    //手指按下的坐标
    private int downX;
    private int downY;
    //是否完成挂挂交互
    private boolean isComplete = false;
    private int basisMove;
    private int basisWholeMove = 5000;
    private boolean isInvokeCounterThread;

    //    private Bitmap mOutterBitmap;//图片遮罩
    private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    private int defaultSurfaceColor = 0xffc0c0c0;
    private Bitmap mSurfaceBitmap;

    /**
     * 是否启用擦除面积占比的计算器
     */
    private boolean isUsePercentCounter = true;
    /**
     * 擦除一定面积展示全部内容的阈值
     */
    private int percentThreshold = 30;
    /**
     * 滑动的手指粗细
     */
    private int paintStroke = 30;

    public interface IGesture {
        /**
         * 手势开始
         */
        void onGestureStart();

        /**
         * 手势结束
         */
        void onGestureEnd();
    }

    public ShadeView(Context context) {
        super(context);
    }

    public ShadeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化信息
     */
    private void init() {
        isInit = true;
        mOutterPaint = new Paint();
        mPath = new Path();
        //初始化bitmap
        mBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if (getSurfaceBitmap() == null) {
            mCanvas.drawColor(defaultSurfaceColor);
        } else {
            mCanvas.drawBitmap(getSurfaceBitmap(), null, new Rect(0, 0, measuredWidth, measuredHeight), null);
        }
        //设置画笔属性
        setupOutPaint();

    }

    /**
     * 绘制path的画笔属性
     */
    private void setupOutPaint() {
        mOutterPaint.setColor(Color.RED);
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);//设置圆角
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutterPaint.setStyle(Paint.Style.FILL);
        mOutterPaint.setStrokeWidth(paintStroke);//画笔粗细
    }

    /**
     * 设定尺寸
     *
     * @param measuredWidth
     * @param measuredHeight
     */
    public void setMeasureInfo(int measuredWidth, int measuredHeight) {
        this.measuredHeight = verifyMeasureInfo(measuredHeight);
        this.measuredWidth = verifyMeasureInfo(measuredWidth);
        init();
    }

    private int verifyMeasureInfo(int val) {
        return val <= 0 ? DEFAULT_SIZE : val;
    }

    public void setGestureListener(IGesture mGestureListener) {
        this.mGestureListener = mGestureListener;
    }

    /**
     * 是否开启计算器
     *
     * @param usePercentCounter
     */
    public void setUsePercentCounter(boolean usePercentCounter) {
        isUsePercentCounter = usePercentCounter;
    }

    /**
     * 百分比阈值
     *
     * @param percentThreshold
     */
    public void setPercentThreshold(int percentThreshold) {
        this.percentThreshold = percentThreshold;
    }

    /**
     * 画笔粗细
     *
     * @param paintStroke
     */
    public void setPaintStroke(int paintStroke) {
        this.paintStroke = paintStroke;
        setupOutPaint();
    }

    /**
     * 移动超过某个距离才响应 可以避免频繁响应
     *
     * @param basisMove
     */
    public void setBasisMove(int basisMove) {
        this.basisMove = basisMove;
    }

    public Bitmap getSurfaceBitmap() {
        return mSurfaceBitmap;
    }

    public void setSurfaceBitmap(Bitmap mSurfaceBitmap) {
        this.mSurfaceBitmap = mSurfaceBitmap;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //处理wrap_content问题
        int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measuredWidth, measuredHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(measuredWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, measuredHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInit) {
            init();
        }
        mOutterPaint.setStyle(Paint.Style.STROKE);
        mOutterPaint.setXfermode(xfermode);
//        canvas.drawBitmap(mOutterBitmap, 0, 0, null);
        //交互完成就展示完整画布
        if (!isComplete) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            mCanvas.drawPath(mPath, mOutterPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!isInvokedGestureStart) {
                    isInvokedGestureStart = true;
                    if (mGestureListener != null) {
                        mGestureListener.onGestureStart();
                    }
                }
                downX = x;
                downY = y;
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);
                int wx = Math.abs(x - downX);
                int wy = Math.abs(y - downY);
                //滑动超过某个像素才会改变，为了避免很频繁的响应。
                if (dx > basisMove || dy > basisMove) {
                    mPath.lineTo(x, y);
                }
                //一旦超过一定幅度 就允许这次交互开启线程
                if (wx > basisWholeMove || wy > basisWholeMove) {
                    isInvokeCounterThread = true;
                }
                mLastX = x;
                mLastY = y;
                Log.v("ooooooooooo", "-1 " + x + " " + y);
                break;
            case MotionEvent.ACTION_UP:
                if (mGestureListener != null) {
                    mGestureListener.onGestureEnd();
                }
                //如果不使用计算器,抬起手指就展示完整结果
                if (isUsePercentCounter) {
                    if (isInvokeCounterThread) {
                        isInvokeCounterThread = false;
                        new Thread(mRunnable).start();
                    }
                } else {
                    isComplete = true;
                }
                break;
        }
        invalidate();//刷新
        return true;
    }

    /**
     * 处理滑动冲突
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;

        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * 统计擦除区域任务
     */
    private Runnable mRunnable = new Runnable() {
        private int[] mPixels;

        @Override
        public void run() {

            Log.v("ooooooooooo", "1 " + System.currentTimeMillis());
            int w = measuredWidth;
            int h = measuredHeight;

            float wipeArea = 0;
            float totalArea = w * h;

            Bitmap bitmap = mBitmap;

            mPixels = new int[w * h];
            Log.v("ooooooooooo", "2 " + System.currentTimeMillis());
            /**
             * 拿到所有的像素信息
             */
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            Log.v("ooooooooooo", "3 " + System.currentTimeMillis());
            /**
             * 遍历统计擦除的区域
             */
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                        if (wipeArea > 0 && totalArea > 0 && (int) (wipeArea * 100 / totalArea) > percentThreshold) {
                            isComplete = true;
                            postInvalidate();
                            return;
                        }
                    }
                }
            }
            Log.v("ooooooooooo", "4 " + System.currentTimeMillis());
            /**
             * 根据所占百分比，进行一些操作
             */
            if (wipeArea > 0 && totalArea > 0 && !isComplete) {
                int percent = (int) (wipeArea * 100 / totalArea);

                if (percent > percentThreshold) {
                    isComplete = true;
                    postInvalidate();
                }
            }


            Log.v("ooooooooooo", "5 " + System.currentTimeMillis() + " " + wipeArea + " " + totalArea);
        }

    };
}
