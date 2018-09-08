package com.style.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 自定义部分滚动View
 */

public class BloodPressureChart extends View {
    private final String TAG = this.getClass().getSimpleName();

    private Paint mAxisPaint;
    private TextPaint mLabelYPaint;
    private TextPaint mLabelXPaint;
    private Paint mChartPaint;
    private TextPaint mValuePaint;
    private int colorChart = 0xFF45CE7B;
    private int colorLabelY = 0xffaaaaaa;
    private int colorGrid = 0x99CCCCCC;
    private int colorChartText = 0xff666666;
    /**
     * 坐标文本高度
     */
    private float labelXHeight, labelYHeight;
    private int mViewHeight, mViewWidth;

    /**
     * 边距
     */
    private float mPadding;
    /**
     * 网格宽高
     */
    private float mYaxisHeight, mXaxisWidth;
    /**
     * 纵坐标文本高度
     */
    private float mYTextWidth;
    /**
     * 柱子宽度
     */
    private float mItemWidth;
    /**
     * 柱子间间隔宽度
     */
    private float mIntervalWidth;
    private ArrayList<BloodItem> mItemList = new ArrayList<>();
    private static final float DEFAULT_MIN = 40F;
    private static final float DEFAULT_MAX = 160F;
    private static final float DEFAULT_MIN_SECOND = 0F;
    private static final float DEFAULT_MAX_SECOND = 200F;
    private float yMin = DEFAULT_MIN;
    private float yMax = DEFAULT_MAX;
    // 速度追踪
    private VelocityTracker velocityTracker = VelocityTracker.obtain();

    public BloodPressureChart(Context context) {
        this(context, null);
    }

    public BloodPressureChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BloodPressureChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(getContext(), new DecelerateInterpolator());

        mPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        mItemWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        mIntervalWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, getResources().getDisplayMetrics());
        init(context);
        //setData(getData());
    }

    private void init(Context context) {
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setStyle(Paint.Style.STROKE);
        mAxisPaint.setDither(true);//防抖动
        mAxisPaint.setShader(null);//设置渐变色
        mAxisPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, getResources().getDisplayMetrics()));
        mAxisPaint.setColor(colorGrid);

        mLabelYPaint = new TextPaint();
        mLabelYPaint.setAntiAlias(true);
        mLabelYPaint.setTextAlign(Paint.Align.CENTER);
        mLabelYPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mLabelYPaint.setColor(colorLabelY);
        labelYHeight = mLabelYPaint.getFontMetrics().bottom - mLabelYPaint.getFontMetrics().top;
        mYTextWidth = mLabelYPaint.measureText("200");

        mLabelXPaint = new TextPaint(mLabelYPaint);
        labelXHeight = mLabelXPaint.getFontMetrics().bottom - mLabelXPaint.getFontMetrics().top;

        mValuePaint = new TextPaint(mLabelYPaint);
        mValuePaint.setColor(colorChartText);

        mChartPaint = new Paint();
        mChartPaint.setAntiAlias(true);
        mChartPaint.setStyle(Paint.Style.FILL);
        //mChartPaint.setStrokeJoin(Paint.Join.ROUND);// 笔刷图形样式
        //mChartPaint.setStrokeCap(Paint.Cap.ROUND);// 设置画笔转弯的连接风格
        mChartPaint.setDither(true);//防抖动
        mChartPaint.setShader(null);//设置渐变色
        mChartPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics()));
        mChartPaint.setColor(colorChart);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
        mXaxisWidth = mViewWidth - mPadding * 2 - mYTextWidth;
        mYaxisHeight = mViewHeight - mPadding * 2 - labelXHeight;
        Log.e(TAG, "onMeasure--" + mViewWidth + "  " + mViewHeight);
        setMeasuredDimension(mViewWidth, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawGridsAndYLabel(canvas);
        drawPolyAndXLabel(canvas);
    }

    private void drawGridsAndYLabel(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.save();
        canvas.translate(mPadding + mYTextWidth, mPadding);
        //画竖线
        float xGridWidth = mXaxisWidth / 4;
        for (int i = 0; i <= 4; i++) {
            canvas.drawLine(xGridWidth * i, 0, xGridWidth * i, mYaxisHeight, mAxisPaint);
        }
        //画横线
        float yGridHeight = mYaxisHeight / 4;
        for (int i = 0; i <= 4; i++) {
            int y = (int) (yGridHeight * i);
            canvas.drawLine(0, y, mXaxisWidth, y, mAxisPaint);
            int yLabel = (int) (yMax - (yMax - yMin) / 4 * i);
            canvas.drawText(String.valueOf(yLabel), -mYTextWidth, y + labelYHeight / 3, mLabelYPaint);
        }
        canvas.restore();
    }

    private void drawPolyAndXLabel(Canvas canvas) {
        if (mItemList == null || mItemList.isEmpty()) {
            //drawEmpty(canvas);
            return;
        }
        canvas.save();
        canvas.translate(mPadding + mYTextWidth, mPadding + mYaxisHeight);

        BloodItem item;
        //内部边距，防止柱子与y轴左右边界线重合
        float innerPadding = 50f;
        float originalX;
        float nowX;
        //LinearGradient gradient;
        for (int i = 0; i < mItemList.size(); i++) {
            item = mItemList.get(i);
            originalX = (innerPadding + (mItemWidth + mIntervalWidth) * i);
            nowX = (originalX + mOffset);
            if (nowX >= 0 && nowX <= mXaxisWidth - innerPadding) {
                if (item.yLow > yMin && item.yHigh < yMax) {
                    //gradient = new LinearGradient(nowX, item.sY, nowX, item.dY, new int[]{0xff4fa213, 0xff91c532}, null, Shader.TileMode.MIRROR);
                    //mChartPaint.setShader(gradient);
                    float top = -(mYaxisHeight * (item.yHigh - yMin)) / (yMax - yMin);
                    float bottom = -(mYaxisHeight * (item.yLow - yMin)) / (yMax - yMin);
                    canvas.drawRect(nowX - mItemWidth / 2, top, nowX + mItemWidth / 2, bottom, mChartPaint);
                    canvas.drawText(String.valueOf(item.yHigh), nowX, getBaseLine((int) (top - labelXHeight), (int) top, mValuePaint.getFontMetricsInt()), mValuePaint);
                    canvas.drawText(String.valueOf(item.yLow), nowX, getBaseLine((int) bottom, (int) (bottom + labelXHeight), mValuePaint.getFontMetricsInt()), mValuePaint);
                }
                canvas.drawText(item.xLabel, nowX, getBaseLine(0, (int) (labelXHeight + mPadding), mLabelXPaint.getFontMetricsInt()), mLabelXPaint);
            }
        }
        canvas.restore();
    }

    /**
     * 根据矩形区域换算文字的BaseLine
     * 绘制在top与bottom中间的文字
     *
     * @return
     */
    private int getBaseLine(int top, int bottom, Paint.FontMetricsInt metricsInt) {
        return (top + bottom - metricsInt.bottom - metricsInt.top) / 2;
    }

    public void setData(List<BloodItem> list) {
        mItemList.clear();
        yMin = DEFAULT_MIN;
        yMax = DEFAULT_MAX;
        if (list != null && !list.isEmpty()) {
            for (BloodItem item : list) {
                if (item.yHigh > DEFAULT_MAX || item.yLow < DEFAULT_MIN) {
                    yMax = DEFAULT_MAX_SECOND;
                    yMin = DEFAULT_MIN_SECOND;
                }
            }
            mItemList.addAll(list);
            mMinOffset = -((mItemWidth + mIntervalWidth) * mItemList.size() - mXaxisWidth);
        }
        invalidate();
    }

    private List<BloodItem> getData() {
        ArrayList<BloodItem> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            BloodItem b = new BloodItem(random.nextInt(30) + 50, random.nextInt(30) + 90, String.valueOf(i));
            list.add(b);
        }
        return list;
    }

    /**
     * 偏移量最大值，最小值，当前偏移量,由于向左滑，所以最大值为0，如果最小值大于或等于0表示不需要移动
     */
    private float mMaxOffset = 0, mMinOffset = 0, mOffset = 0;
    private float mLastX;
    private boolean mCanRefresh;
    private Scroller mScroller;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getRawX();
                if (mScroller.computeScrollOffset())
                    mScroller.forceFinished(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mMinOffset < 0) {
                    float rawXMove = event.getRawX();
                    // 计算偏移量
                    float offsetX = rawXMove - mLastX;
                    // 在当前偏移量的基础上增加偏移量
                    mOffset = mOffset + offsetX;
                    setCanRefresh();
                    // 偏移量修改后下次重绘会有变化
                    mLastX = rawXMove;
                    if (mCanRefresh)
                        invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // 计算速度的单位时间,最大速度为5px/ms
                velocityTracker.computeCurrentVelocity(1, 5);
                float mVelocityX = velocityTracker.getXVelocity();
                // 计算完成后回收内存
                velocityTracker.clear();
                //velocityTracker.recycle();
                Log.e(TAG, "mVelocityX = " + mVelocityX + " px/ms");
                //偏移量已经是边界时不用再计算滚动逻辑
                if (mOffset > mMinOffset && mOffset < mMaxOffset && mVelocityX != 0) {
                    float dx;//速度越大滚动距离理应越大,假设速度为5px/ms时，最大滑动位移5000px，设置花费时间为3000ms。以此为标准.速度越大，位移越大，时间越长。
                    int duration;
                    dx = mVelocityX / (5f / 5000f);
                    duration = (int) Math.abs(mVelocityX / (5f / 3000f));
                    Log.e(TAG, "dx = " + dx + " px" + "  duration = " + duration + " ms");
                    /*if (mVelocityX > 0) {//向右滑
                        duration = (dx / mVelocityX);
                    } else {//左滑，内容左移，偏移量应该减小，这里设置负数
                        duration = (dx / mVelocityX);
                    }*/
                    if (dx != 0) {
                        //scroller.getCurrX() = mStartX + Math.round(x * dx);  x等于从0逐渐增大到1.
                        mScroller.startScroll((int) mOffset, 0, (int) dx, 0, duration < 500 ? 500 : duration);//duration太小会有跳动效果，不平滑
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            if (x >= mMinOffset && x <= mMaxOffset) {
                mOffset = x;
                setCanRefresh();
                if (mCanRefresh)
                    postInvalidate();
            }
        } else {
            mScroller.forceFinished(true);
        }
    }

    /**
     * 对偏移量进行边界值判定
     */
    private void setCanRefresh() {
        mOffset = mOffset >= mMaxOffset ? mMaxOffset : mOffset;
        mOffset = mOffset <= mMinOffset ? mMinOffset : mOffset;
        //Log.i(TAG, "mOffset = " + mOffset);
        if (mOffset >= mMinOffset && mOffset <= mMaxOffset) {
            mCanRefresh = true;
        } else {
            mCanRefresh = false;
        }
    }

    public static class BloodItem {
        public int yLow;
        public int yHigh;
        public String xLabel;

        public BloodItem(int yLow, int yHigh, String xLabel) {
            this.yLow = yLow;
            this.yHigh = yHigh;
            this.xLabel = xLabel;
        }
    }
}