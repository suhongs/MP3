package com.example.hw3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URLDecoder;
import java.util.ArrayList;

public class PlayMusicActivity extends AppCompatActivity {
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
            musicFinishText.setText(Integer.toString(duration/1000/60)+":"+String.format("%02d", duration/1000%60));

            musicPlayingText.setText(Integer.toString(currentPosition/1000/60)+":"+String.format("%02d", currentPosition/1000%60));
            progressUpdate(duration, currentPosition);

        }
    };

    private void init() {
        musicTitle = findViewById(R.id.music_title);
        playBtn = findViewById(R.id.play_btn);
        musicPlayingText = findViewById(R.id.music_playing_text);
        musicFinishText = findViewById(R.id.music_finish_text);
        musicProgressBar = findViewById(R.id.music_progressBar);

    }

    private void progressUpdate(int duration, int currentPosition) {
        musicProgressBar.setVisibility(ProgressBar.VISIBLE);
        musicProgressBar.setMax(duration);
        musicProgressBar.setProgress(currentPosition);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        init();
        Intent intent = getIntent();

        list = (ArrayList<MusicData>) intent.getSerializableExtra("playlist");
        position = intent.getExtras().getInt("position");

        updateUI(position);

        musicIntent = new Intent(this, MusicService.class);
        musicIntent.putExtra("playlist", list);
        musicIntent.putExtra("position", position);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    public void onClick(View v) throws InterruptedException {
        musicIntent.putExtra("borad", false);
        switch (v.getId()) {
            case R.id.play_btn:
                if (isPlaying == false) { // 음악 재생
                    musicIntent.setAction(MusicService.ACTION_PLAY);
                    // 이미지 변경
                    playBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                    isPlaying = true;
                }else{ // 일시 정지
                    musicIntent.setAction(MusicService.ACTION_PAUSE);
//                    startService(musicIntent);

                    // 이미지 변경
                    playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                    isPlaying = false;
                }
                break;
            case R.id.skip_next_btn:
                musicIntent.setAction(MusicService.ACTION_NEXT);
                position+=1;
                if(position == list.size())
                    position = 0;
                updateUI(position);
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                isPlaying = true;
                break;
            case R.id.skip_previous_btn:
                musicIntent.setAction(MusicService.ACTION_PREV);
                position -= 1;
                if(position == -1)
                    position += list.size();
                updateUI(position);
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                isPlaying = true;
                break;
        }
        startService(musicIntent);
    }

    public void updateUI(int position){
        musicFinishText.setText((Integer.parseInt(list.get(position).getDuration())/1000/60)+":"+Integer.parseInt(list.get(position).getDuration())/1000%60);
        musicTitle.setText(URLDecoder.decode(list.get(position).getTitle()));
    }

    @Override
    protected void onDestroy() {
        stopService(musicIntent);
        super.onDestroy();
    }
}
