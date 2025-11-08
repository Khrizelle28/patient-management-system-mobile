package com.example.patientmanagementsystemmobile.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AlarmSoundPlayer {
    private static final String TAG = "AlarmSoundPlayer";
    private static MediaPlayer mediaPlayer;
    private static final int SOUND_DURATION_MS = 15000; // 15 seconds

    /**
     * Play alarm sound for a specified duration
     */
    public static void playAlarmSound(Context context) {
        try {
            // Stop any existing sound
            stopAlarmSound();

            // Get default notification/alarm sound
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            // Create MediaPlayer
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, alarmUri);

            // Set audio attributes for alarm
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);

            // Set looping to true so sound repeats
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

            Log.d(TAG, "Alarm sound started - will play for " + (SOUND_DURATION_MS / 1000) + " seconds");

            // Stop sound after specified duration
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                stopAlarmSound();
                Log.d(TAG, "Alarm sound stopped after timeout");
            }, SOUND_DURATION_MS);

        } catch (Exception e) {
            Log.e(TAG, "Error playing alarm sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop the alarm sound if it's playing
     */
    public static void stopAlarmSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
                Log.d(TAG, "Alarm sound stopped and released");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping alarm sound: " + e.getMessage());
            }
        }
    }
}
