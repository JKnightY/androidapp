package com.example.c23team2ca;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private TextView matchCountTextView, timerTextView;
    private RecyclerView gameRecyclerView;
    private GameAdapter gameAdapter;
    private List<String> gameImages = new ArrayList<>();
    private Handler timerHandler = new Handler();
    private int matchCount = 0;
    private int seconds = 0;
    private SoundPool soundPool;
    private int matchSound;
    private int winSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        matchCountTextView = findViewById(R.id.matchCountTextView);
        timerTextView = findViewById(R.id.timerTextView);
        gameRecyclerView = findViewById(R.id.gameRecyclerView);

        ArrayList<String> selectedImages = getIntent().getStringArrayListExtra("selectedImages");
        if (selectedImages != null) {
            gameImages.addAll(selectedImages);
            gameImages.addAll(selectedImages);
            Collections.shuffle(gameImages);
        }

        gameAdapter = new GameAdapter(gameImages, this::onImageRevealed);
        gameRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        gameRecyclerView.setAdapter(gameAdapter);

        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        matchSound = soundPool.load(this, R.raw.match, 1);
        winSound = soundPool.load(this, R.raw.win, 1);

        startTimer();
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                seconds++;
                int minutes = seconds / 60;
                int secs = seconds % 60;
                timerTextView.setText(String.format("%d:%02d", minutes, secs));
                timerHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void onImageRevealed(boolean isMatch) {
        if (isMatch) {
            matchCount++;
            matchCountTextView.setText(String.format("%d of 6 matches", matchCount));
            soundPool.play(matchSound, 1, 1, 0, 0, 1); // Play the match sound
            if (matchCount == 6) {
                // All matches found, play win sound and start CongratulationsActivity
                soundPool.play(winSound, 1, 1, 0, 0, 1); // Play the win sound
                Intent intent = new Intent(GameActivity.this, CongratulationsActivity.class);
                intent.putExtra("time", seconds);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release SoundPool resources
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
