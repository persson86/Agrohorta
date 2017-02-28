package com.mobile.persson.agrohorta.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.persson.agrohorta.R;
import com.mobile.persson.agrohorta.database.models.PlantModelRealm;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.GridItemViewHolder> {
    private List<PlantModelRealm> mPlants;
    private Context context;
    private AdapterView.OnItemClickListener itemClickListener;

    private StorageReference mStorageRef;
    private StorageReference mImageRef;

    public ContentAdapter(Context context, List<PlantModelRealm> plants) {
        this.mPlants = plants;
        this.context = context;

        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://agro-horta.appspot.com");
        mImageRef = mStorageRef.child("images");
    }

    @Override
    public GridItemViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.plant_row, parent, false);
        return new GridItemViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(GridItemViewHolder viewHolder, int position) {
        PlantModelRealm plant = mPlants.get(position);

        viewHolder.tvPlantName.setText(plant.getPlantName());

        Glide.with(viewHolder.ivPlantImage.getContext())
                .using(new FirebaseImageLoader())
                .load(mImageRef.child(plant.getPlantImage()))
                .into(viewHolder.ivPlantImage);
    }

    @Override
    public int getItemCount() {
        return mPlants.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(GridItemViewHolder itemHolder) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    public class GridItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivPlantImage;
        public TextView tvPlantName;
        public ContentAdapter contentAdapter;

        public GridItemViewHolder(View itemView, ContentAdapter adapter) {
            super(itemView);
            contentAdapter = adapter;
            ivPlantImage = (ImageView) itemView.findViewById(R.id.ivPlantImage);
            tvPlantName = (TextView) itemView.findViewById(R.id.tvPlantName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            contentAdapter.onItemHolderClick(this);
        }
    }
}