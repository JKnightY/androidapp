package com.example.c23team2ca;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText urlEditText;
    private Button fetchButton, startGameButton;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private RecyclerView imageRecyclerView;
    private ImageAdapter imageAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> selectedImages = new ArrayList<>();
    private List<String> fetchedUrls = new ArrayList<>();
    private SoundPool soundPool;
    private int soundEffect;
    private AlertDialog progressDialog;
    private ProgressBar dialogProgressBar;
    private TextView dialogProgressTextView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlEditText = findViewById(R.id.urlEditText);
        fetchButton = findViewById(R.id.fetchButton);
        startGameButton = findViewById(R.id.startGameButton);
        progressBar = findViewById(R.id.progressBar);
        progressTextView = findViewById(R.id.progressTextView);
        imageRecyclerView = findViewById(R.id.imageRecyclerView);

        imageAdapter = new ImageAdapter(imageUrls, selectedImages, this::onImageSelected);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        imageRecyclerView.setAdapter(imageAdapter);

        fetchButton.setOnClickListener(v -> fetchImages());
        startGameButton.setOnClickListener(v -> startGame());

        // Initialize SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        soundEffect = soundPool.load(this, R.raw.sound_effect, 1);

        handler = new Handler(Looper.getMainLooper());

        // Initialize progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_dialog, null);
        dialogProgressBar = dialogView.findViewById(R.id.dialogProgressBar);
        dialogProgressTextView = dialogView.findViewById(R.id.dialogProgressTextView);
        builder.setView(dialogView);
        builder.setCancelable(false);
        progressDialog = builder.create();
    }

    private void fetchImages() {
        String url = urlEditText.getText().toString();
        if (url.isEmpty()) {
            //Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            //return;
            url = (String) urlEditText.getHint();
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressTextView.setVisibility(View.VISIBLE);
        progressTextView.setText("Downloading 0 of 20 images...");

        dialogProgressBar.setMax(20);
        dialogProgressTextView.setText("Downloading 0 of 20 images...");
        progressDialog.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new DownloadImagesTask(url));
    }

    private void onImageSelected(String imageUrl) {
        if (selectedImages.contains(imageUrl)) {
            selectedImages.remove(imageUrl);
        } else {
            if (selectedImages.size() < 6) {
                selectedImages.add(imageUrl);
                // Play sound effect when an image is selected
                soundPool.play(soundEffect, 1, 1, 0, 0, 1);
            } else {
                Toast.makeText(this, "You can only select 6 images", Toast.LENGTH_SHORT).show();
            }
        }
        startGameButton.setVisibility(selectedImages.size() == 6 ? View.VISIBLE : View.GONE);
    }

    private void startGame() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putStringArrayListExtra("selectedImages", new ArrayList<>(selectedImages));
        startActivity(intent);
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

    private class DownloadImagesTask implements Runnable {

        private final String url;

        public DownloadImagesTask(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            List<String> images = new ArrayList<>();
            try {
                Log.d(TAG, "Fetching URL: " + url);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();
                Elements elements = doc.select("img[src]");
                fetchedUrls.clear();
                for (Element img : elements) {
                    String src = img.attr("abs:src");
                    fetchedUrls.add(src);
                }
                Collections.shuffle(fetchedUrls);
                if (fetchedUrls.size() > 20) {
                    fetchedUrls = fetchedUrls.subList(0, 20);
                }

                for (int i = 0; i < fetchedUrls.size(); i++) {
                    String imageUrl = fetchedUrls.get(i);
                    images.add(imageUrl);
                    int progress = i + 1;
                    handler.post(() -> updateProgress(progress, fetchedUrls.size()));
                }

                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressTextView.setVisibility(View.GONE);
                    progressDialog.dismiss();
                    imageUrls.clear();
                    imageUrls.addAll(images);
                    imageAdapter.notifyDataSetChanged();
                    Snackbar.make(findViewById(android.R.id.content), "Images downloaded successfully", Snackbar.LENGTH_LONG).show();
                });

            } catch (IOException e) {
                Log.e(TAG, "Error fetching images", e);
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressTextView.setVisibility(View.GONE);
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Failed to fetch images", Toast.LENGTH_SHORT).show();
                });
            }
        }

        private void updateProgress(int progress, int max) {
            handler.post(() -> {
                progressTextView.setText("Downloading " + progress + " of " + max + " images...");
                progressBar.setProgress(progress);
                dialogProgressTextView.setText("Downloading " + progress + " of " + max + " images...");
                dialogProgressBar.setProgress(progress);
            });
        }
    }
}