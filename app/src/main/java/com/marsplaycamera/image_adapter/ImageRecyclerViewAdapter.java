package com.marsplaycamera.image_adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.marsplaycamera.R;
import com.marsplaycamera.databinding.ImagePreviewBinding;
import com.marsplaycamera.utils.Utils;

import java.util.ArrayList;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<String> imagesModelArrayList;
    private Activity activity;
    private ImageModelInterface imageModelInterface;

    public interface ImageModelInterface{

        void deleteImage(int pos);

        void showImageDialog(int pos);

    }

    public ImageRecyclerViewAdapter(ArrayList<String> imagesModelArrayList, Activity activity) {
        this.imagesModelArrayList = imagesModelArrayList;
        this.activity = activity;
        imageModelInterface = (ImageModelInterface) activity;
    }


    @NonNull
    @Override
    public ImageRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImagePreviewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.image_preview, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageRecyclerViewAdapter.MyViewHolder holder, final int position) {
        Glide.with(holder.binding.getRoot().getContext())
                .load(imagesModelArrayList.get(position))
                .into(holder.binding.image);

        holder.binding.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageModelInterface.deleteImage(position);
            }
        });

        holder.binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageModelInterface.showImageDialog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesModelArrayList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImagePreviewBinding binding;

        public MyViewHolder(@NonNull ImagePreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    public void setImagesModelArrayList(ArrayList<String> imagesModelArrayList) {
        this.imagesModelArrayList = imagesModelArrayList;
        notifyDataSetChanged();
    }

}
