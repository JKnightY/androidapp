package com.example.c23team2ca;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CongratulationsActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulations);

        TextView congratulationsTextView = findViewById(R.id.congratulationsTextView);
        TextView timeTextView = findViewById(R.id.timeTextView);
        Button backButton = findViewById(R.id.backButton);

        // Get the time from the intent
        int time = getIntent().getIntExtra("time", 0);
        int minutes = time / 60;
        int seconds = time % 60;

        congratulationsTextView.setText("Congratulations!");
        timeTextView.setText(String.format("Total time: %d:%02d", minutes, seconds));

        // Initialize and start MediaPlayer for win.mp3
        mediaPlayer = MediaPlayer.create(this, R.raw.win);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Set onClickListener for backButton
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CongratulationsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
