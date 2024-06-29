package com.example.c23team2ca;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<String> gameImages;
    private OnImageRevealedListener listener;
    private ImageView firstImageView;
    private ImageView secondImageView;
    private String firstImageUrl;
    private String secondImageUrl;

    public GameAdapter(List<String> gameImages, OnImageRevealedListener listener) {
        this.gameImages = gameImages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        String imageUrl = gameImages.get(position);
        GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build());
        Glide.with(holder.imageView.getContext()).load(glideUrl).into(holder.imageView);
        holder.imageView.setVisibility(View.INVISIBLE); // 默认不可见
        holder.itemView.setOnClickListener(v -> onImageClick(holder.imageView, imageUrl));
    }

    @Override
    public int getItemCount() {
        return gameImages.size();
    }

    private void onImageClick(ImageView imageView, String imageUrl) {
        if (firstImageView == null) {
            firstImageView = imageView;
            firstImageUrl = imageUrl;
            imageView.setVisibility(View.VISIBLE); // 显示图片
        } else if (secondImageView == null && imageView != firstImageView) {
            secondImageView = imageView;
            secondImageUrl = imageUrl;
            imageView.setVisibility(View.VISIBLE); // 显示图片
            checkMatch();
        }
    }

    private void checkMatch() {
        if (firstImageUrl.equals(secondImageUrl)) {
            listener.onImageRevealed(true);
            resetSelection();
        } else {
            listener.onImageRevealed(false);
            firstImageView.postDelayed(() -> {
                firstImageView.setVisibility(View.INVISIBLE); // 隐藏图片
                secondImageView.setVisibility(View.INVISIBLE); // 隐藏图片
                resetSelection();
            }, 1000);
        }
    }

    private void resetSelection() {
        firstImageView = null;
        secondImageView = null;
        firstImageUrl = null;
        secondImageUrl = null;
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    public interface OnImageRevealedListener {
        void onImageRevealed(boolean isMatch);
    }
}
