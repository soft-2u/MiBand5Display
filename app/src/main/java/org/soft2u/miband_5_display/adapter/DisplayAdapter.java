package org.soft2u.miband_5_display.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.soft2u.miband_5_display.OnLoadMoreListener;
import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.model.ThemeModel;

import java.util.List;

public class DisplayAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener {
    private List<ThemeModel> themes;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    private Context mContext;

    private final int VIEW_ITEM = 1;

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    // data is passed into the constructor
    public DisplayAdapter(Context context, List<ThemeModel> themes, RecyclerView recyclerView) {
        this.mContext = context;
        this.themes = themes;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

//                Log.d("DisplayNewFragment", "totalItemCount: " + String.valueOf(totalItemCount));
//                Log.d("DisplayNewFragment", "lastVisibleItem: " + String.valueOf(lastVisibleItem));
//                Log.d("DisplayNewFragment", "visibleThreshold: " + String.valueOf(visibleThreshold));
//                Log.d("DisplayNewFragment", "result: " + loading);
                if(totalItemCount >= 12) {
                    if (!loading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                        loading = true;
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        int VIEW_PROG = 0;
        return themes.get(position) == null ? VIEW_PROG : VIEW_ITEM;
    }

    // inflates the row layout from xml when needed

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_display, parent, false);

            vh = new ThemeViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progressbar, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return themes.size();
    }

//    private int lastPosition = -1;


    @Override
    public boolean onLongClick(View view) {
        Log.d("aaa", "OK 2");
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // parent activity will implement this method to respond to click events
    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // allows clicks events to be caught
    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // convenience method for getting data at click position
    ThemeModel getItem(int id) {
        return themes.get(id);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThemeViewHolder) {
            // Get the data item for this position
            ThemeModel theme = themes.get(position);
            // Check if an existing view is being reused, otherwise inflate the view

            ((ThemeViewHolder) holder).tvJson.setText(theme.getJson());
            ((ThemeViewHolder) holder).tvDownload.setText(String.valueOf(theme.getDownload()));
            ((ThemeViewHolder) holder).tvView.setText(String.valueOf(theme.getView()));
            ((ThemeViewHolder) holder).tvLangAmount.setText(String.valueOf(theme.getLangAmount()));
            ((ThemeViewHolder) holder).tvSize.setText(String.valueOf((int) Math.ceil(theme.getSize())));
            ((ThemeViewHolder) holder).tvSize.append("KB");
            Glide.with(mContext)
                    .load(theme.getImgUrl())
                    .placeholder(R.drawable.ic_blank_photo)
//                .apply(RequestOptions.circleCropTransform())
                    .into(((ThemeViewHolder) holder).ivCover);

            if(!theme.getUpdate().equals("null"))
                ((ThemeViewHolder) holder).ivUpdateNotify.setVisibility(View.VISIBLE);
            else
                ((ThemeViewHolder) holder).ivUpdateNotify.setVisibility(View.GONE);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    // View lookup cache
    public class ThemeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvJson, tvDownload, tvView, tvSize, tvLangAmount;
        ImageView ivCover, ivUpdateNotify;

        ThemeViewHolder(View itemView) {
            super(itemView);
            tvDownload = itemView.findViewById(R.id.tv_theme_downloaded);
            tvView = itemView.findViewById(R.id.tv_theme_viewed);
            tvLangAmount = itemView.findViewById(R.id.tv_theme_language_amount);
            tvSize = itemView.findViewById(R.id.tv_theme_size);
            ivCover = itemView.findViewById(R.id.iv_theme_cover);
            ivUpdateNotify = itemView.findViewById(R.id.iv_theme_update_notify);
            tvJson = itemView.findViewById(R.id.tv_theme_json);
            itemView.setOnClickListener(this);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if(mLongClickListener != null) mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar1);
        }
    }
}