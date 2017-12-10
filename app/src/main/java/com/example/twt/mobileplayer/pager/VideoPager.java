package com.example.twt.mobileplayer.pager;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.activity.SystemVideoPlayer;
import com.example.twt.mobileplayer.adapter.VideoPagerAdapter;
import com.example.twt.mobileplayer.base.BasePager;
import com.example.twt.mobileplayer.domain.MediaItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class VideoPager extends BasePager {
    private static final String TAG = "VideoPager";
    private Activity mActivity;
    private List<MediaItem> mMediaItemList;
    private VideoPagerAdapter adapter;
    private int pos;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mMediaItemList!=null&&mMediaItemList.size()>0){
                    mPb_loading.setVisibility(View.GONE);
                    mListView.setAdapter(adapter);
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            MediaItem mediaItem = mMediaItemList.get(position);

                            //1.调起系统所有的播放-隐式意图
                        /*Intent intent = new Intent();
                        intent.setDataAndType(Uri.parse("http://192.168.1.102:8080/vivo1.mp4"),"video*//**//*");
                        mActivity.startActivity(intent);*/

                            //2.调用自己写的播放器-显示意图--一个播放地址
                            Intent intent = new Intent(mActivity,SystemVideoPlayer.class);
                            Bundle bundle=new Bundle();
                            bundle.putSerializable("data", (Serializable) mMediaItemList);
                            bundle.putInt("position",position);
                            intent.putExtras(bundle);
                            mActivity.startActivity(intent);
                        }
                    });
                }else {
                    mPb_loading.setVisibility(View.GONE);
                    mTv_nomedia.setVisibility(View.VISIBLE);
                    mTv_nomedia.setText("没有搜索到本地视频");
                }
                    break;
            
                case 1:

                    //列表删除
                    if (mMediaItemList.remove(pos) != null) {//这行代码必须有
                        System.out.println("success");
                    } else {
                        System.out.println("failed");
                    }
                    //本地删除
                  //  mContentResolver.delete(mUri,MediaStore.Images.Media.DATA +"="+mCursor.getString(pos) , null);

                    Toast.makeText(mActivity, "删除此项", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "handleMessage: "+mMediaItemList.size());
                    break;
            }

        }
    };
    private ListView mListView;
    private TextView mTv_nomedia;
    private ProgressBar mPb_loading;
    private ContentResolver mContentResolver;
    private Uri mUri;
    private Cursor mCursor;

    public VideoPager(Activity mActivity) {
        super(mActivity);
        this.mActivity=mActivity;
    }


    @Override
    public View initView() {
        View view=View.inflate(mActivity, R.layout.video_pager,null);
        mListView = (ListView) view.findViewById(R.id.listview);
        mTv_nomedia = (TextView) view.findViewById(R.id.tv_nomedia);
        mPb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
         adapter=new VideoPagerAdapter(mMediaItemList,mActivity,false);
        //为 ListView 的所有 item 注册 ContextMenu
        this.registerForContextMenu(mListView);
        if (isInitData){
            mHandler.sendEmptyMessage(0);
        }

        return  view;
    }

    @Override
    public void initData() {
        super.initData();

        if(!isInitData) {
            Log.d(TAG, "本地视频数据初始化 ");
            getDataFromLocal();
        }



    }

    private void getDataFromLocal() {
        new Thread(){
            @Override
            public void run() {
                isGrantExternalRW((Activity) mActivity);
                mMediaItemList=new ArrayList<MediaItem>();
                mUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                mContentResolver = mActivity.getContentResolver();
                String[] objs = {
                        MediaStore.Video.Media.DISPLAY_NAME,//视频文件在sdcard的名称
                        MediaStore.Video.Media.DURATION,//视频总时长
                        MediaStore.Video.Media.SIZE,//视频的文件大小
                        MediaStore.Video.Media.DATA,//视频的绝对地址
                        MediaStore.Video.Media.ARTIST,//歌曲的演唱者

                };
                mCursor = mContentResolver.query(mUri,objs,null,null,null);
                if (mCursor !=null){
                    while (mCursor.moveToNext()){
                        MediaItem mediaItem=new MediaItem();
                        mMediaItemList.add(mediaItem);
                        mediaItem.setDisplay_name(mCursor.getString(0));
                        mediaItem.setDuration(mCursor.getLong(1));
                        mediaItem.setSize(mCursor.getLong(2));
                        mediaItem.setData(mCursor.getString(3));
                        mediaItem.setArtist(mCursor.getString(4));
                        Log.d(TAG, "run: "+ mCursor.getString(3));
                    }

                    mCursor.close();
                }

                mHandler.sendEmptyMessage(0);
            }
        }.start();

    }
    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }
    //重写onCreateContextMenu方法
    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        menu.setHeaderTitle("选择操作");
        menu.add(0, 1, Menu.NONE, "发送");
        menu.add(0, 2, Menu.NONE, "标记为重要");
        menu.add(0, 3, Menu.NONE, "重命名");
        menu.add(0, 4, Menu.NONE, "删除");
    }
    //重写onContextItemSelected方法（这里我只实现了删除列表项的功能，其他功能如有需要，请自行添加）
    @Override
    public boolean onContextItemSelected (MenuItem item){
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 1:
                // 发送...
                break;
            case 2:
                //标记...
                break;
            case 3:
                //重命名...
                break;
            case 4:
                //删除列表项...
                 pos = (int) mListView.getAdapter().getItemId(menuInfo.position);

               mHandler.sendEmptyMessage(1);

                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }
}
