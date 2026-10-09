package org.soft2u.miband_5_display.adapter;

import android.content.Context;
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
import org.soft2u.miband_5_display.model.IconModel;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private List<IconModel> icons;
    private ItemClickListener mClickListener;
    private Context mContext;

    private final int VIEW_ITEM = 1;

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    // data is passed into the constructor
    public IconAdapter(Context context, List<IconModel> icons, RecyclerView recyclerView) {
        this.mContext = context;
        this.icons = icons;
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
        return icons.get(position) == null ? VIEW_PROG : VIEW_ITEM;
    }

    // inflates the row layout from xml when needed

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_icon, parent, false);

            vh = new IconViewHolder(v);
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
        return icons.size();
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
    IconModel getItem(int id) {
        return icons.get(id);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof IconViewHolder) {
            // Get the data item for this position
            IconModel theme = icons.get(position);
            // Check if an existing view is being reused, otherwise inflate the view

            ((IconViewHolder) holder).tvJson.setText(theme.getJson());
            ((IconViewHolder) holder).tvDownload.setText(String.valueOf(theme.getDownload()));
            ((IconViewHolder) holder).tvView.setText(String.valueOf(theme.getView()));
            ((IconViewHolder) holder).tvSize.setText(String.valueOf((int) Math.ceil(theme.getSize())));
            ((IconViewHolder) holder).tvSize.append("KB");
            Glide.with(mContext)
                    .load(theme.getImgUrl())
                    .placeholder(R.drawable.ic_blank_photo)
//                .apply(RequestOptions.circleCropTransform())
                    .into(((IconViewHolder) holder).ivCover);

            if(!theme.getUpdate().equals("null"))
                ((IconViewHolder) holder).ivUpdateNotify.setVisibility(View.VISIBLE);
            else
                ((IconViewHolder) holder).ivUpdateNotify.setVisibility(View.GONE);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    // View lookup cache
    public class IconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvJson, tvDownload, tvView, tvSize;
        ImageView ivCover, ivUpdateNotify;

        IconViewHolder(View itemView) {
            super(itemView);
            tvDownload = itemView.findViewById(R.id.tv_icon_downloaded);
            tvView = itemView.findViewById(R.id.tv_icon_viewed);
            tvSize = itemView.findViewById(R.id.tv_icon_size);
            ivCover = itemView.findViewById(R.id.iv_icon_cover);
            ivUpdateNotify = itemView.findViewById(R.id.iv_icon_update_notify);
            tvJson = itemView.findViewById(R.id.tv_icon_json);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        ProgressViewHolder(View itemview) {
            super(itemview);
            progressBar = itemview.findViewById(R.id.progressBar1);
        }
    }
}