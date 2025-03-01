package com.example.c23team2ca;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private TextView matchCountTextView, timerTextView, playerTurnTextView;
    private RecyclerView gameRecyclerView;
    private GameAdapter gameAdapter;
    private List<String> gameImages = new ArrayList<>();
    private Handler timerHandler = new Handler();
    private int matchCount = 0;
    private int seconds = 0;
    private SoundPool soundPool;
    private int matchSound;
    private int winSound;
    private int errorSound; // 配对错误的音效
    private MediaPlayer backgroundMusicPlayer; // 背景音乐播放器
    private int currentPlayer = 1;
    private int player1Matches = 0;
    private int player2Matches = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        matchCountTextView = findViewById(R.id.matchCountTextView);
        timerTextView = findViewById(R.id.timerTextView);
        gameRecyclerView = findViewById(R.id.gameRecyclerView);
        playerTurnTextView = findViewById(R.id.playerTurnTextView);

        ArrayList<String> selectedImages = getIntent().getStringArrayListExtra("selectedImages");
        if (selectedImages != null) {
            gameImages.addAll(selectedImages);
            gameImages.addAll(selectedImages);
            Collections.shuffle(gameImages);
        }

        gameAdapter = new GameAdapter(gameImages, this::onImageRevealed);
        gameRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        gameRecyclerView.setAdapter(gameAdapter);
        gameRecyclerView.scheduleLayoutAnimation();

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
        errorSound = soundPool.load(this, R.raw.error_sound, 1); // 配对错误的音效

        // Initialize MediaPlayer for background music
        backgroundMusicPlayer = MediaPlayer.create(this, R.raw.background_music);
        backgroundMusicPlayer.setLooping(true);
        backgroundMusicPlayer.start();

        startTimer();
        updatePlayerTurnTextView();
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
            soundPool.play(matchSound, 1, 1, 0, 0, 1); // Play the match sound
            if (currentPlayer == 1) {
                player1Matches++;
            } else {
                player2Matches++;
            }
            matchCount++;
            matchCountTextView.setText(String.format("Score: %d:%d", player1Matches, player2Matches));
            if (matchCount == 6) {
                // All matches found, play win sound and start CongratulationsActivity
                soundPool.play(winSound, 1, 1, 0, 0, 1); // Play the win sound
                announceWinner();
                Intent intent = new Intent(GameActivity.this, CongratulationsActivity.class);
                intent.putExtra("time", seconds);
                startActivity(intent);
                finish();
            }
        } else {
            soundPool.play(errorSound, 1, 1, 0, 0, 1); // Play the error sound
            switchPlayer();
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        updatePlayerTurnTextView();
        if (currentPlayer == 1) {
            matchCountTextView.setText(String.format("Player 1: %d", player1Matches));
        } else {
            matchCountTextView.setText(String.format("Player 2: %d", player2Matches));
        }
    }

    private void updatePlayerTurnTextView() {
        playerTurnTextView.setText(String.format("Player %d's Turn", currentPlayer));
    }

    private void announceWinner() {
        String winnerMessage;
        if (player1Matches > player2Matches) {
            winnerMessage = String.format("Player 1 wins! Score: Player 1: %d, Player 2: %d", player1Matches, player2Matches);
        } else if (player2Matches > player1Matches) {
            winnerMessage = String.format("Player 2 wins! Score: Player 1: %d, Player 2: %d", player1Matches, player2Matches);
        } else {
            winnerMessage = String.format("It's a tie! Score: Player 1: %d, Player 2: %d", player1Matches, player2Matches);
        }
        Toast.makeText(this, winnerMessage, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release SoundPool resources
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        // Release MediaPlayer resources
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.release();
            backgroundMusicPlayer = null;
        }
    }
}