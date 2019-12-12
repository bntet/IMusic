package com.example.imusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.imusic.R;
import com.example.imusic.activity.MainActivity;
import com.example.imusic.activity.MusicPlayActivity;
import com.example.imusic.bean.MusicBean;
import com.example.imusic.layoutControl.AudioControl;

import java.io.Serializable;
import java.util.List;

public class MusicPlayService extends Service {
    public static MediaPlayer mediaPlayer = null;           //音乐播放器
    public static List<MusicBean> musicBeans;               //音乐列表
    public static int position = -1;                        //音乐索引
    public static Boolean MUSIC_STATE = false ;             //判断用户点击的歌曲是否 是当前正在播放的歌曲
    private int currentTime;

    //通知栏
    private RemoteViews remoteViews;
    private MusicBind musicBind = new MusicBind();
    //通知构造器
    private NotificationCompat.Builder mBuilder ;
    //通知
    private Notification notification;
    private NotificationManager notificationManager;
    private MusicPlayReceiver musicPlayReceiver;


    private final static int NOTIFICATION_ID = 1 ;          //标识码
    private final static int NOTIFICATION_NEXT = 2 ;        //下一首的请求码
    private final static int NOTIFICATION_PRE = 3 ;         //上一首的请求码
    private final static int NOTIFICATION_PLAY = 4 ;        //播放和暂停的请求码
    private final static int NOTIFICATION_TOUCH = 5 ;       //点击


    public class MusicBind extends Binder {
        /*
        获取Service
         */
        public MusicPlayService getService()
        {
            return MusicPlayService.this ;
        }
    }

    public MusicPlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        remoteViews = new RemoteViews(getPackageName(), R.layout.remoteview_musicplay);
        //点击通知栏
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,NOTIFICATION_TOUCH,intent,PendingIntent.FLAG_CANCEL_CURRENT);

        //点击下一首
        Intent intentNext = new Intent();
        intentNext.setAction("com.imusic.MUSIC_SWITCH") ;
        intentNext.putExtra("NEXT" , "NEXT") ;
        intentNext.putExtra("PRE" , "") ;
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_NEXT, intentNext, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.fl_play_next, nextPendingIntent);

        //点击上一首
        Intent intentPre = new Intent();
        intentPre.setAction("com.imusic.MUSIC_SWITCH") ;
        intentPre.putExtra("NEXT" , "") ;
        intentPre.putExtra("PRE" , "PRE") ;
        PendingIntent prePendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_PRE, intentPre, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.fl_play_pre, prePendingIntent);

        //播放按钮
        Intent intentPlay = new Intent("com.imusic.MUSICPLAY_BROADCAST");
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_PLAY, intentPlay, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.fl_play_toggle, playPendingIntent);

        mBuilder = new NotificationCompat.Builder(MusicPlayService.this);

        //解决Android8.0 通知问题
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        //解决Android8.0 中引进channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId("notification_id");
        }

        //设置图标
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        //设置标题
        mBuilder.setContentTitle("IMusic");
        //设置内容
        mBuilder.setContentText("Music is everywhere");
        //点击通知不会消失
        mBuilder.setAutoCancel(false);
        //点击通知效果
        mBuilder.setContentIntent(contentPendingIntent);
        notification = mBuilder.build();

        //启动前台服务
        startForeground(NOTIFICATION_ID,notification);
        System.out.println("前台服务启动ing");

        /*
        注册广播
         */
        musicPlayReceiver = new MusicPlayReceiver();
        IntentFilter filter = new IntentFilter("com.imusic.MUSICPLAY_BROADCAST");
        registerReceiver(musicPlayReceiver,filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //更新前台服务
    private void updateNotification(){
        remoteViews.setTextViewText(R.id.tv_name,musicBeans.get(position).getMusicName());
        remoteViews.setTextViewText(R.id.tv_artist,musicBeans.get(position).getArtist());
        try{
            int imagePath =  Integer.parseInt(musicBeans.get(position).getAlbum_id())  ;
            remoteViews.setImageViewResource(R.id.image_view_album , imagePath);
        }catch (Exception e)
        {
            Bitmap bt = BitmapFactory.decodeFile(musicBeans.get(position).getAlbum_id()) ;
            remoteViews.setImageViewBitmap(R.id.image_view_album , bt);

        }
        mBuilder.setContent(remoteViews);
        notification = mBuilder.build();
        startForeground(NOTIFICATION_ID,notification);
    }


    //设置音乐列表
    public void setMusicBeanList(List<MusicBean> musicBeanList){
        musicBeans = musicBeanList;
    }

    /*      如果点击的歌曲和当前歌曲不一致则停止之前的歌曲，并且播放新的歌曲
        如果点击的歌曲和当前歌曲一直，则不做处理重新播放处理，但需要做同步处理     */
    public void setMediaPlayer(MediaPlayer MPlayer){
        if(mediaPlayer == null){
            mediaPlayer = MPlayer;
            mediaPlayer.start();
        }else if(mediaPlayer != null && !MUSIC_STATE){          //用户点击的歌曲 不是 当前播放的歌曲
            mediaPlayer.stop();
            mediaPlayer = MPlayer;
            mediaPlayer.start();
        }else{
            Intent intent1 = new Intent();
            intent1.setAction("com.imusic.MUSIC_BROADCAST");
            intent1.putExtra("currentTime",mediaPlayer.getCurrentPosition());
            mediaPlayer.stop();
            mediaPlayer = MPlayer;
            sendBroadcast(intent1);
        }

        updateNotification();
    }

    //设置歌曲索引
    public void setIndex(int index)
    {
        /*
        判断用户点击的歌曲是否时当前正在播放的歌曲
         */
        if (index != position)
        {
            MUSIC_STATE = false ;
        }
        else {
            MUSIC_STATE = true ;
        }
        position = index;
    }

    private class MusicPlayReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            currentTime = mediaPlayer.getCurrentPosition();
            if(intent.getStringExtra("AudioControl") == null){
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    remoteViews.setImageViewResource(R.id.iv_play_toggle,R.drawable.ic_remote_view_play);
                    AudioControl.isPause = true;
                }else{
                    if (currentTime == 0){
                        mediaPlayer.start();
                    }else{
                        mediaPlayer.start();
                        mediaPlayer.seekTo(currentTime);
                    }
                    AudioControl.isPause = false;
                    remoteViews.setImageViewResource(R.id.iv_play_toggle,R.drawable.ic_remote_view_pause);
                }
            }
            updateNotification();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        //注销广播
        unregisterReceiver(musicPlayReceiver);
    }
}
