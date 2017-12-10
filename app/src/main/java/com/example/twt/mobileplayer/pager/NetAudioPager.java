package com.example.twt.mobileplayer.pager;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.adapter.NetAudioAdapter;
import com.example.twt.mobileplayer.base.BasePager;
import com.example.twt.mobileplayer.domain.NetAudioPagerData;
import com.example.twt.mobileplayer.utils.CacheUtil;
import com.example.twt.mobileplayer.utils.Constants;
import com.example.twt.mobileplayer.utils.LogUtil;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;




/**
 * @author twt
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class NetAudioPager extends BasePager {

    private Activity mActivity;
    private ListView mListView;

    private TextView tv_nonet;

    private ProgressBar pb_loading;
    /**
     * 页面的数据
     */
    private List<NetAudioPagerData.ListEntity> datas;

    private NetAudioAdapter adapter;


    public NetAudioPager(Activity mActivity) {
        super(mActivity);
        this.mActivity=mActivity;
    }

    /**
     * 初始化当前页面的控件，由父类调用
     * @return
     */
    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.netaudio_pager,null);
        tv_nonet= (TextView) view.findViewById(R.id.tv_nonet);
        pb_loading= (ProgressBar) view.findViewById(R.id.pb_loading);
        mListView= (ListView) view.findViewById(R.id.listview);

        return view;
    }


    @Override
    public void initData() {

         initView();

        LogUtil.e("网络音频的数据被初始化了。。。");
        String savaJson = CacheUtil.getString(mContext, Constants.ALL_RES_URL);

        if(!TextUtils.isEmpty(savaJson)){
            //解析数据
            processData(savaJson);
        }
        //联网
        if(!isInitData) {
            getDataFromNet();
          }

    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtil.e("请求数据成功==" + result);
                //保持数据
                CacheUtil.setString(mContext, Constants.ALL_RES_URL, result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtil.e("请求数据失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

                LogUtil.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtil.e("onFinished==");
            }
        });
    }

    /**
     * 解析json数据和显示数据
     * 解析数据：1.GsonFormat生成bean对象；2.用gson解析数据
     * @param json
     */
    private void processData(String json) {
        //解析数据
        NetAudioPagerData data = parsedJson(json);
        datas =  data.getList();

        if(datas != null && datas.size() >0 ){
            //有数据
            tv_nonet.setVisibility(View.GONE);
            //设置适配器
            adapter = new NetAudioAdapter(mContext,datas);
            mListView.setAdapter(adapter);
        }else{
            tv_nonet.setText("没有对应的数据...");
            //没有数据
            tv_nonet.setVisibility(View.VISIBLE);
        }

        pb_loading.setVisibility(View.GONE);

    }

    /**
     * Gson解析数据
     * @param json
     * @return
     */
    private NetAudioPagerData parsedJson(String json) {
        return new Gson().fromJson(json,NetAudioPagerData.class);
    }

}
