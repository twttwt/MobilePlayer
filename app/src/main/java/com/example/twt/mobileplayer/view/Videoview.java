package com.example.twt.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.VideoView;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class Videoview extends VideoView {
    public Videoview(Context context) {
        this(context,null);
    }

    public Videoview(Context context, AttributeSet attrs) {
        super(context, attrs);

    }



    public Videoview(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    public void setScreen(int width,int height) {
        ViewGroup.LayoutParams params=getLayoutParams();
        getLayoutParams().height=height;
        getLayoutParams().width=width;
        setLayoutParams(params);
    }

}
