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
    public static final String ACTION_PLAY = "com.example.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.action.PAUSE";

    int currentPosition = 0, position = 0;
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
        mediaPlayer.stop();
        removeNotification();
        isPlaying = false; // 스레드 중지
        super.onDestroy();
    }

    boolean next;

    // 액티비티에서 데이터 받음
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        list = (ArrayList<MusicData>) intent.getSerializableExtra("playlist");
        Log.i("테스트", list.toString());

        this.intent = intent;
        position = intent.getExtras().getInt("position");
        if (intent.getExtras().getBoolean("next"))
            currentPosition = 0;
        if (intent.getAction().equals((ACTION_PLAY))) {
            Log.i("테스트", "음악 시작");
            if (currentPosition == 0)
                playMusic(list.get(position));
            else {
                Log.i("테스트", "음악 재시작");
                resume();
            }
            isPlaying = true;
        } else {
            Log.i("테스트", "음악 일시 중지");
            isPlaying = false;
            pause();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) { // Prepare 단계가 완료되었을 때 호출될 함수를 재정의한다
        mediaPlayer.start();

        // 맨 처음 음악 실행할 때만 이 함수 실행 됨.
        Log.i("테스트", "크흠흠");
        intent = new Intent("custom-event-name");
        intent.putExtra("duration", mediaPlayer.getDuration());
        intent.putExtra("currentPosition", currentPosition);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        sendMessage();
        createNotification();
    }


    Intent intent;

    private void sendMessage() {
        Log.d("테스트", mediaPlayer.getDuration() + "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isPlaying) {
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


    public void playMusic(MusicData musicDto) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPath = musicDto.getPath();
            mediaPlayer.setDataSource(mPath);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("테스트", e.getMessage());
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            currentPosition = mediaPlayer.getCurrentPosition();
            updataNortification();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
            sendMessage();
            updataNortification();
        }
    }

    private RemoteViews remoteView;

    public void updataNortification() {
        Log.i("intent",intent.getAction());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setContent(remoteView);
        if (intent.getAction().equals(ACTION_PLAY)) {
            builder.setSmallIcon(R.drawable.ic_baseline_play_arrow_24);
            remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_pause_24);
        }
        else if (intent.getAction().equals(ACTION_PAUSE)) {
            builder.setSmallIcon(R.drawable.ic_baseline_pause_24);
            remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_play_arrow_24);
        }


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    public void createNotification() {
        remoteView = new RemoteViews(getPackageName(), R.layout.custom_notification);


        remoteView.setTextViewText(R.id.notification_music_title, "제발 나와라아~~");
        remoteView.setImageViewResource(R.id.notification_music_image, R.drawable.ic_launcher_background);
        remoteView.setImageViewResource(R.id.notification_skip_previous_btn, R.drawable.ic_baseline_skip_previous_24);

        remoteView.setImageViewResource(R.id.notification_skip_next_btn, R.drawable.ic_baseline_skip_next_24);

        remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_pause_24);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
                .setContent(remoteView);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

    }

    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

}
