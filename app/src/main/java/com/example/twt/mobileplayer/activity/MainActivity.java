package com.example.twt.mobileplayer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.base.BasePager;
import com.example.twt.mobileplayer.pager.AudioPager;
import com.example.twt.mobileplayer.pager.NetAudioPager;
import com.example.twt.mobileplayer.pager.NetVideoPager;
import com.example.twt.mobileplayer.pager.VideoPager;

import java.util.ArrayList;
import java.util.List;

import static com.example.twt.mobileplayer.R.id.rg_bottom_tag;


public class MainActivity extends FragmentActivity {

    private RadioGroup mRadioGroup;
    private List<BasePager> mBasePagerList;
    private int position=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRadioGroup = (RadioGroup) findViewById(rg_bottom_tag);
       mRadioGroup.check(R.id.rb_video);
        initData();
        initFragment();

    }
    /**
     * 根据位置得到对应的页面
     * @return
     */
    private BasePager getBasePager() {
        BasePager basePager = mBasePagerList.get(position);
        if(basePager != null&&!basePager.isInitData){
            basePager.initData();//联网请求或者绑定数据
            basePager.isInitData = true;
        }
        return basePager;
    }
   private void initFragment() {
        //1.得到FragmentManger
        FragmentManager manager = getSupportFragmentManager();
        //2.开启事务
        FragmentTransaction ft = manager.beginTransaction();
        //3.替换
        ft.replace(R.id.fl_main_content,getBasePager());
        //4.提交事务
        ft.commit();
    }



    private void initData() {
        mBasePagerList=new ArrayList<>();
        mBasePagerList.add(new VideoPager(this));
        mBasePagerList.add(new AudioPager(this));
        mBasePagerList.add(new NetVideoPager(this));
        mBasePagerList.add(new NetAudioPager(this));


        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                 switch (checkedId){
                     case R.id.rb_video:
                         position=0;
                         break;
                     case R.id.rb_audio:
                         position=1;
                         break;
                     case R.id.rb_netvideo:
                         position=2;
                         break;
                     case R.id.rb_netaudio:
                         position=3;
                         break;

                 }
                initFragment();
            }

        });

    }
    /**
     * 是否已经退出
     */
    private boolean isExit = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode ==KeyEvent.KEYCODE_BACK){
            if(position != 0){//不是第一页面
                position = 0;
                mRadioGroup.check(R.id.rb_video);//首页
                return true;
            }else  if(!isExit){
                isExit = true;
                Toast.makeText(MainActivity.this,"再按一次退出",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit  = false;
                    }
                },2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
