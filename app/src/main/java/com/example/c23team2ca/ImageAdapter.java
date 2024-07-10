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

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> imageUrls;
    private List<String> selectedImages;
    private OnImageSelectedListener onImageSelectedListener;

    public interface OnImageSelectedListener {
        void onImageSelected(String imageUrl);
    }

    public ImageAdapter(List<String> imageUrls, List<String> selectedImages, OnImageSelectedListener onImageSelectedListener) {
        this.imageUrls = imageUrls;
        this.selectedImages = selectedImages;
        this.onImageSelectedListener = onImageSelectedListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build());

        Glide.with(holder.imageView.getContext())
                .load(glideUrl)
                .into(holder.imageView);

        if (selectedImages.contains(imageUrl)) {
            holder.selectedIndicator.setVisibility(View.VISIBLE);
            holder.imageView.setAlpha(0.5f);
        } else {
            holder.selectedIndicator.setVisibility(View.GONE);
            holder.imageView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            onImageSelectedListener.onImageSelected(imageUrl);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView selectedIndicator;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            selectedIndicator = itemView.findViewById(R.id.selectedIndicator);
        }
    }
}