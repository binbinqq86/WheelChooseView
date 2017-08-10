package com.example.tb.wheelchooseview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by : tb on 2017/8/4 上午9:49.
 * Description :仿IOS滚轮选择控件
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
     * 当前选中位置，默认选中第一个
     */
    private int currIndex = 5;
    /**
     * 最大显示数据个数，默认5个
     */
    private int maxShowNum = 3;
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
    private static final float DEFAULT_MAX_ROTATE_DEGREE = 60f;
    /**
     * 默认字体大小，单位：sp
     */
    private static final float DEFAULT_TEXT_SIZE = 30f;
    /**
     * 默认字体padding，单位：dp
     */
    private static final float DEFAULT_TEXT_PADDING = 6f;
    /**
     * 默认缩小比例
     */
    private static final float DEFAULT_TEXT_SCALE = 2f;
    
    private Camera camera;
    private Matrix matrix;
    /**
     * 控件的中心坐标
     */
    private float centerY, centerX;
    /**
     * 画分割线的paint
     */
    private Paint linePaint;
    /**
     * 是否绘制中间的分割线
     */
    private boolean hasSeparateLine = true;
    
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
        
        if (maxShowNum % 2 == 0) {
            throw new IllegalArgumentException("=====maxShowNum can't be even number======");
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        centerX = width / 2f;
        centerY = height / 2f;
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
        
        int size = dataList.size();
        int center = maxShowNum / 2;
        float eachCenterYPre = 0f;
        float eachCenterYCurr;
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
            float tempScalePadding = centerTextPadding - Math.abs(i) * (centerTextPadding - centerTextPadding / scaleTextPadding) / center;
            //计算textSize缩放等级
            float tempScaleSize = centerTextSize - Math.abs(i) * (centerTextSize - centerTextSize / scaleTextSize) / center;
            //计算textAlpha缩放等级
            float tempScaleAlpha = 255f - Math.abs(i) * (255f - 255f / scaleTextAlpha) / center;
            //计算绕x轴旋转的角度
            float tempScaleRotateDegree = -DEFAULT_MAX_ROTATE_DEGREE * i / center;
//            Log.e(TAG, "onDraw: " + tempScaleAlpha + "$" + tempScalePadding + "$" + tempScaleSize + "$" + tempScaleRotateDegree);
            textPaint.setTextSize(tempScaleSize);
            textPaint.setAlpha((int) tempScaleAlpha);

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
            
            // 调节中心点
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
            
            canvas.concat(matrix);
            
            //=======================计算baseLine===============================
            float tempScalePaddingPre = centerTextPadding - Math.abs(i - 1) * (centerTextPadding - centerTextPadding / scaleTextPadding) / (maxShowNum / 2);
            float tempScaleSizePre = centerTextSize - Math.abs(i - 1) * (centerTextSize - centerTextSize / scaleTextSize) / (maxShowNum / 2);
            textPaintPre.setTextSize(tempScaleSizePre);
            
            String text = "";//默认绘制空文本，占位
            int realIndex = i + currIndex;
            if (realIndex >= 0 && realIndex < size) {
                text = dataList.get(realIndex);
            }
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float fontHeight = fontMetrics.bottom - fontMetrics.top;
            
            Paint.FontMetrics fontMetricsPre = textPaintPre.getFontMetrics();
            float fontHeightPre = fontMetricsPre.bottom - fontMetricsPre.top;
            
            if (i == -center) {
                eachCenterYPre = (height - contentHeight) / 2f + tempScalePadding + fontHeight / 2f;
                eachCenterYCurr = eachCenterYPre;
            } else {
                eachCenterYCurr = eachCenterYPre + tempScalePaddingPre + fontHeightPre / 2f + tempScalePadding + fontHeight / 2f;
                eachCenterYPre = eachCenterYCurr;
            }
            //baseLine计算参考：http://blog.csdn.net/harvic880925/article/details/50423762
            float baseline = eachCenterYCurr + (fontMetrics.bottom - fontMetrics.top) / 2f - fontMetrics.bottom;
            canvas.drawText(text, width / 2, baseline, textPaint);
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
    }
    
    private void calculateContentHeight() {
        int center = maxShowNum / 2;
        TextPaint tp = new TextPaint();
        for (int i = -center; i <= center; i++) {
            //计算textPadding缩放等级，上下分别的padding，不是两者的和
            float tempScalePadding = centerTextPadding - Math.abs(i) * (centerTextPadding - centerTextPadding / scaleTextPadding) / center;
            //计算textSize缩放等级
            float tempScaleSize = centerTextSize - Math.abs(i) * (centerTextSize - centerTextSize / scaleTextSize) / center;
            tp.setTextSize(tempScaleSize);
            Paint.FontMetrics fontMetrics = tp.getFontMetrics();
            contentHeight += fontMetrics.bottom - fontMetrics.top + 2 * tempScalePadding;
        }
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
        this.currIndex = currIndex;
        return this;
    }
    
    public int getMaxShowNum() {
        return maxShowNum;
    }
    
    public WheelChooseView setMaxShowNum(int maxShowNum) {
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
    
    public WheelChooseView setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
        return this;
    }
    
    public int getOtherTextColor() {
        return otherTextColor;
    }
    
    public WheelChooseView setOtherTextColor(int otherTextColor) {
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
