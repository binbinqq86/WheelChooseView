package com.example.tb.wheelchooseview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by : tb on 2017/8/4 上午9:49.
 * Description :高仿IOS滚轮选择控件
 */
public class WheelChooseView extends View {
    private static final String TAG = "WheelChooseView";
    private TextPaint textPaint;
    private List<String> dataList = new ArrayList<>();
    /**
     * 以中间为核心，与最边缘文本的缩放比例：包含padding，size，alpha(均大于等于1)
     */
    private float scaleTextPadding, scaleTextSize, scaleTextAlpha;
    /**
     * 当前选中位置
     */
    private int currIndex;
    /**
     * 最大显示数据个数，默认5个
     */
    private int maxShowNum = 5;
    /**
     * 是否是循环滚动模式
     */
    private boolean isRecycleMode = false;
    /**
     * 中间文本的颜色
     */
    private int centerTextColor = Color.BLUE;
    /**
     * 其他文本的颜色
     */
    private int otherTextColor = Color.BLACK;
    /**
     * 中间文本的大小
     */
    private int centerTextSize;
    /**
     * 中间文本的padding
     */
    private float centerTextPadding;
    /**
     * 控件宽高
     */
    private int width, height;
    /**
     * 绘制的文本的总高度
     */
    private float contentHeight;
    /**
     * 文字绕x轴最大旋转角度
     */
    private static final float DEFAULT_MAX_ROTATE_DEGREE = 75f;
    /**
     * 默认字体大小，单位：sp
     */
    private static final float DEFAULT_TEXT_SIZE = 30f;
    /**
     * 默认字体padding，单位：dp
     */
    private static final float DEFAULT_TEXT_PADDING = 3f;
    /**
     * 默认缩小比例
     */
    private static final float DEFAULT_TEXT_SCALE = 2f;
    /**
     * 所有textSize集合
     */
    private float[] eachTextSize;
    /**
     * 所有padding集合
     */
    private float[] eachTextPadding;
    /**
     * 所有alpha集合
     */
    private float[] eachTextAlpha;
    /**
     * 所有rotate集合
     */
    private float[] eachTextRotate;
    /**
     * 所有textHeight集合
     */
    private float[] eachTextHeight;
    
    private Camera camera;
    private Matrix matrix;
    /**
     * 每个文本的中心坐标
     */
    private float centerX;
    /**
     * 画分割线的paint
     */
    private Paint linePaint;
    /**
     * 是否绘制中间的分割线
     */
    private boolean hasSeparateLine = true;
    /**
     * 第一次按下的y坐标
     */
    private float mFirstY;
    /**
     * 偏移的y轴距离
     */
    private float offsetY;
    /**
     * 偏移的个数
     */
    private int offsetIndex;
    /**
     * 作为参考标准的
     */
    private float textHeight;
    
    public WheelChooseView(Context context) {
        this(context, null);
    }
    
