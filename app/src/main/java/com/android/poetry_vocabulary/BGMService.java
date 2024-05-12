package com.android.poetry_vocabulary;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class BGMService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "bgm_channel";

    private MediaPlayer mediaPlayer;  // 媒体播放器
    private AudioManager audioManager;  // 音频管理器
    private AudioFocusRequest audioFocusRequest;  // 音频焦点请求

    @Override
    public void onCreate() {
        super.onCreate();

        // 获取音频管理器
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @SuppressLint({"ForegroundServiceType", "ObsoleteSdkInt"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel(); // 创建通知渠道

        // 初始化 MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.bgm);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true); // 设置循环播放
            //设置音量
            mediaPlayer.setVolume(0.2f, 0.2f);
        } else {
            Log.e("BGMService", "Error creating media player");
            stopSelf();
        }

        // 申请音频焦点
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            // 获取到音频焦点,可以播放音乐
                            if (mediaPlayer != null) {
                                mediaPlayer.start();
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            // 永久失去音频焦点,停止播放
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            // 临时失去音频焦点,暂停播放
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                            }
                        }
                    })
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
                    .build();
            int result = audioManager.requestAudioFocus(audioFocusRequest);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // 获取到音频焦点,可以播放音乐
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        } else {
            // 创建一个AudioFocusChangeListener对象
            AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    // 失去音频焦点时的处理逻辑
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // 获得音频焦点时的处理逻辑
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                }
            };

            // 请求音频焦点
            int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // 获取到音频焦点,可以播放音乐
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        }

        // 创建通知并启动前台服务
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("背景音乐")
                .setContentText("正在播放背景音乐")
                .setSmallIcon(R.drawable.ic_music)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        Log.d("BGMService", "Service started");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        audioManager.abandonAudioFocusRequest(audioFocusRequest);
        Log.d("BGMService", "Service stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 创建通知渠道（Android 8.0 及以上版本需要）
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "BGM Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}