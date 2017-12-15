package com.example.twt.mobileplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.domain.MediaItem;
import com.example.twt.mobileplayer.utils.Utils;
import com.example.twt.mobileplayer.view.Videoview;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.twt.mobileplayer.R.id.cancel_action;
import static com.example.twt.mobileplayer.R.id.videoview;

public class SystemVideoPlayer extends Activity implements View.OnClickListener {

    private static final String TAG = "SystemVideoPlayer";
    /**
     * 全屏
     */
    private static final int FULL_SCREEN = 1;
    /**
     * 默认屏幕
     */
    private static final int DEFAULT_SCREEN = 2;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:  //更新进度条
                    //1.得到当前的视频播放进程
                    int currentPosition = mVideoView.getCurrentPosition();
                    Log.d(TAG, "handleMessage: "+currentPosition);
                    //2.SeekBar.setProgress(当前进度);
                    seekbarVideo.setProgress(currentPosition);

                    tvCurrentTime.setText(mUtils.stringForTime(currentPosition));

                    //监听卡
                    if (!isUseSystem && mVideoView.isPlaying()) {

                        if(mVideoView.isPlaying()){
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 500) {
                                //视频卡了
                                ll_buffer.setVisibility(View.VISIBLE);
                                sendEmptyMessage(3);
                            } else {
                                //视频不卡了
                                ll_buffer.setVisibility(View.GONE);
                                removeMessages(3);
                            }
                        }else{
                            ll_buffer.setVisibility(View.GONE);
                        }

                    }
                    /**
                     * 网络视频设置缓存
                     */
                    if (isNetUri){
                        int buffer=mVideoView.getBufferPercentage();
                        int totalBuffer=buffer*seekbarVideo.getMax();
                        int secondaryProgress=totalBuffer/100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    }else {
                        seekbarVideo.setSecondaryProgress(0);
                    }
                    precurrentPosition = currentPosition;
                    mHandler.removeMessages(0);
                    mHandler.sendEmptyMessageDelayed(0,1000);
                    break;

                case 1:   //更新时间

                    tvSystemTime.setText(currentTime(System.currentTimeMillis()));
                    mHandler.sendEmptyMessageDelayed(1,1000);
                    break;
                case 2:   //隐藏控制面板
                    hideMediaController();
                    isShow=false;
                    break;
                case 3:   //显示网速
                    //1.得到网络速度
                    String netSpeed = mUtils.getNetSpeed(SystemVideoPlayer.this);
                    Log.d(TAG, "handleMessage: "+netSpeed);
                    //显示网络速
                    tv_laoding_netspeed.setText("玩命加载中..."+netSpeed);
                    tv_buffer_netspeed.setText("缓存中..."+netSpeed);

                    //2.每两秒更新一次
                    mHandler.removeMessages(3);
                    mHandler.sendEmptyMessageDelayed(3, 2000);
                    break;
                case 4://更新标题
                    tvName.setText(display_name);
                    break;
            }

        }
    };
    /**
     * 上一次的播放进度
     */
    private int precurrentPosition;
    private boolean isUseSystem = false; //mp4 false   m3u8 true
    private List<MediaItem> mMediaItemList;
    private int mPosition;
    private MyReceiver mReceiver;
    private GestureDetector mGestureDetector;
    private RelativeLayout mediaController;
    private int videoWidth;   //视频原宽度
    private int videoHeight;  //视频原高度
    private Videoview mVideoView;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private Utils mUtils;
    private  boolean isShow;  //是否显示控制面板
    private  boolean isFullScreen=false; //是否全屏
    private int screenWidth=0;  //屏幕宽度
    private int screenHeight=0;//屏幕高度
    private AudioManager as;
    private float StartY;
    private float endY;
    private int maxVoice;//最大音量
    private int mCurrentVoice;  //当前音量
    private int mVol; //当前按下的音量
    private boolean isMute; //是否静音
    /**
     * 屏幕的高
     */
    private float touchRang;
    private Uri uri;
    private boolean isNetUri;
    private MediaItem mediaItem;
    private LinearLayout ll_buffer;
    private LinearLayout ll_loading;
    private TextView tv_laoding_netspeed;
    private TextView tv_buffer_netspeed;
    private String display_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_video_player);



        findViews();
        setListener();
        getData();
        setData();
        //出错

       //mVideoView.setMediaController(new MediaController(this));
    }

    private void setListener() {
        seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    seekBar.setProgress(progress);
                    tvCurrentTime.setText(mUtils.stringForTime(progress));
                    mVideoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(2);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.sendEmptyMessageDelayed(2,4000);
            }
        });
        seekbarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    if (isMute){
                        updateVoice(progress,true);
                    }else {
                        updateVoice(progress,false);
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(2);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.sendEmptyMessageDelayed(2,4000);
            }
        });

        if (isUseSystem){
            //监听视频播放卡-系统的api
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mVideoView.setOnInfoListener(new MyOnInfoListener());
            }
        }
        /**
         * 播放出错处理
         */
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //1当前播放器不支持该格式
                //2网络连接中断
                //3该视频不完全
                StartToMediaPlayer();

                return true;
            }
        });
        /**
         * 准备播放
         */
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();
                mVideoView.start();
                setVideoType(DEFAULT_SCREEN);
                seekbarVoice.setMax(maxVoice);
                if (mCurrentVoice==0){
                    updateVoice(mCurrentVoice,true);
                }else {
                    updateVoice(mCurrentVoice,false);
                }

                tvDuration.setText(mUtils.stringForTime(mp.getDuration()));
                seekbarVideo.setMax(mp.getDuration());
                setButtonState();
                //把加载页面消失掉
                ll_loading.setVisibility(View.GONE);
                mHandler.sendEmptyMessage(0);//更新进度
                mHandler.sendEmptyMessage(1);//更新时间
                mHandler.sendEmptyMessageDelayed(2,4000);//隐藏控制面板
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(getApplicationContext(),"播放完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setData() {
        if(mMediaItemList != null &&mMediaItemList .size() >0){
            mediaItem = mMediaItemList.get(mPosition);
            tvName.setText(mediaItem.getDisplay_name());//设置视频的名称
            isNetUri = mUtils.isNetUri(mediaItem.getData());
            mVideoView.setVideoPath(mediaItem.getData());

        }else if(uri !=null){
            tvName.setText(uri+"");//设置视频的名称
            isNetUri = mUtils.isNetUri(uri+"");
            mVideoView.setVideoURI(uri);
        }else{
            Toast.makeText(SystemVideoPlayer.this, "帅哥你没有传递数据", Toast.LENGTH_SHORT).show();
        }


        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED); //监听手机电量变化
        mReceiver = new MyReceiver();
        registerReceiver(mReceiver,filter);





    }

    private void StartToMediaPlayer() {
        if (mVideoView!=null)
            mVideoView.stopPlayback();
        Intent intent=new Intent(SystemVideoPlayer.this,VitamioVideoPlayer.class);
        if(mMediaItemList != null && mMediaItemList.size() > 0){

            Bundle bundle = new Bundle();
            bundle.putSerializable("data", (Serializable) mMediaItemList);
            intent.putExtras(bundle);
            intent.putExtra("position", mPosition);

        }else if(uri != null){
            intent.setData(uri);
        }
        startActivity(intent);
        finish();
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener{

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://视频卡了，拖动卡
                    //                    Toast.makeText(SystemVideoPlayer.this, "卡了", Toast.LENGTH_SHORT).show();
                    ll_buffer.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessage(3); //显示网速
                    break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END://视频卡结束了，拖动卡结束了
                    //                    Toast.makeText(SystemVideoPlayer.this, "卡结束了", Toast.LENGTH_SHORT).show();
                    ll_buffer.setVisibility(View.GONE);
                    break;
            }
            return true;
        }



    }
    private void setButtonState() {
        if (mMediaItemList != null && mMediaItemList.size() > 0) {
            if (mMediaItemList.size() == 1) {
                setEnable(false);
            } else if (mMediaItemList.size() == 2) {
                if (mPosition == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);

                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);

                } else if (mPosition == mMediaItemList.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);

                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);

                }
            } else {
                if (mPosition == 0) {
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                } else if (mPosition == mMediaItemList.size() - 1) {
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                } else {
                    setEnable(true);
                }
            }
        } else if (uri != null) {
            //两个按钮设置灰色
            setEnable(false);
        }
    }
    private void setEnable(boolean isEnable) {
        if (isEnable) {
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            btnVideoPre.setEnabled(true);
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
            btnVideoNext.setEnabled(true);
        } else {
            //两个按钮设置灰色
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }

    }

    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getExtras().getInt("level"); //获取当前电量
            BatteryChanged(level);
        }
    }

    private void BatteryChanged(int level) {
        switch (level){
            case 0:
                ivBattery.setImageResource(R.drawable.ic_battery_0);
                break;
            case 10:
               ivBattery.setImageResource(R.drawable.ic_battery_10);
                break;
            case 20:
                ivBattery.setImageResource(R.drawable.ic_battery_20);
                break;
            case 40:
                ivBattery.setImageResource(R.drawable.ic_battery_40);
                break;
            case 60:
                ivBattery.setImageResource(R.drawable.ic_battery_60);
                break;
            case 80:
                ivBattery.setImageResource(R.drawable.ic_battery_80);
                break;
            case 100:
                ivBattery.setImageResource(R.drawable.ic_battery_100);
                break;
        }
    }

    private void getData() {
       /* mMediaItem = mMediaItemList.get(mPosition);
        String uri= mMediaItem.getData();
        Log.d(TAG, "onCreate:11111111111111111111 "+mMediaItem.getDuration());
        if (uri!=null)
        mVideoView.setVideoPath(uri);*/

        //得到播放地址
        uri = getIntent().getData();
        mMediaItemList = (ArrayList<MediaItem>) getIntent().getSerializableExtra("data");
        mPosition = getIntent().getIntExtra("position", 0);

    }


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-07-06 13:26:17 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        tv_laoding_netspeed=(TextView)findViewById(R.id.tv_laoding_netspeed);
        tv_buffer_netspeed = (TextView) findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = (LinearLayout) findViewById(R.id.ll_buffer);
        ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
        mVideoView = (Videoview) findViewById(videoview);
        llTop = (LinearLayout)findViewById( R.id.ll_top );
        tvName = (TextView)findViewById( R.id.tv_name );
        ivBattery = (ImageView)findViewById( R.id.iv_battery );
        tvSystemTime = (TextView)findViewById( R.id.tv_system_time );
        btnVoice = (Button)findViewById( R.id.btn_voice );
        seekbarVoice = (SeekBar)findViewById( R.id.seekbar_voice );
        btnSwichPlayer = (Button)findViewById( R.id.btn_swich_player );
        llBottom = (LinearLayout)findViewById( R.id.ll_bottom );
        tvCurrentTime = (TextView)findViewById( R.id.tv_current_time );
        seekbarVideo = (SeekBar)findViewById( R.id.seekbar_video );
        tvDuration = (TextView)findViewById( R.id.tv_duration );
        btnExit = (Button)findViewById( R.id.btn_exit );
        btnVideoPre = (Button)findViewById( R.id.btn_video_pre );
        btnVideoStartPause = (Button)findViewById( R.id.btn_video_start_pause );
        btnVideoNext = (Button)findViewById( R.id.btn_video_next );
        btnVideoSiwchScreen = (Button)findViewById( R.id.btn_video_siwch_screen );
        mediaController=(RelativeLayout) findViewById(R.id.media_controller);
        btnVoice.setOnClickListener( this );
        btnSwichPlayer.setOnClickListener( this );
        btnExit.setOnClickListener( this );
        btnVideoPre.setOnClickListener( this );
        btnVideoStartPause.setOnClickListener( this );
        btnVideoNext.setOnClickListener( this );
        btnVideoSiwchScreen.setOnClickListener( this );

        initData();
    }

    private void initData() {
        mUtils=new Utils();
        //得到屏幕的宽和高最新方式
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        as= (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVoice=as.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurrentVoice = as.getStreamVolume(AudioManager.STREAM_MUSIC);

        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        mGestureDetector=new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                if (mVideoView.isPlaying()){
                    mVideoView.pause();
                    btnVideoStartPause.setBackgroundResource(R.drawable.btn_play_normal);
                    mediaController.setVisibility(View.GONE);

                }else {
                    mVideoView.start();
                    btnVideoStartPause.setBackgroundResource(R.drawable.btn_pause_normal);
                    mediaController.setVisibility(View.GONE);

                }
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isFullScreen){
                    setVideoType(FULL_SCREEN);

                }else {
                   setVideoType(DEFAULT_SCREEN);

                }

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShow){
                    hideMediaController();

                    mHandler.removeMessages(2);
                }else {
                    showMediaController();
                    mHandler.sendEmptyMessageDelayed(2,4000);


                }
                Log.d(TAG, "onSingleTapConfirmed: "+isShow);
                return super.onSingleTapConfirmed(e);
            }
        });



    }

    /**
     * 显示控制面板
     */
    private void showMediaController(){
        mediaController.setVisibility(View.VISIBLE);
        isShow = true;
    }


    /**
     * 隐藏控制面板
     */
    private void hideMediaController(){
        mediaController.setVisibility(View.GONE);
        isShow = false;
    }

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen){
            case FULL_SCREEN:
                mVideoView.setScreen(screenWidth,screenHeight);
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
                isFullScreen=true;
                break;
            case DEFAULT_SCREEN:
                //1.设置视频画面的大小
                //视频真实的宽和高

                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                //屏幕的宽和高
                int width = screenWidth;
                int height = screenHeight;

                // for compatibility, we adjust size based on aspect ratio
                if ( mVideoWidth * height  < width * mVideoHeight ) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }

                mVideoView.setScreen(width,height);
                btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
                isFullScreen=false;
                break;
        }
    }

    private String currentTime(long time) {
        SimpleDateFormat sd=new SimpleDateFormat("HH:mm:ss");
        return sd.format(new Date(time));

    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-07-06 13:26:17 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if ( v == btnVoice ) {
            // Handle clicks for btnVoice
            updateVoice(0,true);
            mHandler.removeMessages(2);
        } else if ( v == btnSwichPlayer ) {
            ShowConfigDialog();
        } else if ( v == btnExit ) {
            // Handle clicks for btnExit
        } else if ( v == btnVideoPre ) {
            // Handle clicks for btnVideoPre
            StartToPre();
            mHandler.removeMessages(2);
        } else if ( v == btnVideoStartPause ) {
            // Handle clicks for btnVideoStartPause
            if (mVideoView.isPlaying()){
                mVideoView.pause();
                btnVideoStartPause.setBackgroundResource(R.drawable.btn_play_normal);
                mHandler.removeMessages(2);
            }else {
                mVideoView.start();
                btnVideoStartPause.setBackgroundResource(R.drawable.btn_pause_normal);
                mHandler.removeMessages(2);
            }
        } else if ( v == btnVideoNext ) {
            // Handle clicks for btnVideoNext
             StarttoNext();
            mHandler.removeMessages(2);
        } else if ( v == btnVideoSiwchScreen ) {
            // Handle clicks for btnVideoSiwchScreen
             if (isFullScreen){
                 setVideoType(DEFAULT_SCREEN);
             }else {
                 setVideoType(FULL_SCREEN);
             }
            mHandler.removeMessages(2);
        }
    }

    private void ShowConfigDialog() {
        final AlertDialog.Builder builder=new AlertDialog.Builder(SystemVideoPlayer.this);
        builder.setTitle("提示");
        builder.setMessage("你可以切换到万能播放器进行观看");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StartToMediaPlayer();
            }
        }) ;
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 播放上一个
     */
    private void StartToPre() {
        --mPosition;
        btnVideoNext.setBackgroundResource(R.drawable.btn_next_normal);
        MediaItem mediaItem = mMediaItemList.get(mPosition);
        isNetUri = mUtils.isNetUri(mediaItem.getData());
        mVideoView.setVideoPath(mediaItem.getData());
        tvName.setText(mediaItem.getDisplay_name());
        mHandler.sendEmptyMessage(4);
    }

    /**
     * 播放下一个
     */
    private void StarttoNext() {
        ++mPosition;
        btnVideoPre.setBackgroundResource(R.drawable.btn_pre_normal);
        MediaItem mediaItem = mMediaItemList.get(mPosition);
        isNetUri = mUtils.isNetUri(mediaItem.getData());
        mVideoView.setVideoPath(mediaItem.getData());
        display_name = mediaItem.getDisplay_name();
        tvName.setText(mediaItem.getDisplay_name());
        mHandler.sendEmptyMessage(4);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        if (mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
        //先释放子类资源
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                StartY = event.getY();
                mVol=as.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenWidth);//screenHeight
                mHandler.removeMessages(2);

                break;
            case MotionEvent.ACTION_UP:
                mHandler.sendEmptyMessageDelayed(2,4000);
                break;
            case MotionEvent.ACTION_MOVE:
                endY = event.getY();
                float distance = StartY - endY;


                //改变声音
                float delta = (distance / touchRang) * maxVoice;
                //最终声音 = 原来的 + 改变声音；
                int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                Log.d(TAG, "onTouchEvent: "+voice);
                if (delta != 0) {
                    isMute = false;
                    updateVoice(voice, isMute);


                    break;
                }
        }
                return super.onTouchEvent(event);

    }

    private void updateVoice(int progress,boolean isMute) {
        if (isMute){
            as.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            seekbarVoice.setProgress(0);
            mCurrentVoice=0;
        }else {
            as.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            seekbarVoice.setProgress(progress);
            mCurrentVoice = progress;
        }
    }

    /**
     * 监听物理健，实现声音的调节大小
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
               if (mCurrentVoice > 0) {
                   mCurrentVoice--;
                   updateVoice(mCurrentVoice, false);
                   Log.d(TAG, "onKeyDown: " + mCurrentVoice);
               }else {
                   updateVoice(mCurrentVoice,true);
               }
                // handler.removeMessages(HIDE_MEDIACONTROLLER);
                // handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (mCurrentVoice < maxVoice)
                    mCurrentVoice++;
                updateVoice(mCurrentVoice, false);
                Log.d(TAG, "onKeyDown: "+mCurrentVoice);
                //  handler.removeMessages(HIDE_MEDIACONTROLLER);
                //handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
                return true;
            }

            return super.onKeyDown(keyCode, event);

    }
}
