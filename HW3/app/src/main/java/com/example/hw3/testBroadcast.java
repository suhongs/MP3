package com.example.hw3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class testBroadcast extends BroadcastReceiver {
    boolean isPlaying;
    Intent musicIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        musicIntent = new Intent(context, MusicService.class);
        musicIntent.putExtra("broad", true);
        if(intent.getAction().equals(MusicService.ACTION_PAUSE)) {
            musicIntent.setAction(MusicService.ACTION_PAUSE);
        } else if(intent.getAction().equals(MusicService.ACTION_PLAY)){
            musicIntent.setAction(MusicService.ACTION_PLAY);
        } else if(intent.getAction().equals(MusicService.ACTION_NEXT)) {
            musicIntent.setAction(MusicService.ACTION_NEXT);
        } else if(intent.getAction().equals(MusicService.ACTION_PREV)) {
            musicIntent.setAction(MusicService.ACTION_PREV);
        }
        // 다음 노래로
        // 이전 노래로로
       context.startService(musicIntent);
    }

    public boolean isPlay(){
        return isPlaying;
    }
}
