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
        createNotificationChannel();
        mNotificationPlayer = new NotificationPlayer(this, CHANNEL_ID);
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
        showCustomLayoutNotification();

        isPlaying = Objects.requireNonNull(intent.getExtras()).getBoolean("playing");
        position = intent.getExtras().getInt("position");
        if (intent.getExtras().getBoolean("next"))
            currentPosition = 0;
        if(isPlaying){
            Log.i("테스트","음악 실행");
            playMusic(list.get(position));
        }
        else{
            Log.i("테스트","음악 일시 중지");
            pause();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) { // Prepare 단계가 완료되었을 때 호출될 함수를 재정의한다
        Log.i("테스트", "준비됨?");
        resume();
        Log.i("테스트", mediaPlayer.isPlaying() + "");
        sendMessage();
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
            updateNotificationPlayer();
        }
    }
    public void resume(){
        if (mediaPlayer != null){
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
            updateNotificationPlayer();
        }
    }

    private void updateNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer.updateNotificationPlayer();
        }
    }



















































































    // 알림의 콘텐츠와 채널 설정
    private void createNotification(){
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
//        remoteViews.setTextViewText(R.id.notification_music_title, /*list.get(position).getTitle()*/ "hi");
//        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
//        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
//                .setCustomBigContentView(remoteViews);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            builder.setCategory(Notification.CATEGORY_MESSAGE)
//                    .setPriority(Notification.PRIORITY_HIGH)
//                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//        }
//        return builder;
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification2);

        // Apply the layouts to the notification
        Notification customNotification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(notificationLayout)
                .build();

        //startForeground(101,customNotification );
    }

    private void showCustomLayoutNotification(){
        createNotification();
//        NotificationCompat.Builder mBuilder = createNotification();
//        startForeground(101,mBuilder.build() );
    }
    private PendingIntent createPendingIntent(){ // 알림 클릭했을 시 작업
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private static final String CHANNEL_ID = "음악채널";
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
//            notificationManager.
        }
    }

}
