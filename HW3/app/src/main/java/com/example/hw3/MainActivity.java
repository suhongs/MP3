package com.example.hw3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=1001;
    String[] permissions = { "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
//    private ListView listView;
    public static ArrayList<MusicData> list;
    private Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= 23)
            checkPermissions();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 전화에 대한 권한을 보유했는지 체크하고 GRANTED 되어있지 않으면 requestPermissions를 호출해 권한을 요청한다.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 101);
            // new String[] 배열에 다양한 권한을 일괄적으로 요청하도록 할 수 있지만 왠만해서는 이렇게 하지 말아라

        }

        goButton = (Button)findViewById(R.id.go);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayMusicActivity.class);
//                intent.putExtra("position",positon);
                Log.i("테스트",list.toString());
                intent.putExtra("playlist", list);
                startActivity(intent);
            }
        });
        getMusicList();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101){
            Log.i("테스트","처음에 응답 받음?");
            getMusicList();
        }
    }

    public  void getMusicList(){
        Log.i("테스트","들어오냐?");
        list = new ArrayList<>();
        //가져오고 싶은 컬럼 명을 나열합니다. 음악의 아이디, 앰블럼 아이디, 제목, 아스티스트 정보를 가져옵니다.
        String[] projection = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, // 노래 길이
                MediaStore.Audio.AudioColumns.DATA
        };

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        Log.i("테스트",cursor.toString());
        if(cursor != null) {
            while (cursor.moveToNext()) {
                MusicData musicDto = new MusicData();
                musicDto.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                musicDto.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                musicDto.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                musicDto.setDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                musicDto.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
                Log.i("테스트",musicDto.getAlbumId());
                list.add(musicDto);
            }
        }
//        cursor.close();
    }
}
