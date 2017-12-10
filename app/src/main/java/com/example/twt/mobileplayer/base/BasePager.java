package com.example.twt.mobileplayer.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public abstract class BasePager extends Fragment {
    /**
     * 上下文
     */
    public Context mContext;
    /**
     * 接收各个页面的实例
     */

    public boolean isInitData;
    public BasePager(Context context ){
       this.mContext=context;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         mContext=getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return initView();
    }


    /**
     * 强制子页面实现该方法，实现想要的特定的效果
     *
     * @return
     */
    public abstract View initView();


    /**
     * 当子页面，需要绑定数据，或者联网请求数据并且绑定的时候，重写该方法
     */
    public void initData(){

    }
}
