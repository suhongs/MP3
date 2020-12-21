package com.example.hw3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URLDecoder;
import java.util.ArrayList;

public class PlayMusicActivity extends AppCompatActivity {
    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PAUSE = "PAUSE";
    private boolean isPlaying = false;

    private Intent musicIntent;
    private ArrayList<MusicData> list;
    private int position;

    private ImageView skipPreviousBtn, playBtn, skipNextBtn;
    private ProgressBar musicProgressBar;
    private TextView musicTitle, musicPlayingText, musicFinishText;

    private int duration, currentPosition;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            duration = intent.getExtras().getInt("duration");
            currentPosition = intent.getExtras().getInt("currentPosition");
            Log.i("테스트","전체 길이 : " + duration);
            musicFinishText.setText(Integer.toString(duration/1000/60)+":"+String.format("%02d", duration/1000&60));
//
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        while(isPlaying) { // 재생 중이면 동작?
//                            Thread.sleep(1);
//                            int time = currentPosition ;
//                            Log.i("테스트", "재생 길이 : " + time);
//                            musicPlayingText.setText(String.valueOf(time));
//                            progressUpdate(duration, currentPosition);
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            thread.start();

        }
    };
    private void init(){
        musicTitle = findViewById(R.id.music_title);
        playBtn = findViewById(R.id.play_btn);
        musicPlayingText = findViewById(R.id.music_playing_text);
        musicFinishText = findViewById(R.id.music_finish_text);
        musicProgressBar = findViewById(R.id.music_progressBar);
//        progressUpdate("","");
    }

    private void progressUpdate(int duration, int currentPosition){
        musicProgressBar.setVisibility(ProgressBar.VISIBLE);
        musicProgressBar.setMax(duration);
//        musicProgressBar.setOn
        musicProgressBar.setProgress(currentPosition);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // action 이름이 "custom-event-name"으로 정의된 intent를 수신하게 된다.
        // observer의 이름은 mMessageReceiver이다.
        LocalBroadcastManager.getInstance(this).registerReceiver( mMessageReceiver, new IntentFilter("custom-event-name"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        init(); // 위젯 초기화
        Intent intent = getIntent();
        list = (ArrayList<MusicData>) intent.getSerializableExtra("playlist");
        position = intent.getExtras().getInt("position");

        updateUI(position);
        Log.i("PlayMusicActivity 테스트", list.toString());

        musicIntent = new Intent(this, MusicService.class);
        musicIntent.putExtra("playlist", list);
        musicIntent.putExtra("position",position);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver( mMessageReceiver);
    }

    public void onClick(View v) throws InterruptedException {
        switch (v.getId()) {
            case R.id.play_btn:
                if(isPlaying) { // 음악 재생
//                    musicIntent.putExtra("position",);
                    musicIntent.putExtra("playing",false);
                    // 이미지 변경
                    playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    isPlaying = false;
                }else{ // 일시 정지
                    musicIntent.putExtra("playing",true);
//                    startService(musicIntent);

                    // 이미지 변경
                    playBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                    isPlaying = true;
                }
                break;
            case R.id.skip_next_btn:
                musicIntent.putExtra("next",true);
                musicIntent.putExtra("playing", false);
                startService(musicIntent);
                position+=1;
                if(position == list.size())
                    position = 0;
                musicIntent.putExtra("position", position);
                musicIntent.putExtra("playing", true);
                updateUI(position);
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                isPlaying = true;
                break;
            case R.id.skip_previous_btn:
                musicIntent.putExtra("next",true);
                musicIntent.putExtra("playing", false);
                startService(musicIntent);
                Log.i("list" , Integer.toString(list.size()));
                position -= 1;
                if(position == -1)
                    position += list.size();
                musicIntent.putExtra("position", position);
                musicIntent.putExtra("playing", true);
                updateUI(position);
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                isPlaying = true;
                break;
        }
        startService(musicIntent);
        musicIntent.putExtra("next",false);
    }

    public void updateUI(int position){
        musicTitle.setText(URLDecoder.decode(list.get(position).getTitle()));
    }

}