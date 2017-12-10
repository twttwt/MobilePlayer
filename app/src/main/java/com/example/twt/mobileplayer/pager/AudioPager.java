package com.example.twt.mobileplayer.pager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.activity.AudioPlayerActivity;
import com.example.twt.mobileplayer.adapter.VideoPagerAdapter;
import com.example.twt.mobileplayer.base.BasePager;
import com.example.twt.mobileplayer.domain.MediaItem;

import java.util.ArrayList;
import java.util.List;

import static com.example.twt.mobileplayer.pager.VideoPager.isGrantExternalRW;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class AudioPager extends BasePager {
    private static final String TAG = "AudioPager";
    private Activity mActivity;
    private ListView mListView;
    private TextView mTv_nomedia;
    private ProgressBar mPb_loading;
    private List<MediaItem> mMediaItemList;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mMediaItemList!=null&&mMediaItemList.size()>0){
                mListView.setAdapter(new VideoPagerAdapter(mMediaItemList,mActivity,true));
                mListView.setOnItemClickListener(new MyOnItemClickListener());
                mPb_loading.setVisibility(View.GONE);
            }else {
                mPb_loading.setVisibility(View.GONE);
                mTv_nomedia.setText("没有搜索到本地音乐");
            }

        }
    };
    public AudioPager(Activity mActivity) {
        super(mActivity);
        this.mActivity=mActivity;
    }


    @Override
    public View initView() {
       View view=View.inflate(mActivity, R.layout.video_pager,null);
        mListView = (ListView) view.findViewById(R.id.listview);
        mTv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        mPb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        if (isInitData){
            mHandler.sendEmptyMessage(0);
        }
        return  view;
    }

    @Override
    public void initData() {
        super.initData();

        getDataFromLocal();
    }

    private void getDataFromLocal() {
        new Thread(){
            @Override
            public void run() {
                isGrantExternalRW((Activity) mActivity);
                mMediaItemList=new ArrayList<MediaItem>();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = mActivity.getContentResolver();
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Audio.Media.DURATION,//视频总时长
                        MediaStore.Audio.Media.SIZE,//视频的文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST,//歌曲的演唱者

                };
                Cursor cursor=contentResolver.query(uri,objs,null,null,null);
                if (cursor!=null){
                    while (cursor.moveToNext()){
                        MediaItem mediaItem=new MediaItem();
                        mMediaItemList.add(mediaItem);
                        mediaItem.setDisplay_name(cursor.getString(0));
                        mediaItem.setDuration(cursor.getLong(1));
                        mediaItem.setSize(cursor.getLong(2));
                        mediaItem.setData(cursor.getString(3));
                        mediaItem.setArtist(cursor.getString(4));
                    }
                    cursor.close();
                }
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }
    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent=new Intent(mActivity, AudioPlayerActivity.class);
            intent.putExtra("position",position);
            startActivity(intent);
        }
    }
}
