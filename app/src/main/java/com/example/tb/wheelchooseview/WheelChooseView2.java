package com.example.tb.wheelchooseview;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
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
 * 第二版：每行文字高度固定相等
 */
public class WheelChooseView2 extends View {
    private static final String TAG = "WheelChooseView";
    private TextPaint textPaint;
    private List<String> dataList = new ArrayList<>();
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
    private static final float DEFAULT_MAX_ROTATE_DEGREE = 60f;
    /**
     * 默认字体大小，单位：sp
     */
    private static final float DEFAULT_TEXT_SIZE = 30f;
    /**
     * 默认字体padding，单位：dp
     */
    private static final float DEFAULT_TEXT_PADDING = 5f;
    /**
     * textHeight
     */
    private float textHeight;
    
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
     * 渐变的蒙层
     */
    private Paint alphaPaint;
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
    private Paint.FontMetrics fontMetrics;
    
    public WheelChooseView2(Context context) {
        this(context, null);
    }
    
    public WheelChooseView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public WheelChooseView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WheelChooseView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    
    private void init(Context mContext) {
        centerTextPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TEXT_PADDING, getResources().getDisplayMetrics());
        centerTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, getResources().getDisplayMetrics());
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(centerTextSize);
        fontMetrics = textPaint.getFontMetrics();
        textHeight = fontMetrics.bottom - fontMetrics.top + 2 * centerTextPadding;
        contentHeight = textHeight * maxShowNum;
        
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(0xff000000);
        linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics()));
        
        alphaPaint = new Paint();
        alphaPaint.setAntiAlias(true);
        
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
        for (int i = -center; i <= center; i++) {
            //开始绘制文本=====================================
            //区分中间跟边缘文本的颜色
            if (i == 0) {
                textPaint.setColor(centerTextColor);
            } else {
                textPaint.setColor(otherTextColor);
            }
            
            //计算绕x轴旋转的角度
            float tempScaleRotateDegree = -DEFAULT_MAX_ROTATE_DEGREE * i / center;
//            Log.e(TAG, "onDraw: " + tempScaleAlpha + "$" + tempScalePadding + "$" + tempScaleSize + "$" + tempScaleRotateDegree);
            
            //根据实际滑动距离，计算二次缩放比例
            float delta = offsetY % textHeight / textHeight;
//            Log.e(TAG, i+"===111111onDraw: "+tempScaleSize);
            if (i > 0) {
                tempScaleRotateDegree += tempScaleRotateDegree * delta;
            } else if (i < 0) {
                tempScaleRotateDegree -= tempScaleRotateDegree * delta;
            }
//            Log.e(TAG, i+"===222222onDraw: "+tempScaleSize);
            //==========================================================
            if (tempScaleRotateDegree < -DEFAULT_MAX_ROTATE_DEGREE) {
                //小于最小值处理
                tempScaleRotateDegree = -DEFAULT_MAX_ROTATE_DEGREE;
            }
            if (tempScaleRotateDegree > DEFAULT_MAX_ROTATE_DEGREE) {
                //大于最大值处理
                tempScaleRotateDegree = DEFAULT_MAX_ROTATE_DEGREE;
            }
//            Log.e(TAG, i+"===333333onDraw: "+tempScaleSize);
            
            //=======================计算baseLine===============================
            float eachCenterYCurr = top + textHeight / 2f + (i + center) * textHeight;
            
            //用temp值去改变和处理绘制===============================
            if (Math.abs(offsetY / textHeight) >= 1) {
                //说明该变换位置了
//                eachCenterYCurr = eachCenterYPre;
            } else {
            }
            eachCenterYCurr += offsetY % textHeight;//此处会蹦，因为一下子变为0了，需要处理
            Log.e(TAG, i + "===onDraw: " + eachCenterYCurr + "#" + offsetY % textHeight);
            //baseLine计算参考：http://blog.csdn.net/harvic880925/article/details/50423762
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
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
            
            String text = "";//默认绘制空文本，占位
            int realIndex = i + currIndex - offsetIndex;
            if (realIndex >= 0 && realIndex < size) {
                text = dataList.get(realIndex);
            }
            canvas.drawText(text, width / 2, baseline, textPaint);//此处也可以改为图片选择器
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

//        Paint p = new Paint(linePaint);
//        p.setColor(Color.YELLOW);
//        canvas.drawLine(0, top, width, top, p);
//        canvas.drawLine(0, top + contentHeight, width, top + contentHeight, p);

//        canvas.drawLine(0, top + textHeight, width, top + textHeight, p);
//        canvas.drawLine(0,top+2*textHeight,width,top+2*textHeight,p);
//        canvas.drawLine(0,top+3*textHeight,width,top+3*textHeight,p);
//        canvas.drawLine(0, top + 4 * textHeight, width, top + 4 * textHeight, p);
        
        //绘制蒙层渐变
        int colors[] = new int[3];
        float positions[] = new float[3];
        
        // 第1个点
        colors[0] = 0xFFFFFFFF;
        positions[0] = 0;
        
        // 第2个点
        colors[1] = 0x00FFFFFF;
        positions[1] = 0.5f;
        
        // 第3个点
        colors[2] = 0xFFFFFFFF;
        positions[2] = 1;
        
        LinearGradient shader = new LinearGradient(
                0, top,
                0, top + contentHeight,
                colors,
                positions,
                Shader.TileMode.CLAMP);
        alphaPaint.setShader(shader);
        canvas.drawRect(new Rect(0, (int) top, width, (int) (top + contentHeight)), alphaPaint);
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
    
    public int getCurrIndex() {
        return currIndex;
    }
    
    public WheelChooseView2 setCurrIndex(int currIndex) {
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
    public WheelChooseView2 setMaxShowNum(int maxShowNum) {
        if (maxShowNum % 2 == 0) {
            throw new IllegalArgumentException("=====maxShowNum can't be even number======");
        }
        this.maxShowNum = maxShowNum;
        return this;
    }
    
    public boolean isRecycleMode() {
        return isRecycleMode;
    }
    
    public WheelChooseView2 setRecycleMode(boolean recycleMode) {
        isRecycleMode = recycleMode;
        return this;
    }
    
    public int getCenterTextColor() {
        return centerTextColor;
    }
    
    public WheelChooseView2 setCenterTextColor(@ColorInt int centerTextColor) {
        this.centerTextColor = centerTextColor;
        return this;
    }
    
    public int getOtherTextColor() {
        return otherTextColor;
    }
    
    public WheelChooseView2 setOtherTextColor(@ColorInt int otherTextColor) {
        this.otherTextColor = otherTextColor;
        return this;
    }
    
    public int getCenterTextSize() {
        return centerTextSize;
    }
    
    public WheelChooseView2 setCenterTextSize(int centerTextSize) {
        this.centerTextSize = centerTextSize;
        return this;
    }
    
    public float getCenterTextPadding() {
        return centerTextPadding;
    }
    
    public WheelChooseView2 setCenterTextPadding(float centerTextPadding) {
        this.centerTextPadding = centerTextPadding;
        return this;
    }
    
    public boolean isHasSeparateLine() {
        return hasSeparateLine;
    }
    
    public WheelChooseView2 setHasSeparateLine(boolean hasSeparateLine) {
        this.hasSeparateLine = hasSeparateLine;
        return this;
    }
}
