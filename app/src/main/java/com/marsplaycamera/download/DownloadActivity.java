package com.marsplaycamera.download;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.marsplaycamera.R;
import com.marsplaycamera.databinding.ImageListActivityBinding;
import com.marsplaycamera.databinding.ImagePreviewBinding;
import com.marsplaycamera.model.ImageModel;

public class DownloadActivity extends AppCompatActivity {

    ImageListActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.image_list_activity);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.imageRecyclerview.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        setupRecyclerView();

        binding.add.setVisibility(View.GONE);
        binding.upload.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        Query query = FirebaseFirestore.getInstance().collection("Images")
                .document("Upload").collection("Testing");

        FirestoreRecyclerOptions<ImageModel> options =
                new FirestoreRecyclerOptions.Builder<ImageModel>()
                        .setQuery(query, ImageModel.class)
                        .setLifecycleOwner(this)
                        .build();

        FirestoreRecyclerAdapter<ImageModel, MyViewHolder> adapter =
                new FirestoreRecyclerAdapter<ImageModel, MyViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i, @NonNull ImageModel model) {

                        binding.noImage.setVisibility(View.GONE);

                        Glide.with(DownloadActivity.this)
                                .load(model.getPath())
                                .into(myViewHolder.binding.image);

                        myViewHolder.binding.remove.setVisibility(View.GONE);

                    }

                    @NonNull
                    @Override
                    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        ImagePreviewBinding binding = DataBindingUtil.inflate(LayoutInflater.from(DownloadActivity.this),
                                R.layout.image_preview, parent, false);
                        return new MyViewHolder(binding);
                    }
                };
        binding.imageRecyclerview.setAdapter(adapter);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {

        ImagePreviewBinding binding;

        public MyViewHolder(@NonNull ImagePreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
