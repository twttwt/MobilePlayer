package com.example.twt.mobileplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.example.twt.mobileplayer.domain.LrcContent;
import com.example.twt.mobileplayer.utils.DensityUtil;

import java.util.List;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class LrcView extends TextView {
    private static final String TAG = "LrcView";
    private Paint currentPaint;  //高亮画笔
    private Paint normalPaint;   //一般画笔
    private List<LrcContent> mlrclist;
    private int index; //当前数组索引
    private float textHeight;  //文本高度
    private int width;  //歌词所在视图的高
    private int height;
    private Context context;
    /**
     * 高亮显示的时间或者休眠时间
     */
    private float sleepTime;
    /**
     * 当前播放进度
     */
    private float currentPositon;
    /**
     * 时间戳，什么时刻到高亮哪句歌词
     */
    private float timePoint;
    public void setMlrclist(List<LrcContent> mlrclist) {
        this.mlrclist = mlrclist;
    }

    public LrcView(Context context) {
       this(context,null);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
       super (context, attrs, defStyleAttr);
        this.context=context;
        init();

    }


    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    private void init() {
        setFocusable(true);
        textHeight = DensityUtil.dip2px(context, 30);

        currentPaint=new Paint();
        currentPaint.setAntiAlias(true);    //设置抗锯齿，让文字美观饱满
        currentPaint.setTextAlign(Paint.Align.CENTER);//设置文本对齐方式
        currentPaint.setColor(Color.GREEN);
        currentPaint.setTextSize(DensityUtil.dip2px(context, 20));



        normalPaint=new Paint();
        normalPaint.setAntiAlias(true);    //设置抗锯齿，让文字美观饱满
        normalPaint.setTextAlign(Paint.Align.CENTER);//设置文本对齐方式
        normalPaint.setColor(Color.WHITE);
        normalPaint.setTextSize(DensityUtil.dip2px(context, 20));


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }
        if (mlrclist != null && mlrclist.size() > 0) {

            //往上推移

            float plush = 0;
            if (sleepTime == 0) {
                plush = 0;
            } else {
                //平移
                //这一句所花的时间 ：休眠时间 = 移动的距离 ： 总距离（行高）
                //移动的距离 =  (这一句所花的时间 ：休眠时间)* 总距离（行高）
                //                float delta = ((currentPositon-timePoint)/sleepTime )*textHeight;

                //屏幕的的坐标 = 行高 + 移动的距离
                plush = textHeight + ((currentPositon - timePoint) / sleepTime) * textHeight;
            }
            canvas.translate(0, -plush);
           //高亮部分

                setText("");
                canvas.drawText(mlrclist.get(index).getLrcStr(), width / 2, height / 2, currentPaint);

                //上半部分歌词
                float temp = height / 2;
                for (int i = index - 1; i >= 0; i--) {
                    temp = temp - textHeight;
                    if (temp < 0) {
                        break;
                    }
                    canvas.drawText(mlrclist.get(i).getLrcStr(), width / 2, temp, normalPaint);
                }

                //下半部分歌词
                temp = height / 2;
                for (int i = index + 1; i < mlrclist.size(); i++) {
                    temp = temp + textHeight;
                    if (temp > height) {
                        break;
                    }
                    canvas.drawText(mlrclist.get(i).getLrcStr(), width / 2, temp, normalPaint);
                }

            }  else {
                //没有歌词
                canvas.drawText("没有歌词", width / 2, height / 2, currentPaint);
            }
        }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.width=w;
        this.height=h;
        super.onSizeChanged(w, h, oldw, oldh);
    }
    public void setIndex(int index) {
        this.index = index;
    }
    /**
     * 根据当前播放的位置，找出该高亮显示哪句歌词
     *
     * @param currentPosition
     */
    public void setshowNextLyric(int currentPosition,int Duration) {
        this.currentPositon = currentPosition;
        if (mlrclist == null || mlrclist.size() == 0){
            return;
        }


        if(currentPosition < Duration) {
            for (int i = 0; i < mlrclist.size(); i++) {
                if (i < mlrclist.size() - 1) {
                    if (currentPosition < mlrclist.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentPosition > mlrclist.get(i).getLrcTime()
                            && currentPosition < mlrclist.get(i + 1).getLrcTime()) {
                        index = i;
                    }
               }
                if (i == mlrclist.size() - 1
                        && currentPosition > mlrclist.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }

        //重新绘制
        invalidate();//在主线程中
        //子线程
        //        postInvalidate();
    }
}
