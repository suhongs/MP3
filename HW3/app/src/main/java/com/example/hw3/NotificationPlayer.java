package com.example.hw3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

public class NotificationPlayer {
    private final static int NOTIFICATION_PLAYER_ID = 101;
    private String channel_id;
    private MusicService mService;
    private NotificationManager mNotificationManager;
    private NotificationManagerBuilder mNotificationManagerBuilder;
    private boolean isForeground;

    public NotificationPlayer(MusicService service, String channel_id) {
        mService = service;
        this.channel_id = channel_id;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void updateNotificationPlayer() {
        Log.i("서비스","gggggggggggggggg");
        cancel();
        mNotificationManagerBuilder = new NotificationManagerBuilder(channel_id,mService);
        mNotificationManagerBuilder.execute();
    }

    public void removeNotificationPlayer() {
        cancel();
        mService.stopForeground(true);
        isForeground = false;
    }

    private void cancel() {
        if (mNotificationManagerBuilder != null) {
            mNotificationManagerBuilder.cancel(true);
            mNotificationManagerBuilder = null;
        }
    }

    private class NotificationManagerBuilder extends AsyncTask<Void, Void, Notification> {
        private RemoteViews mRemoteViews;
        private NotificationCompat.Builder mNotificationBuilder;
        private PendingIntent mMainPendingIntent;
        private String channel_id;
        private MusicService mService;

        public NotificationManagerBuilder(String channel_id, MusicService service){
            this.channel_id = channel_id;
            this.mService = service;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Intent mainActivity = new Intent(mService, MainActivity.class);
//            mMainPendingIntent = PendingIntent.getActivity(mService, 0, mainActivity, 0);
            mRemoteViews = createRemoteView();

            mNotificationBuilder = new NotificationCompat.Builder(mService, this.channel_id);
            mNotificationBuilder.setContentTitle("제목제목")
                    .setOngoing(true)
                    .setContentText("test")
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setContent(mRemoteViews);
            Notification notification = mNotificationBuilder.build();

            notification.priority = Notification.PRIORITY_MAX;
            notification.contentIntent = mMainPendingIntent;
            if (!isForeground) {
                isForeground = true;
                Log.i("서비스",this.channel_id + ", "  + mService.toString() + ", " + R.layout.custom_notification);
                // 서비스를 Foreground 상태로 만든다
                notification.contentView = mRemoteViews;
                this.mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
            }
        }

        @Override
        protected Notification doInBackground(Void... params) {
            mNotificationBuilder.setContent(mRemoteViews);
            mNotificationBuilder.setContentIntent(mMainPendingIntent);
            mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
            Notification notification = mNotificationBuilder.build();
            updateRemoteView(mRemoteViews, notification);
            return notification;
        }

        @Override
        protected void onPostExecute(Notification notification) {
            super.onPostExecute(notification);
            try {
                mNotificationManager.notify(NOTIFICATION_PLAYER_ID, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private RemoteViews createRemoteView() {
            RemoteViews remoteView = new RemoteViews(mService.getPackageName(), R.layout.custom_notification);
            remoteView.setTextViewText(R.id.notification_music_title, "Title");
            Intent actionTogglePlay = new Intent();
            Intent actionForward = new Intent();
            Intent actionRewind = new Intent();
            Intent actionClose = new Intent();
            PendingIntent togglePlay = PendingIntent.getService(mService, 0, actionTogglePlay, 0);
            PendingIntent forward = PendingIntent.getService(mService, 0, actionForward, 0);
            PendingIntent rewind = PendingIntent.getService(mService, 0, actionRewind, 0);
            PendingIntent close = PendingIntent.getService(mService, 0, actionClose, 0);

//            remoteView.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay);
//            remoteView.setOnClickPendingIntent(R.id.btn_forward, forward);
//            remoteView.setOnClickPendingIntent(R.id.btn_rewind, rewind);
//            remoteView.setOnClickPendingIntent(R.id.btn_close, close);
            return remoteView;
        }

        private void updateRemoteView(RemoteViews remoteViews, Notification notification) {
//            if (mService.isPlaying()) {
//                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause);
//            } else {
//                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play);
//            }
//
//            String title = mService.getAudioItem().mTitle;
//            remoteViews.setTextViewText(R.id.txt_title, title);
//            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.getAudioItem().mAlbumId);
//            Picasso.with(mService).load(albumArtUri).error(R.drawable.empty_albumart).into(remoteViews, R.id.img_albumart, NOTIFICATION_PLAYER_ID, notification);
        }

    }
//    private static final String CHANNEL_ID = "음악채널";
//    private void createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
////            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
////            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
}
