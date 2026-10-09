package org.soft2u.miband_5_display.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.ProductDetails;

import org.soft2u.miband_5_display.R;

import java.util.List;

public class ProductsSupportAdapter extends RecyclerView.Adapter<ProductsSupportAdapter.MyViewHolder> implements View.OnClickListener {
    private final List<ProductDetails> productDetailsList;
    private ItemClickListener mClickListener;
    private final String TAG = "ProductsSupportAdapter";

    // data is passed into the constructor
    public ProductsSupportAdapter(List<ProductDetails> productDetailsList) {
        this.productDetailsList = productDetailsList;
        Log.i(TAG, String.valueOf(this.productDetailsList.size()));
    }

    @Override
    public void onClick(View view) {

    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_products_support, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder");
        ProductDetails details = productDetailsList.get(position);
        holder.tvProductName.setText(details.getTitle().split("\\(")[0]);
        holder.tvProductDesc.setText(details.getDescription());
        // PBL 8+: one-time products can carry multiple offers; show the first one
        if (details.getOneTimePurchaseOfferDetailsList() != null
                && !details.getOneTimePurchaseOfferDetailsList().isEmpty()) {
            holder.tvProductPrice.setText(
                    details.getOneTimePurchaseOfferDetailsList().get(0).getFormattedPrice());
        }
    }

    @Override
    public int getItemCount() {
        return productDetailsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvProductName, tvProductDesc, tvProductPrice;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductDesc = itemView.findViewById(R.id.tv_product_desc);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            itemView.setOnClickListener(this);
            Log.i(TAG, "MyViewHolder");
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
}
