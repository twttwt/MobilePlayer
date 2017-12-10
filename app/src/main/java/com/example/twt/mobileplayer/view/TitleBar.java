package com.example.twt.mobileplayer.view;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.activity.SearchActivity;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class TitleBar extends LinearLayout implements View.OnClickListener {

    private View tv_search;

    private View rl_game;

    private View iv_record;
    private Context mContext;
    public TitleBar(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;

    }



    public TitleBar(Context context) {
       this(context,null,0);
    }

    /**
     * 当布局文件加载完成的时候回调这个方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //得到孩子的实例
        tv_search = getChildAt(1);
        rl_game = getChildAt(2);
        iv_record = getChildAt(3);

        //设置点击事件
        tv_search.setOnClickListener(this);
        rl_game.setOnClickListener(this);
        iv_record.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.tv_search:
                //Toast.makeText(mContext,"搜索", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext,SearchActivity.class);
                mContext.startActivity(intent);
               break;
            case R.id.tv_game:
                Toast.makeText(mContext,"游戏", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_record:
                Toast.makeText(mContext,"历史", Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
