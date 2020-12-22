package com.example.hw3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.media.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity{

    String[] permissions = { "android.permission.READ_EXTERNAL_STORAGE","android.permission.FOREGROUND_SERVICE"};
    private String TAG = "MainActivity";
    private ListView musicListView;
    private MusicAdapter adapter;
    public static final String CHANNEL_ID = "음악채널";

    ArrayList<MusicData> list = new ArrayList<>();

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean checkPermissions(){
        int result;

        List<String> permissionList = new ArrayList<>();
        for(String pm: permissions){
            result = ContextCompat.checkSelfPermission(this, pm);
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(pm);
            }
        }
        if(!permissionList.isEmpty()){
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]), 101);
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        createNotificationChannel();
        if(Build.VERSION.SDK_INT >= 23)
            checkPermissions();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 101);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getMusicData();
        musicListView = (ListView)findViewById(R.id.listView);
        adapter = new MusicAdapter(this, list);
        musicListView.setAdapter(adapter);

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PlayMusicActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("playlist", list);
                startActivity((intent));
            }
        });
    }

    private void getMusicData(){
        String[] projection = {MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media._ID,  MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.AudioColumns.DATA,MediaStore.Audio.Media.ARTIST};

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);

        if(cursor != null) {
            Log.i("시발거", Integer.toString(cursor.getCount()));
            while (cursor.moveToNext() ) {
                MusicData data = new MusicData();
                Log.i("rktn", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                data.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                data.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                data.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                data.setDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                data.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
                list.add(data);
                //adapter.notifyDataSetChanged();
            }
        }
    }
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
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
