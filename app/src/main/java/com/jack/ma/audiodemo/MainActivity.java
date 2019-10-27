package com.jack.ma.audiodemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jack.ma.audiodemo.util.AudioUtil;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {


    AudioUtil audioUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //列表
        findViewById(R.id.textView).setOnClickListener(v -> {
            startActivity(new Intent(this, ListActivity.class));
        });

        Button bt = findViewById(R.id.textView1);
        //单个
        bt.setOnClickListener(v -> {
            initMedia(bt);
        });


    }

    private void initMedia(Button bt) {
        if (audioUtil == null) {
            audioUtil = new AudioUtil(AudioUtil.Companion.getTYPE_RAW());
            audioUtil.ready(this, false, R.raw.hanguo + "", new AudioUtil.onReadyListener() {
                @Override
                public void isReady(int duration, @NotNull String chinese, @NotNull String durationTime) {
                    audioUtil.play(0, 0, isPlay -> {
                        if (isPlay)
                            bt.setText("暂停");
                    });
                }

                @Override
                public void onError() {
                    audioUtil.toastShort("播放出现异常");
                    bt.setText("播放");
                }

                @Override
                public void onPlayOver() {
                    audioUtil.toastShort("播放结束");
                    bt.setText("播放");
                }
            });
        } else {
            if (audioUtil.isPlaying()) {
                bt.setText("播放");
                audioUtil.pause();
            } else {
                bt.setText("暂停");
                audioUtil.resume();
            }
        }


    }


}
