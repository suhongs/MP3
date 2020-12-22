package com.example.hw3;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Objects;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    private MediaPlayer mediaPlayer = null;
    private String mPath;
    private ArrayList<MusicData> list;
    public static final String ACTION_PLAY = "com.example.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.action.PAUSE";
    public static final String ACTION_NEXT = "com.example.action.NEXT";
    public static final String ACTION_PREV = "com.example.action.PREV";

    int currentPosition = 0, position = 0;
    boolean isPlaying;
    Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    testBroadcast testBroadcast;
    @Override
    public void onCreate() {
        super.onCreate();
        testBroadcast = new testBroadcast();
        context = this;
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        removeNotification();
        isPlaying = false; // 스레드 중지
        super.onDestroy();
    }

    boolean flag = true;
    // 액티비티에서 데이터 받음
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         this.intent = intent;
        if(!intent.getExtras().getBoolean("broad")) {
            list = (ArrayList<MusicData>) intent.getSerializableExtra("playlist");
        }

        if(flag) {
            position = Objects.requireNonNull(intent.getExtras()).getInt("position");
            flag = false;
        }
        if (intent.getAction().equals((ACTION_PLAY))) {
            if (currentPosition == 0)
                playMusic(list.get(position));
            else {
                resume();
            }
            isPlaying = true;
        }
        else if(intent.getAction().equals(ACTION_PAUSE)){
            isPlaying = false;
            pause();
        }
        else if(intent.getAction().equals(ACTION_NEXT)) {
            if(isPlaying) {
                isPlaying = false;
                pause();
                currentPosition = 0;
            }
            this.position++;
            if(this.position == list.size())
                this.position = 0;
            playMusic(list.get(position));
            isPlaying = true;
        }
        else if(intent.getAction().equals(ACTION_PREV)) {
            if(isPlaying) {
                isPlaying = false;
                pause();
                currentPosition = 0;
            }
            this.position--;
            if(position == -1)
                position += list.size();
            playMusic(list.get(position));
            isPlaying = true;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        sendMessage();
        createNotification();
    }

    Intent intent;
    private void sendMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isPlaying) {
                        intent = new Intent("custom-event-name");
                        intent.putExtra("duration", mediaPlayer.getDuration());
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
            isPlaying = true;
            sendMessage();
            updataNortification();
        }
    }

    private RemoteViews remoteView;

    public void updataNortification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setContent(remoteView);

        if (intent.getAction().equals(ACTION_PLAY)) {
            builder.setSmallIcon(R.drawable.ic_baseline_play_arrow_24);
            remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_pause_24);

            actionTogglePlay = new Intent(this, testBroadcast.class);
            actionTogglePlay.setAction(ACTION_PAUSE);
            PendingIntent togglePlay = PendingIntent.getBroadcast(this, 0, actionTogglePlay,0);
            remoteView.setOnClickPendingIntent(R.id.notification_play_btn, togglePlay);
        }
        else if (intent.getAction().equals(ACTION_PAUSE)) {
            builder.setSmallIcon(R.drawable.ic_baseline_pause_24);
            remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_play_arrow_24);

            actionTogglePlay = new Intent(this, testBroadcast.class);
            actionTogglePlay.setAction(ACTION_PLAY);
            PendingIntent togglePlay = PendingIntent.getBroadcast(this, 0, actionTogglePlay,0);
            remoteView.setOnClickPendingIntent(R.id.notification_play_btn, togglePlay);
        }
        else if(intent.getAction().equals(ACTION_NEXT)) {
            builder.setSmallIcon(R.drawable.ic_baseline_play_arrow_24);
            remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_pause_24);

            actionTogglePlay = new Intent(this, testBroadcast.class);
            actionTogglePlay.setAction(ACTION_PAUSE);
            PendingIntent togglePlay = PendingIntent.getBroadcast(this, 0, actionTogglePlay,0);
            remoteView.setOnClickPendingIntent(R.id.notification_play_btn, togglePlay);
        }
        else if(intent.getAction().equals(ACTION_PREV)) {
            builder.setSmallIcon(R.drawable.ic_baseline_play_arrow_24);
            remoteView.setImageViewResource(R.id.notification_play_btn, R.drawable.ic_baseline_pause_24);

            actionTogglePlay = new Intent(this, testBroadcast.class);
            actionTogglePlay.setAction(ACTION_PAUSE);
            PendingIntent togglePlay = PendingIntent.getBroadcast(this, 0, actionTogglePlay,0);
            remoteView.setOnClickPendingIntent(R.id.notification_play_btn, togglePlay);
        }


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    Intent actionTogglePlay;
    Intent actionToggleNext;
    Intent actionTogglePrev;
    Intent actionImage;

    public void createNotification() {
        remoteView = new RemoteViews(getPackageName(), R.layout.custom_notification);

        actionTogglePlay = new Intent(this, testBroadcast.class);
        actionToggleNext = new Intent(this, testBroadcast.class);
        actionTogglePrev = new Intent(this, testBroadcast.class);
        actionImage = new Intent(this, PlayMusicActivity.class);
        actionImage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        actionImage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(Contents.bigFlag)
            Contents.bigFlag = false;

        actionTogglePrev.setAction(ACTION_PREV);
        actionToggleNext.setAction(ACTION_NEXT);
        actionTogglePlay.setAction(ACTION_PAUSE);

        PendingIntent toggleNext = PendingIntent.getBroadcast(this, 0, actionToggleNext, 0);
        PendingIntent togglePlay = PendingIntent.getBroadcast(this, 0, actionTogglePlay,0);
        PendingIntent togglePrev = PendingIntent.getBroadcast(this, 0, actionTogglePrev, 0);
        PendingIntent image = PendingIntent.getActivities(this, 0, new Intent[]{actionImage}, 0);

        remoteView.setOnClickPendingIntent(R.id.notification_play_btn, togglePlay);
        remoteView.setOnClickPendingIntent(R.id.notification_skip_next_btn, toggleNext);
        remoteView.setOnClickPendingIntent(R.id.notification_skip_previous_btn, togglePrev);
        remoteView.setOnClickPendingIntent(R.id.notification_music_image, image);

        remoteView.setTextViewText(R.id.notification_music_title, list.get(position).getTitle());
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