    public WheelChooseView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public WheelChooseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WheelChooseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    
    private void init(Context mContext) {
        scaleTextAlpha = scaleTextPadding = scaleTextSize = DEFAULT_TEXT_SCALE;
        centerTextPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TEXT_PADDING, getResources().getDisplayMetrics());
        centerTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, getResources().getDisplayMetrics());
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(0xff000000);
        linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics()));
        
        camera = new Camera();
        matrix = new Matrix();
        
        currIndex = maxShowNum / 2;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        centerX = width / 2f;
    }
    
    /**
     * 需放在所有set后调用
     *
     * @param list
     */
    public void setDataList(List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        dataList = list;
        eachTextPadding = new float[maxShowNum];
        eachTextSize = new float[maxShowNum];
        eachTextHeight = new float[maxShowNum];
        eachTextAlpha = new float[maxShowNum];
        eachTextRotate = new float[maxShowNum];
        calculateContentHeight();
        invalidate();
    }
    
    public List<String> getDataList() {
        return dataList;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataList == null || dataList.size() == 0) {
            return;
        }
        
        //裁剪真实绘制区域
        float top = (height - contentHeight) / 2f;
        canvas.clipRect(getPaddingLeft(), top, width - getPaddingRight(), top + contentHeight);
        
        int size = dataList.size();
        int center = maxShowNum / 2;
        float eachCenterYPre = 0f;
        float eachCenterYCurr = 0f;
        TextPaint textPaintPre = new TextPaint(textPaint);
        for (int i = -center; i <= center; i++) {
            //开始绘制文本=====================================
            //区分中间跟边缘文本的颜色
            if (i == 0) {
                textPaint.setColor(centerTextColor);
            } else {
                textPaint.setColor(otherTextColor);
            }
            
            //计算textPadding缩放等级，上下分别的padding，不是两者的和
            float tempScalePadding = eachTextPadding[i + center];
            //计算textSize缩放等级
            float tempScaleSize = eachTextSize[i + center];
            //计算textAlpha缩放等级
            float tempScaleAlpha = eachTextAlpha[i + center];
            //计算绕x轴旋转的角度
            float tempScaleRotateDegree = eachTextRotate[i + center];
//            Log.e(TAG, "onDraw: " + tempScaleAlpha + "$" + tempScalePadding + "$" + tempScaleSize + "$" + tempScaleRotateDegree);
            
            //根据实际滑动距离，计算二次缩放比例
            float delta = offsetY % textHeight / textHeight;
//            Log.e(TAG, i+"===111111onDraw: "+tempScaleSize);
            if (i > 0) {
                tempScaleSize -= tempScaleSize * delta;
                tempScalePadding -= tempScalePadding * delta;
                tempScaleAlpha -= tempScaleAlpha * delta;
                tempScaleRotateDegree += tempScaleRotateDegree * delta;
            } else if (i < 0) {
                tempScaleSize += tempScaleSize * delta;
                tempScalePadding += tempScalePadding * delta;
                tempScaleAlpha += tempScaleAlpha * delta;
                tempScaleRotateDegree -= tempScaleRotateDegree * delta;
            }
//            Log.e(TAG, i+"===222222onDraw: "+tempScaleSize);
            //==========================================================
            if (tempScaleSize < eachTextSize[maxShowNum - 1]) {
                //小于最小值处理
                tempScaleSize = eachTextSize[maxShowNum - 1];
            }
            if (tempScaleSize > eachTextSize[maxShowNum / 2]) {
                //大于最大值处理
                tempScaleSize = eachTextSize[maxShowNum / 2];
            }
            //==========================================================
            if (tempScalePadding < eachTextPadding[maxShowNum - 1]) {
                //小于最小值处理
                tempScalePadding = eachTextPadding[maxShowNum - 1];
            }
            if (tempScalePadding > eachTextPadding[maxShowNum / 2]) {
                //大于最大值处理
                tempScalePadding = eachTextPadding[maxShowNum / 2];
            }
            
            //==========================================================
            if (tempScaleAlpha < eachTextAlpha[maxShowNum - 1]) {
                //小于最小值处理
                tempScaleAlpha = eachTextAlpha[maxShowNum - 1];
            }
            if (tempScaleAlpha > eachTextAlpha[maxShowNum / 2]) {
                //大于最大值处理
                tempScaleAlpha = eachTextAlpha[maxShowNum / 2];
            }
            
            //==========================================================
            if (tempScaleRotateDegree < eachTextRotate[maxShowNum - 1]) {
                //小于最小值处理
                tempScaleRotateDegree = eachTextRotate[maxShowNum - 1];
            }
            if (tempScaleRotateDegree > eachTextRotate[0]) {
                //大于最大值处理
                tempScaleRotateDegree = eachTextRotate[0];
            }
//            Log.e(TAG, i+"===333333onDraw: "+tempScaleSize);
            
            textPaint.setTextSize(tempScaleSize);
            textPaint.setAlpha((int) tempScaleAlpha);
            
            //=======================计算baseLine===============================
            int in = i == -center ? i : (i - 1);
//            float tempScalePaddingPre = centerTextPadding - Math.abs(i - 1) * (centerTextPadding - centerTextPadding / scaleTextPadding) / (maxShowNum / 2);
            float tempScalePaddingPre = eachTextPadding[in + center];
//            float tempScaleSizePre = centerTextSize - Math.abs(i - 1) * (centerTextSize - centerTextSize / scaleTextSize) / (maxShowNum / 2);
            float tempScaleSizePre = eachTextSize[in + center];
            textPaintPre.setTextSize(tempScaleSizePre);
            
            String text = "";//默认绘制空文本，占位
            int realIndex = i + currIndex - offsetIndex;
            if (realIndex >= 0 && realIndex < size) {
                text = dataList.get(realIndex);
            }
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float fontHeight = fontMetrics.bottom - fontMetrics.top;
            
            Paint.FontMetrics fontMetricsPre = textPaintPre.getFontMetrics();
            float fontHeightPre = fontMetricsPre.bottom - fontMetricsPre.top;
            
            if (i == -center) {
                eachCenterYPre = top + tempScalePadding + fontHeight / 2f;
//                Log.e(TAG, "onDraw: " + eachCenterYPre + "#" + eachCenterYCurr);
                eachCenterYCurr = eachCenterYPre;
            } else {
                eachCenterYCurr = eachCenterYPre + tempScalePaddingPre + fontHeightPre / 2f + tempScalePadding + fontHeight / 2f;
//                Log.e(TAG, "onDraw: " + eachCenterYPre + "#" + eachCenterYCurr);
                eachCenterYPre = eachCenterYCurr;
            }
            
            eachCenterYCurr += offsetY % textHeight;
            
            //baseLine计算参考：http://blog.csdn.net/harvic880925/article/details/50423762
            float baseline = eachCenterYCurr + (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom;
            
            canvas.save();
            matrix.reset();
            camera.save();
            camera.rotateX(tempScaleRotateDegree);
            camera.getMatrix(matrix);
            camera.restore();
            
            //修正失真，主要修改 MPERSP_0 和 MPERSP_1
            float[] mValues = new float[9];
            matrix.getValues(mValues);//获取数值
            mValues[6] = mValues[6] / getResources().getDisplayMetrics().density;//数值修正
            mValues[7] = mValues[7] / getResources().getDisplayMetrics().density;//数值修正
            matrix.setValues(mValues);//重新赋值
            
            //调节中心点，每个文本各自的中心点
            matrix.preTranslate(-centerX, -eachCenterYCurr);
            matrix.postTranslate(centerX, eachCenterYCurr);
            
            canvas.concat(matrix);
            canvas.drawText(text, width / 2, baseline, textPaint);
            canvas.restore();
        }
        //绘制分割线
        if (hasSeparateLine) {
            textPaint.setTextSize(centerTextSize);
            Paint.FontMetrics cFontMetrics = textPaint.getFontMetrics();
            float centerFontHeight = cFontMetrics.bottom - cFontMetrics.top;
            float y1 = height / 2f - centerTextPadding - centerFontHeight / 2f;
            float y2 = height / 2f + centerTextPadding + centerFontHeight / 2f;
            canvas.drawLine(0, y1, width, y1, linePaint);
            canvas.drawLine(0, y2, width, y2, linePaint);
        }
        Paint p = new Paint(linePaint);
        p.setColor(Color.YELLOW);
        canvas.drawLine(0, top, width, top, p);
        canvas.drawLine(0, top + contentHeight, width, top + contentHeight, p);
    }
    
    /**
     * 根据滑动距离，刷新视图
     */
    private void refreshView() {
        //这里按照第一个元素的高度，当滑过这个高度时就认为偏移了一个元素
        int i = (int) (offsetY / textHeight);
        if (isRecycleMode || (currIndex - i >= 0 && currIndex - i < dataList.size())) {
            offsetIndex = i;
            postInvalidate();
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFirstY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                offsetY = event.getY() - mFirstY;
                refreshView();
                break;
            case MotionEvent.ACTION_UP:
            default:
//                offsetY = 0;
                break;
        }
        return true;
    }
    
    /**
     * 计算实际绘制区域的高度，即所有文本的总高度
     */
    private void calculateContentHeight() {
        int center = maxShowNum / 2;
        TextPaint tp = new TextPaint();
        for (int i = -center; i <= center; i++) {
            //计算textPadding缩放等级，上下分别的padding，不是两者的和
            float tempScalePadding = centerTextPadding - Math.abs(i) * (centerTextPadding - centerTextPadding / scaleTextPadding) / center;
            //计算textSize缩放等级
            float tempScaleSize = centerTextSize - Math.abs(i) * (centerTextSize - centerTextSize / scaleTextSize) / center;
            //计算textAlpha缩放等级
            float tempScaleAlpha = 255f - Math.abs(i) * (255f - 255f / scaleTextAlpha) / center;
            //计算绕x轴旋转的角度
            float tempScaleRotateDegree = -DEFAULT_MAX_ROTATE_DEGREE * i / center;
            
            tp.setTextSize(tempScaleSize);
            Paint.FontMetrics fontMetrics = tp.getFontMetrics();
            eachTextHeight[i + center] = fontMetrics.bottom - fontMetrics.top + 2 * tempScalePadding;
            contentHeight += eachTextHeight[i + center];
            
            eachTextSize[i + center] = tempScaleSize;
            eachTextPadding[i + center] = tempScalePadding;
            eachTextAlpha[i + center] = tempScaleAlpha;
            eachTextRotate[i + center] = tempScaleRotateDegree;
        }
        textHeight=eachTextHeight[0];
//        Log.e(TAG, "calculateContentHeight: "+contentHeight );
    }
    
    public float getScaleTextPadding() {
        return scaleTextPadding;
    }
    
    public WheelChooseView setScaleTextPadding(float scaleTextPadding) {
        this.scaleTextPadding = scaleTextPadding;
        return this;
    }
    
    public float getScaleTextSize() {
        return scaleTextSize;
    }
    
    public WheelChooseView setScaleTextSize(float scaleTextSize) {
        this.scaleTextSize = scaleTextSize;
        return this;
    }
    
    public float getScaleTextAlpha() {
        return scaleTextAlpha;
    }
    
    public WheelChooseView setScaleTextAlpha(float scaleTextAlpha) {
        this.scaleTextAlpha = scaleTextAlpha;
        return this;
    }
    
    public int getCurrIndex() {
        return currIndex;
    }
    
    public WheelChooseView setCurrIndex(int currIndex) {
        if (currIndex < 0) {
            currIndex = 0;
        }
        this.currIndex = currIndex;
        return this;
    }
    
    public int getMaxShowNum() {
        return maxShowNum;
    }
    
    /**
     * 只支持奇数
     *
     * @param maxShowNum
     * @return
     */
    public WheelChooseView setMaxShowNum(int maxShowNum) {
        if (maxShowNum % 2 == 0) {
            throw new IllegalArgumentException("=====maxShowNum can't be even number======");
        }
        this.maxShowNum = maxShowNum;
        return this;
    }
    
    public boolean isRecycleMode() {
        return isRecycleMode;
    }
    
    public WheelChooseView setRecycleMode(boolean recycleMode) {
        isRecycleMode = recycleMode;
        return this;
    }
    
    public int getCenterTextColor() {
        return centerTextColor;
    }
    
    public WheelChooseView setCenterTextColor(@ColorInt int centerTextColor) {
        this.centerTextColor = centerTextColor;
        return this;
    }
    
    public int getOtherTextColor() {
        return otherTextColor;
    }
    
    public WheelChooseView setOtherTextColor(@ColorInt int otherTextColor) {
        this.otherTextColor = otherTextColor;
        return this;
    }
    
    public int getCenterTextSize() {
        return centerTextSize;
    }
    
    public WheelChooseView setCenterTextSize(int centerTextSize) {
        this.centerTextSize = centerTextSize;
        return this;
    }
    
    public float getCenterTextPadding() {
        return centerTextPadding;
    }
    
    public WheelChooseView setCenterTextPadding(float centerTextPadding) {
        this.centerTextPadding = centerTextPadding;
        return this;
    }
    
    public boolean isHasSeparateLine() {
        return hasSeparateLine;
    }
    
    public WheelChooseView setHasSeparateLine(boolean hasSeparateLine) {
        this.hasSeparateLine = hasSeparateLine;
        return this;
    }
}
