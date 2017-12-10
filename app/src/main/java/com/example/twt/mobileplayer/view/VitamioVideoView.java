package com.example.twt.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import io.vov.vitamio.widget.VideoView;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class VitamioVideoView extends VideoView {
    public VitamioVideoView(Context context) {
        super(context);
    }

    public VitamioVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VitamioVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void setScreen(int width,int height) {
        ViewGroup.LayoutParams params=getLayoutParams();
        getLayoutParams().height=height;
        getLayoutParams().width=width;
        setLayoutParams(params);
    }
}
