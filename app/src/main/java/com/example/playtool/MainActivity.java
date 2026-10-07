package com.example.playtool;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private AudioManager audioManager;
    private TextView volumeText;

    private int maxVolume = 15;
    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 让手机实体音量键也直接控制“媒体音量”（即扬声器/听筒播放音量）
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // 初始化系统文字转语音
        tts = new TextToSpeech(this, this);

        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);
        SeekBar volumeBar = findViewById(R.id.volumeBar);
        volumeText = findViewById(R.id.volumeText);

        btnLeft.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                speak(getString(R.string.text_left));
            }
        });

        btnRight.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                speak(getString(R.string.text_right));
            }
        });

        // 音量滑条：范围设为 0 ~ 系统最大媒体音量，初始为当前系统音量
        volumeBar.setMax(maxVolume);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeBar.setProgress(current);
        updateVolumeText(current);

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setMediaVolume(progress);
                    updateVolumeText(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * 直接修改系统媒体音量（扬声器音量）。
     * 0 表示静音，maxVolume 表示最大。
     */
    private void setMediaVolume(int index) {
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateVolumeText(int index) {
        int percent = maxVolume > 0 ? (index * 100 / maxVolume) : 0;
        volumeText.setText(getString(R.string.volume_percent, percent));
    }

    private void speak(String text) {
        if (!ttsReady) {
            return;
        }
        tts.stop();
        tts.setLanguage(Locale.CHINA);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "play_tool_utterance");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CHINA);
            ttsReady = (result != TextToSpeech.LANG_MISSING_DATA
                    && result != TextToSpeech.LANG_NOT_SUPPORTED);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
