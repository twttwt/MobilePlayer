package com.example.twt.mobileplayer.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.twt.mobileplayer.IMusicPlayerService;
import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.domain.MediaItem;
import com.example.twt.mobileplayer.service.MusicPlayerService;
import com.example.twt.mobileplayer.utils.CacheUtil;
import com.example.twt.mobileplayer.utils.DisplayUtil;
import com.example.twt.mobileplayer.utils.FastBlurUtil;
import com.example.twt.mobileplayer.utils.LrcUtil;
import com.example.twt.mobileplayer.utils.Utils;
import com.example.twt.mobileplayer.view.BackgourndAnimationRelativeLayout;
import com.example.twt.mobileplayer.view.DiscView;
import com.example.twt.mobileplayer.view.LrcView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import static com.example.twt.mobileplayer.service.MusicPlayerService.REPEAT_ALL;
import static com.example.twt.mobileplayer.service.MusicPlayerService.REPEAT_NORMAL;
import static com.example.twt.mobileplayer.service.MusicPlayerService.REPEAT_SINGLE;

/**
 * @author twt
 */
public class AudioPlayerActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "AudioPlayerActivity";
    private static final int SHOW_LYRIC = 1;
    private static final int PROGRESS = 0;
    private int mPosition;
    private MediaPlayer mPlayer;
    private Utils mUtils;


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //显示歌词
                 case SHOW_LYRIC:

                    //1.得到当前的进度
                    try {
                        int currentPosition = service.getCurrentPosition();


                        //2.把进度传入ShowLyricView控件，并且计算该高亮哪一句
                        lrcview.setshowNextLyric(currentPosition,service.getDuration());
                        //3.实时的发消息
                        mHandler.removeMessages(SHOW_LYRIC);
                        mHandler.sendEmptyMessage(SHOW_LYRIC);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case  PROGRESS:


                    try {
                        int currentPosition = service.getCurrentPosition();

                        seekbarAudio.setProgress(currentPosition);

                        //3.时间进度跟新
                        tvTime.setText(mUtils.stringForTime(currentPosition)+"/"+mUtils.stringForTime(service.getDuration()));
                        if (currentPosition>=service.getDuration()&&service.getPlayMode()!=REPEAT_SINGLE){
                            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                    mHandler.removeMessages(PROGRESS);
                    mHandler.sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
                default:
                    break;

            }




        }
    };

    private IMusicPlayerService service;//服务的代理类，通过它可以调用服务的方法
    private ServiceConnection conn = new ServiceConnection() {

        /**
         * 当连接成功的时候回调这个方法
         * @param name
         * @param iBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if(service != null){
                try {
                    if (isNotifl){
                        showViewData();
                    }else {
                        service.openAudio(mPosition);
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 当断开连接的时候回调这个方法
         * @param name
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if(service != null){
                    service.stop();
                    service = null;
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
    private Myreceiver receiver;
    private boolean isNotifl;
    private int playmode;
    private LrcView lrcview;
    private BackgourndAnimationRelativeLayout rootLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        findViews();

        getData();
        setListener();
        bindAndStartService();
        receiverBroadcast();
        makeStatusBarTransparent();
    }

    //设置透明状态栏
    private void makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    private void receiverBroadcast() {
        IntentFilter filter=new IntentFilter();
        filter.addAction(MusicPlayerService.OPENAUDIO);
        receiver = new Myreceiver();
        registerReceiver(receiver,filter);
    }






    class Myreceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            showData(null);
            // showLyric();
            // showViewData();
        }
    }
    //3.订阅方法
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = false,priority = 0)
    public void showData(MediaItem mediaItem) {

        showLyric();
        showViewData();


    }

    private void


    showLyric() {
        LrcUtil lrcUtil=new LrcUtil();

        try {

            String path=service.getAudioPath();
            lrcUtil.readFile(path);

            lrcview.setMlrclist(lrcUtil.getLrcList());

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mHandler.sendEmptyMessage(SHOW_LYRIC);

    }

    private void showViewData() {
        try {
            seekbarAudio.setMax(service.getDuration());
            tvTime.setText(mUtils.stringForTime(service.getDuration()));
            tvName.setText(service.getName());
            tvArtist.setText(service.getArtist());
            setPlaymode(playmode);
            mHandler.sendEmptyMessage(PROGRESS);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        intent.setAction("com.twt.mobileplayer_OPENAUDIO");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        //不至于实例化多个服务
        startService(intent);
    }

    private void setListener() {

        /**
         * 进度条更新
         */
        seekbarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){

                    try {

                        seekBar.setProgress(progress);
                        service.seekTo(progress);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }



    private void getData() {
        isNotifl = getIntent().getBooleanExtra("notification", false);
        if (!isNotifl){
            mPosition = getIntent().getIntExtra("position", 0);

        }
        playmode = CacheUtil.getInt(this, "playmode");
        Log.d(TAG, "getData: "+playmode);



    }

    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private DiscView mDisc;


    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2017-07-10 23:05:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        mDisc = (DiscView) findViewById(R.id.discview);
        lrcview=(LrcView) findViewById(R.id.lrcview);
        ivIcon = (ImageView)findViewById( R.id.iv_icon );
        tvArtist = (TextView)findViewById( R.id.tv_artist );
        tvName = (TextView)findViewById( R.id.tv_name );
        tvTime = (TextView)findViewById( R.id.tv_time );
        seekbarAudio = (SeekBar)findViewById( R.id.seekbar_audio );
        btnAudioPlaymode = (Button)findViewById( R.id.btn_audio_playmode );
        btnAudioPre = (Button)findViewById( R.id.btn_audio_pre );
        btnAudioStartPause = (Button)findViewById( R.id.btn_audio_start_pause );
        btnAudioNext = (Button)findViewById( R.id.btn_audio_next );
        btnLyrc = (Button)findViewById( R.id.btn_lyrc );
        rootLayout = (BackgourndAnimationRelativeLayout) findViewById(R.id.rootLayout);
        try2UpdateMusicPicBackground(R.raw.ic_music1);

        btnAudioPlaymode.setOnClickListener( this );
        btnAudioPre.setOnClickListener( this );
        btnAudioStartPause.setOnClickListener( this );
        btnAudioNext.setOnClickListener( this );
        btnLyrc.setOnClickListener( this );

       // mDisc.setMusicDataList(R.raw.ic_music1);

        mPlayer = new MediaPlayer();
        mUtils=new Utils();
        //1.EventBus注册//this是当前类
        EventBus.getDefault().register(this);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-07-10 23:05:37 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if ( v == btnAudioPlaymode ) {
            // Handle clicks for btnAudioPlaymode

            changePlaymode();
        } else if ( v == btnAudioPre ) {
            // Handle clicks for btnAudioPre
            preAudio();
        } else if ( v == btnAudioStartPause ) {
            // Handle clicks for btnAudioStartPause
            startAndPause();
        } else if ( v == btnAudioNext ) {
            // Handle clicks for btnAudioNext
            nextAudio();
        } else if ( v == btnLyrc ) {
            // Handle clicks for btnLyrc
            showLyric();
        }
    }
    private void setPlaymode(int playmode) {
        if (playmode==REPEAT_NORMAL){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
        }else if (playmode==REPEAT_SINGLE){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
        }else if (playmode==REPEAT_ALL){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
        }
    }

    private void changePlaymode() {

        if (playmode==REPEAT_NORMAL){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            playmode=REPEAT_SINGLE;
            Toast.makeText(this,"单曲循环", Toast.LENGTH_SHORT).show();

        }else if (playmode==REPEAT_SINGLE){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            playmode=REPEAT_ALL;
            Toast.makeText(this,"全部循环", Toast.LENGTH_SHORT).show();

        }else if (playmode==REPEAT_ALL){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            playmode=REPEAT_NORMAL;
            Toast.makeText(this,"顺序播放", Toast.LENGTH_SHORT).show();
        }
        try {
            service.setPlayMode(playmode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void startAndPause() {
        try {
            if (service.isPlaying()){
                service.pause();
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
            }else {
                service.start();
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void preAudio() {
        try {
            service.pre();
            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            try2UpdateMusicPicBackground(R.raw.ic_music1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void nextAudio() {
        try {
            service.next();
            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            try2UpdateMusicPicBackground(R.raw.ic_music1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 改变音乐播放背景
     * @param musicPicRes
     */
    private void try2UpdateMusicPicBackground(final int musicPicRes) {
        if (rootLayout.isNeed2UpdateBackground(musicPicRes)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Drawable foregroundDrawable = getForegroundDrawable(musicPicRes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootLayout.setForeground(foregroundDrawable);
                            rootLayout.beginAnimation();
                        }
                    });
                }
            }).start();
        }
    }

    private Drawable getForegroundDrawable(int musicPicRes) {
        /*得到屏幕的宽高比，以便按比例切割图片一部分*/
        final float widthHeightSize = (float) (DisplayUtil.getScreenWidth(AudioPlayerActivity.this)
                * 1.0 / DisplayUtil.getScreenHeight(this) * 1.0);

        Bitmap bitmap = getForegroundBitmap(musicPicRes);
        int cropBitmapWidth = (int) (widthHeightSize * bitmap.getHeight());
        int cropBitmapWidthX = (int) ((bitmap.getWidth() - cropBitmapWidth) / 2.0);

        /*切割部分图片*/
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth,
                bitmap.getHeight());
        /*缩小图片*/
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.getWidth() / 50, bitmap
                .getHeight() / 50, false);
        /*模糊化*/
        final Bitmap blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 8, true);

        final Drawable foregroundDrawable = new BitmapDrawable(blurBitmap);
        /*加入灰色遮罩层，避免图片过亮影响其他控件*/
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return foregroundDrawable;
    }

    private Bitmap getForegroundBitmap(int musicPicRes) {
        int screenWidth = DisplayUtil.getScreenWidth(this);
        int screenHeight = DisplayUtil.getScreenHeight(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), musicPicRes, options);
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        if (imageWidth < screenWidth && imageHeight < screenHeight) {
            return BitmapFactory.decodeResource(getResources(), musicPicRes);
        }

        int sample = 2;
        int sampleX = imageWidth / DisplayUtil.getScreenWidth(this);
        int sampleY = imageHeight / DisplayUtil.getScreenHeight(this);

        if (sampleX > sampleY && sampleY > 1) {
            sample = sampleX;
        } else if (sampleY > sampleX && sampleX > 1) {
            sample = sampleY;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(getResources(), musicPicRes, options);
    }

    @Override
    protected void onDestroy() {
        if (receiver!=null){
            unregisterReceiver(receiver);
            receiver=null;
        }
       if (conn!=null){
            unbindService(conn);
            conn=null;
        }
        //2.EventBus取消注册
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();

    }
}