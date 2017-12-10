package com.example.twt.mobileplayer.pager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.activity.SystemVideoPlayer;
import com.example.twt.mobileplayer.adapter.NetVideoPagerAdapter;
import com.example.twt.mobileplayer.base.BasePager;
import com.example.twt.mobileplayer.domain.MediaItem;
import com.example.twt.mobileplayer.utils.CacheUtil;
import com.example.twt.mobileplayer.utils.Constants;
import com.example.twt.mobileplayer.view.XListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class NetVideoPager extends BasePager {
    private static final String TAG = "NetVideoPager";
    private Activity mActivity;
    private XListView mListView;
    private TextView tv_nonet;
    private ProgressBar pb_loading;
    private ArrayList<MediaItem> mMediaItemList;
    /**
     * 是否已经加载更多了
     */
    private boolean isLoadMore = false;
    private NetVideoPagerAdapter adapter;

    public NetVideoPager(Activity mActivity) {
        super(mActivity);
        this.mActivity=mActivity;
    }

    @Override
    public View initView() {
       View view=View.inflate(mActivity, R.layout.netvideo_pager,null);
        mListView = (XListView) view.findViewById(R.id.listview);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        tv_nonet = (TextView) view.findViewById(R.id.tv_nonet);
        mListView.setPullLoadEnable(true);
        mListView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                getDataFromNet();
                Log.d(TAG, "onRefresh: 下拉刷新");
            }

            @Override
            public void onLoadMore() {


                getMoreDataFromNet();
                Log.d(TAG, "onLoadMore: 加载更多");

            }
        });

           if (isInitData){
               String cache = CacheUtil.getString(mActivity, Constants.NET_URL);
               if (!TextUtils.isEmpty(cache)){
                   processDate(cache);
                   Log.d(TAG, "initView: 从本地加载");
               }
           }

        return  view;
    }

    @Override
    public void initData() {
        super.initData();
        if(!isInitData) {
            Log.d(TAG, "网络视频数据初始化 ");
            getDataFromNet();
        }


    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
               processDate(result);

                CacheUtil.setString(mActivity,Constants.NET_URL,result);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void processDate(String result) {
        if (!isLoadMore){

            mMediaItemList=parseJson(result);

            setDate();

        }else {
            isLoadMore=false;
            mMediaItemList.addAll(parseJson(result));
            //刷新适配器
            adapter.notifyDataSetChanged();
            Log.d(TAG, "processDate: "+mMediaItemList.size());

            onLoad();
        }
    }

    private void setDate() {
        if (mMediaItemList!=null&&mMediaItemList.size()>0){
            pb_loading.setVisibility(View.GONE);
            adapter = new NetVideoPagerAdapter(mMediaItemList,mActivity);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                  //  MediaItem mediaItem = mMediaItemList.get(position);


                    //2.调用自己写的播放器-显示意图--一个播放地址
                    Intent intent = new Intent(mActivity,SystemVideoPlayer.class);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("data",  mMediaItemList);
                    bundle.putInt("position",position-1);
                    intent.putExtras(bundle);
                    mActivity.startActivity(intent);
                }
            });
            onLoad();
            //把文本隐藏
            tv_nonet.setVisibility(View.GONE);
        }else {
            //没有数据
            //文本显示
            tv_nonet.setVisibility(View.VISIBLE);
        }
    }

    private void getMoreDataFromNet() {
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
               // Log.d(TAG, "onSuccess: "+result);
                isLoadMore=true;
                processDate(result);



            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
                isLoadMore=false;
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
                isLoadMore=false;
            }

            @Override
            public void onFinished() {
                isLoadMore=false;
            }
        });
    }

    private ArrayList<MediaItem> parseJson(final String result) {
        ArrayList<MediaItem> mediaItems = new ArrayList<>();


                    try {

                        JSONObject jsobject=new JSONObject(result);
                        JSONArray jsonArray = jsobject.optJSONArray("trailers");
                        if (jsonArray!=null&&jsonArray.length()>0){
                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                                if (jsobject!=null){
                                    MediaItem mediaItem=new MediaItem();


                                    String title = jsonObject.optString("videoTitle");
                                    mediaItem.setDisplay_name(title);

                                    String url = jsonObject.optString("url");
                                    mediaItem.setData(url);

                                    String imgUrl = jsonObject.optString("coverImg");
                                    mediaItem.setImgUrl(imgUrl);

                                    String summary = jsonObject.optString("summary");
                                    mediaItem.setSummary(summary);

                                    mediaItems.add(mediaItem);
                                }


                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }





        return mediaItems;

    }
    private void onLoad() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mListView.setRefreshTime("更新时间:"+getSysteTime());
    }
    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }
}
