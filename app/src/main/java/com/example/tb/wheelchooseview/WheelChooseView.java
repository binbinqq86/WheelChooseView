package com.example.tb.wheelchooseview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
        scaleTextAlpha = scaleTextPadding = scaleTextSize = 2f;
        centerTextPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        centerTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, getResources().getDisplayMetrics());
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
    }
    
    public void setDataList(List<String> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        dataList = list;
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
        for (int i = -center; i <= center; i++) {
            int realIndex = i + currIndex;
            if (realIndex >= 0 && realIndex < size) {
                //开始绘制文本=====================================
                
                //区分中间跟边缘文本的颜色
                if (i == 0) {
                    textPaint.setColor(centerTextColor);
                } else {
                    textPaint.setColor(otherTextColor);
                }
                
                //计算textPadding缩放等级
                float tempScalePadding = centerTextPadding - Math.abs(i) * (centerTextPadding - centerTextPadding / scaleTextPadding) / (maxShowNum / 2);
                //计算textSize缩放等级
                float tempScaleSize = centerTextSize - Math.abs(i) * (centerTextSize - centerTextSize / scaleTextSize) / (maxShowNum / 2);
                //计算textAlpha缩放等级
                float tempScaleAlpha = 255 - Math.abs(i) * (255 - 255 / scaleTextAlpha) / (maxShowNum / 2);
//                Log.e(TAG, "onDraw: "+tempScaleAlpha+"$"+tempScalePadding+"$"+tempScaleSize );
                textPaint.setTextSize(tempScaleSize);
                textPaint.setAlpha((int) tempScaleAlpha);
                
                String text = dataList.get(realIndex);
                float tempY=getHeight()/2+i*tempScalePadding*2;
                canvas.drawText(text,getWidth()/2,tempY,textPaint);
            }
        }
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
}
