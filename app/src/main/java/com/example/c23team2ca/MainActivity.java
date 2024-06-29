package com.example.c23team2ca;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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

        imageAdapter = new ImageAdapter(imageUrls, this::onImageSelected);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        imageRecyclerView.setAdapter(imageAdapter);

        fetchButton.setOnClickListener(v -> fetchImages());
        startGameButton.setOnClickListener(v -> startGame());
    }

    private void fetchImages() {
        String url = urlEditText.getText().toString();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        final String finalUrl = url;

        progressBar.setVisibility(View.VISIBLE);
        progressTextView.setVisibility(View.VISIBLE);
        progressTextView.setText("Downloading 0 of 20 images...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Log.d(TAG, "Fetching URL: " + finalUrl);
                Document doc = Jsoup.connect(finalUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();
                Elements images = doc.select("img[src]");
                fetchedUrls.clear();
                for (Element img : images) {
                    String src = img.attr("abs:src");
                    fetchedUrls.add(src);
                }
                Collections.shuffle(fetchedUrls);
                if (fetchedUrls.size() > 20) {
                    fetchedUrls = fetchedUrls.subList(0, 20);
                }

                handler.post(() -> {
                    imageUrls.clear();
                    imageUrls.addAll(fetchedUrls);
                    imageAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    progressTextView.setVisibility(View.GONE);
                });
            } catch (IOException e) {
                Log.e(TAG, "Error fetching images", e);
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressTextView.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Failed to fetch images", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void onImageSelected(String imageUrl) {
        if (selectedImages.contains(imageUrl)) {
            selectedImages.remove(imageUrl);
        } else {
            if (selectedImages.size() < 6) {
                selectedImages.add(imageUrl);
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
}
