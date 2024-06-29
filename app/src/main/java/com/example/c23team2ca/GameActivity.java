package com.example.c23team2ca;

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
            if (matchCount == 6) {
                // Game over, return to MainActivity
                finish();
            }
        }
    }
}
