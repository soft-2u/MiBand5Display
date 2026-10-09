package org.soft2u.miband_5_display.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.model.BaseThemeModel;

import java.util.List;

public class BaseThemeAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private List<BaseThemeModel> listModel;
    private ItemClickListener mClickListener;
    private Context mContext;

    // data is passed into the constructor
    public BaseThemeAdapter(Context context, List<BaseThemeModel> listModel) {
        this.mContext = context;
        this.listModel = listModel;
    }

    // inflates the row layout from xml when needed

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_base_theme, parent, false);
        vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return listModel.size();
    }

//    private int lastPosition = -1;

    @Override
    public void onClick(View v) {
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // convenience method for getting data at click position
    BaseThemeModel getItem(int id) {
        return listModel.get(id);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Get the data item for this position
        BaseThemeModel baseTheme = listModel.get(position);
        // Check if an existing view is being reused, otherwise inflate the view

        ((MyViewHolder) holder).tvFolderName.setText(String.valueOf(baseTheme.getFolderName()));
        Glide.with(mContext)
                .load(baseTheme.getImgUrl())
                .placeholder(R.drawable.ic_blank_photo)
//                .apply(RequestOptions.circleCropTransform())
                .into(((MyViewHolder) holder).ivCover);
    }

    // View lookup cache
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvFolderName;
        ImageView ivCover;

        public MyViewHolder(View itemview) {
            super(itemview);
            tvFolderName = itemview.findViewById(R.id.tv_folder_name);
            ivCover = itemview.findViewById(R.id.iv_theme_cover);
            itemview.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}