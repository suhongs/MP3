package com.example.hw3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Objects;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    private MediaPlayer mediaPlayer = null;
    private String mPath;
    private ArrayList<MusicData> list;

    int currentPosition=0;
    boolean isPlaying;
    Context context;
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


    @Override
    public void onCreate() {
        Log.i("서비스 테스트", "onCreate()");
        context = this;
        mediaPlayer = new MediaPlayer();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i("서비스 테스트", "onDestroy()");

        super.onDestroy();
    }

    // 액티비티에서 데이터 받음
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        list = (ArrayList<MusicData>) intent.getSerializableExtra("playlist");
        Log.i("테스트", list.toString());

        isPlaying = Objects.requireNonNull(intent.getExtras()).getBoolean("playing");

        if(isPlaying){
            Log.i("테스트","음악 실행");
            playMusic(list.get(1));
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
        }
    }
    public void resume(){
        if (mediaPlayer != null){
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
        }
    }
}
