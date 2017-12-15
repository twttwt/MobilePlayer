package com.example.twt.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.twt.mobileplayer.IMusicPlayerService;
import com.example.twt.mobileplayer.R;
import com.example.twt.mobileplayer.activity.AudioPlayerActivity;
import com.example.twt.mobileplayer.domain.LrcContent;
import com.example.twt.mobileplayer.domain.MediaItem;
import com.example.twt.mobileplayer.utils.CacheUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerService extends Service {
    public static final String OPENAUDIO = "com.twt.mobileplayer_OPENAUDIO";
    private int position;
    private ArrayList<MediaItem> mediaItems;
    private ArrayList<LrcContent> lrcList;
    private MediaItem mediaItem;
    private MediaPlayer mediaPlayer;
    private NotificationManager manager;
    /**
     * 顺序播放
     */
    public static final int REPEAT_NORMAL = 1;
    /**
     * 单曲循环
     */
    public static final int REPEAT_SINGLE = 2;
    /**
     * 全部循环
     */
    public static final int REPEAT_ALL = 0;

    /**
     * 播放模式
     */
    private int playmode = REPEAT_NORMAL;
    private int currentTime;
    private int duration;
    private int index;

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getDataFromLocal();


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mStub;
    }
    private IMusicPlayerService.Stub mStub=new IMusicPlayerService.Stub(){
        MusicPlayerService service = MusicPlayerService.this;
        @Override
        public void openAudio(int position) throws RemoteException {
             service.openAudio(position);



        }


        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
                    service.next();
        }

        @Override
        public void pre() throws RemoteException {
                    service.pre();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException {
                     service.setPlayMode(playmode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int progress) throws RemoteException {
            service.seekTo(progress);
        }

        @Override
        public int lrcIndex() throws RemoteException {
            return service.lrcIndex();
        }
    };
    /**
     * 根据位置打开对应的音频文件
     * @param position
     */
    private void openAudio(int position){
        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0) {

            mediaItem = mediaItems.get(position);

            if (mediaPlayer != null) {
            //    mediaPlayer.release();
                mediaPlayer.reset();
            }

            try {
                mediaPlayer = new MediaPlayer();
                //设置监听：播放出错，播放完成，准备好
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();

                if (playmode==REPEAT_SINGLE){
                    mediaPlayer.setLooping(true);
                }else {
                    mediaPlayer.setLooping(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            Toast.makeText(MusicPlayerService.this, "还没有数据", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * 播放音乐
     */

    private void start(){
        mediaPlayer.start();

         manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent=new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("notification",true);//标识来自状态拦
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("小熊音乐")
                .setContentText("正在播放:"+getName())
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);

    }

    /**
     * 播暂停音乐
     */
    private void pause(){
        mediaPlayer.pause();
    }

    /**
     * 停止
     */
    private void stop(){
        if (mediaPlayer!=null){
            mediaPlayer.stop();
        }

    }

    /**
     * 得到当前的播放进度
     * @return
     */
    private int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }


    /**
     * 得到当前音频的总时长
     * @return
     */
    private int getDuration(){
        return (int) mediaItems.get(position).getDuration();
    }

    /**
     * 得到艺术家
     * @return
     */
    private String getArtist(){
        return mediaItems.get(position).getArtist();
    }

    /**
     * 得到歌曲名字
     * @return
     */
    private String getName(){
        return mediaItems.get(position).getDisplay_name();
    }


    /**
     * 得到歌曲播放的路径
     * @return
     */
    private String getAudioPath(){
        return mediaItems.get(position).getData();
    }

    /**
     * 播放下一个视频
     */
    private void next(){
        //1.根据当前的播放模式，设置下一个的位置
        setNextPosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openNextAudio();
    }

    private void openNextAudio() {
        int playmode = getPlayMode();
        if(playmode==MusicPlayerService.REPEAT_NORMAL){
            if(position < mediaItems.size()){
                //正常范围
                openAudio(position);
            }else{
                position = mediaItems.size()-1;
            }
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            openAudio(position);
        }else if(playmode ==MusicPlayerService.REPEAT_ALL){
            openAudio(position);
        }else{
            if(position < mediaItems.size()){
                //正常范围
                openAudio(position);
            }else{
                position = mediaItems.size()-1;
            }
        }
    }

    private void setNextPosition() {
        int playmode = getPlayMode();
        if(playmode==MusicPlayerService.REPEAT_NORMAL){
            position++;
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            position++;
            if(position >=mediaItems.size()){
                position = 0;
            }
        }else if(playmode ==MusicPlayerService.REPEAT_ALL){
            position++;
            if(position >=mediaItems.size()){
                position = 0;
            }
        }else{
            position++;
        }
    }


    /**
     * 播放上一个视频
     */
    private void pre(){
        //1.根据当前的播放模式，设置上一个的位置
        setPrePosition();
        //2.根据当前的播放模式和下标位置去播放音频
        openPreAudio();
    }
    /**
     * 根据当前的播放模式，设置上一个的位置
     */
    private void setPrePosition() {
        int playmode = getPlayMode();
        if(playmode==MusicPlayerService.REPEAT_NORMAL){
            position--;
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            position--;
            if(position < 0){
                position = mediaItems.size()-1;
            }
        }else if(playmode ==MusicPlayerService.REPEAT_ALL){
            position--;
            if(position < 0){
                position = mediaItems.size()-1;
            }
        }else{
            position--;
        }
    }

    private void openPreAudio() {
        int playmode = getPlayMode();
        if(playmode==MusicPlayerService.REPEAT_NORMAL){
            if(position >= 0){
                //正常范围
                openAudio(position);
            }else{
                position = 0;
            }
        }else if(playmode == MusicPlayerService.REPEAT_SINGLE){
            openAudio(position);
        }else if(playmode ==MusicPlayerService.REPEAT_ALL){
            openAudio(position);
        }else{
            if(position >= 0){
                //正常范围
                openAudio(position);
            }else{
                position = 0;
            }
        }
    }

    /**
     * 设置播放模式
     * @param playmode
     */
    private void setPlayMode(int playmode){
        this.playmode = playmode;
        CacheUtil.setInt(this,"playmode",playmode);


        if (playmode==REPEAT_SINGLE){
            mediaPlayer.setLooping(true);
        }else {
            mediaPlayer.setLooping(false);
        }
    }


    /**
     * 得到播放模式
     * @return
     */
    private int getPlayMode(){
        return playmode;
    }

    /**
     * 准备播放
     */
    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener{

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
        /**
         * 发送广播通知activity
         */
        //notifyChange(OPENAUDIO);
        EventBus.getDefault().post(mediaItem);
    }
}

    private void notifyChange(String openaudio) {
        Intent intent=new Intent(openaudio);
        sendBroadcast(intent);
    }



    /**
     * 播放完成
     */
    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
           next();
        }
    }

    private void prepareToPlay() {
        MediaItem mediaItem = mediaItems.get(position);
        String data = mediaItem.getData();
        try {
            mediaPlayer.setDataSource(data);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放出错
     */
    class MyOnErrorListener implements MediaPlayer.OnErrorListener{

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }

    /**
     * 是否正在播放
     */
    private boolean isPlaying(){
       return mediaPlayer.isPlaying();

    }

    /**
     * 更新进度
     */
    public void seekTo(int progress){
        mediaPlayer.seekTo(progress);
    }

    private void getDataFromLocal() {
        new Thread(){
            @Override
            public void run() {

                mediaItems=new ArrayList<MediaItem>();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                ContentResolver contentResolver = getContentResolver();
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
                        mediaItems.add(mediaItem);
                        mediaItem.setDisplay_name(cursor.getString(0));
                        mediaItem.setDuration(cursor.getLong(1));
                        mediaItem.setSize(cursor.getLong(2));
                        mediaItem.setData(cursor.getString(3));
                        mediaItem.setArtist(cursor.getString(4));
                    }
                    cursor.close();
                }

            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (manager!=null){
            manager.cancel(1);
        }

        super.onDestroy();
    }
    /**
     * 根据时间获取歌词显示的索引值
     * @return
     */
    public int lrcIndex() {

        if(mediaPlayer.isPlaying()) {
            currentTime = getCurrentPosition();
            duration = getDuration();
        }
        if(currentTime < duration) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
                    if (currentTime < lrcList.get(i).getLrcTime() && i == 0) {
                        index = i;
                    }
                    if (currentTime > lrcList.get(i).getLrcTime()
                            && currentTime < lrcList.get(i + 1).getLrcTime()) {
                        index = i;
                    }
                }
                if (i == lrcList.size() - 1
                        && currentTime > lrcList.get(i).getLrcTime()) {
                    index = i;
                }
            }
        }
        return index;
    }
}
