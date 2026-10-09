package org.soft2u.miband_5_display.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.model.ThemeModel;

import java.util.List;

public class DisplayAdapterSlideshow extends RecyclerView.Adapter implements View.OnClickListener {
    private List<ThemeModel> listModel;
    private ItemClickListener mClickListener;
    private Context mContext;

    // data is passed into the constructor
    public DisplayAdapterSlideshow(Context context, List<ThemeModel> listModel) {
        this.mContext = context;
        this.listModel = listModel;
    }

    // inflates the row layout from xml when needed

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_display_slideshow, parent, false);
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
    ThemeModel getItem(int id) {
        return listModel.get(id);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Get the data item for this position
        ThemeModel model = listModel.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        switch (position) {
            case 0:
                ((MyViewHolder) holder).layoutRoot.setBackgroundResource(R.drawable.sharp_slideshow_1);
                break;
            case 1:
                ((MyViewHolder) holder).layoutRoot.setBackgroundResource(R.drawable.sharp_slideshow_2);
                break;
            case 2:
                ((MyViewHolder) holder).layoutRoot.setBackgroundResource(R.drawable.sharp_slideshow_3);
                break;
            case 3:
                ((MyViewHolder) holder).layoutRoot.setBackgroundResource(R.drawable.sharp_slideshow_4);
                break;
            default:
                ((MyViewHolder) holder).layoutRoot.setBackgroundResource(R.drawable.sharp_slideshow_4);
                break;
        }
        ((MyViewHolder) holder).tvTitle.setText(String.valueOf(model.getTitle()));
        ((MyViewHolder) holder).tvAuthor.setText(String.valueOf(model.getAuthor()));
        Glide.with(mContext)
                .load(model.getImgUrl())
                .placeholder(R.drawable.ic_blank_photo)
                .into(((MyViewHolder) holder).ivCover);
    }

    // View lookup cache
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ConstraintLayout layoutRoot;
        TextView tvTitle, tvAuthor;
        ImageView ivCover;

        public MyViewHolder(View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.layout_root);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            ivCover = itemView.findViewById(R.id.iv_theme_cover);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}