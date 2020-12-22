package com.example.hw3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Objects;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    private MediaPlayer mediaPlayer = null;
    private String mPath;
    private ArrayList<MusicData> list;
    private NotificationPlayer mNotificationPlayer;
    public static final String ACTION_PLAY = "com.example.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.action.PAUSE";

    public static String MAIN_ACTION = "com.example.foregroundservice.action.main";
    public static String PLAY_ACTION = "com.example.foregroundservice.play.main";
    public static String NEXTPLAY_ACTION = "com.example.foregroundservice.action.nextplay";
    public static String STARTFOREGROUND_ACTION = "com.example.foregroundservice.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.example.foregroundservice.action.stopforeground";


    int currentPosition=0, position = 0;
    boolean isPlaying;
    Context context;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("서비스 테스트", "onCreate()");
        context = this;
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        Log.i("서비스 테스트", "onDestroy()");

        super.onDestroy();
    }

    boolean next;
    // 액티비티에서 데이터 받음
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        list = (ArrayList<MusicData>) intent.getSerializableExtra("playlist");
        Log.i("테스트", list.toString());

//        mNotificationPlayer = new NotificationPlayer(this, MainActivity.CHANNEL_ID);

        position = intent.getExtras().getInt("position");
        if (intent.getExtras().getBoolean("next"))
            currentPosition = 0;
        if(intent.getAction().equals((ACTION_PLAY))){
            if(currentPosition == 0)
                playMusic(list.get(position));
            else
                resume();
            isPlaying = true;
        }
        else{
            Log.i("테스트","음악 일시 중지");
            isPlaying = false;
            pause();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) { // Prepare 단계가 완료되었을 때 호출될 함수를 재정의한다
        mediaPlayer.start();
        sendMessage();
        createNotification();
    }


    Intent intent;
    private void sendMessage() {
        Log.d("테스트", mediaPlayer.getDuration() + "");
        intent = new Intent("custom-event-name");
        intent.putExtra("duration", mediaPlayer.getDuration());
        intent.putExtra("currentPosition", currentPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(isPlaying) {
                        intent.putExtra("currentPosition", mediaPlayer.getCurrentPosition());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private MusicData musicData;

    public void playMusic(MusicData musicDto) {
        mediaPlayer = new MediaPlayer();
        try {
            musicData = musicDto;
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPath = musicDto.getPath();

            mediaPlayer.setDataSource(mPath);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("테스트", e.getMessage());
        }
    }

    public void pause(){
        if (mediaPlayer != null){
            mediaPlayer.pause();
            currentPosition=mediaPlayer.getCurrentPosition();
//            updateNotificationPlayer();
        }
    }
    public void resume(){
        if (mediaPlayer != null){
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
//            updateNotificationPlayer();
        }
    }

    private void updateNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer.updateNotificationPlayer();
        }
    }

    public void createNotification(){
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.custom_notification);


        remoteView.setTextViewText(R.id.notification_music_title,"제발 나와라아~~");
        remoteView.setImageViewResource(R.id.notification_music_image, R.drawable.ic_launcher_background);
        remoteView.setImageViewResource(R.id.notification_skip_previous_btn, R.drawable.ic_baseline_skip_previous_24);
        remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_play_arrow_24);
        remoteView.setImageViewResource(R.id.notification_skip_next_btn, R.drawable.ic_baseline_skip_next_24);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
                .setContent(remoteView);



        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());

    }

}
