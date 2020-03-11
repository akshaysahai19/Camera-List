package com.marsplaycamera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.marsplaycamera.databinding.CameraPreviewCardsBinding;

import java.util.ArrayList;

public class CustomCameraRecyclerAdapter extends RecyclerView.Adapter<CustomCameraRecyclerAdapter.MyViewHolder> {

    private ArrayList<String> imagePathList;
    private Context context;
    private ImageListInterface imageListInterface;
    private int type = 0;

    public interface ImageListInterface {

        void removeImage(int pos);

        void imageClicked(int pos);
    }

    public CustomCameraRecyclerAdapter(Context context, ArrayList<String> imagePathList, int ype) {
        this.imagePathList = imagePathList;
        this.context = context;
        this.type = type;
        imageListInterface = (ImageListInterface) context;
    }

    @NonNull
    @Override
    public CustomCameraRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CameraPreviewCardsBinding cardsBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                getLayout(), parent, false);
        return new MyViewHolder(cardsBinding);
    }

    private int getLayout() {
        if (type==0){
            return R.layout.camera_preview_cards;
        }else {
            return R.layout.selected_preview_cards;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CustomCameraRecyclerAdapter.MyViewHolder holder, final int position) {
        Glide.with(context)
                .load(imagePathList.get(position))
                .into(holder.cardsBinding.image);

        holder.cardsBinding.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageListInterface.removeImage(position);
            }
        });

        holder.cardsBinding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageListInterface.imageClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePathList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CameraPreviewCardsBinding cardsBinding;

        public MyViewHolder(@NonNull CameraPreviewCardsBinding cardsBinding) {
            super(cardsBinding.getRoot());
            this.cardsBinding = cardsBinding;
        }
    }

    public void setImagePathList(ArrayList<String> imagePathList) {
        this.imagePathList = imagePathList;
        notifyDataSetChanged();
    }
}
