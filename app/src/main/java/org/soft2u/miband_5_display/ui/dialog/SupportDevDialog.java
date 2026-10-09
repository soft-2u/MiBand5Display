package org.soft2u.miband_5_display.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;

import org.soft2u.miband_5_display.R;
import org.soft2u.miband_5_display.adapter.ProductsSupportAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SupportDevDialog extends DialogFragment implements ProductsSupportAdapter.ItemClickListener {

    private Context mContext;
    private FragmentActivity mActivity;
    private final String TAG = "SupportDevDialog";
    private RecyclerView lsProductsSupport;
    private TextView tvBillingStatus;
    private BillingClient billingClient;

    private List<ProductDetails> productDetailsList;
    private ProductsSupportAdapter adapter;

    public SupportDevDialog() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FragmentActivity){
            mContext = context;
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.fragment_dialog_support_dev, container, false);
        lsProductsSupport = root.findViewById(R.id.lv_products_support);
        tvBillingStatus = root.findViewById(R.id.tv_billing_status);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false); // important -> empty list
        lsProductsSupport.setLayoutManager(layoutManager); // important -> empty list

        showBillingStatus(R.string.label_loading_products);
        setupBilling();

        (root.findViewById(R.id.rl_bkg_trans)).setOnClickListener((View v) ->
                dismiss()
        );

        return root;
    }

    private void showBillingStatus(int messageRes, Object... args) {
        if (tvBillingStatus == null || lsProductsSupport == null || !isAdded()) return;
        tvBillingStatus.setText(getString(messageRes, args));
        tvBillingStatus.setVisibility(View.VISIBLE);
        lsProductsSupport.setVisibility(View.GONE);
    }

    private void hideBillingStatus() {
        if (tvBillingStatus == null || lsProductsSupport == null || !isAdded()) return;
        tvBillingStatus.setVisibility(View.GONE);
        lsProductsSupport.setVisibility(View.VISIBLE);
    }

    private void setupBilling() {
        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            // onPurchasesUpdated
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                if(SupportDevDialog.this.isVisible() && mContext != null)
                    Toast.makeText(mContext, R.string.toast_cancel_support, Toast.LENGTH_LONG).show();
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                Toast.makeText(mContext, R.string.toast_already_owned_support, Toast.LENGTH_LONG).show();
            } else {
                // Handle any other error codes.
                Toast.makeText(mContext, R.string.toast_error_there_was_a_problem, Toast.LENGTH_LONG).show();
                Log.w(TAG, "Purchase fail: " + billingResult.getResponseCode());
            }
        };

        billingClient = BillingClient.newBuilder(mActivity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                Log.i(TAG, "onBillingSetupFinished: code=" + billingResult.getResponseCode()
                        + " msg=" + billingResult.getDebugMessage());
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    loadListProducts();
                } else if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                    showBillingStatus(R.string.msg_billing_setup_failed, billingResult.getResponseCode());
                    Toast.makeText(mContext, R.string.toast_google_play_app_not_found, Toast.LENGTH_SHORT).show();
                } else {
                    showBillingStatus(R.string.msg_billing_setup_failed, billingResult.getResponseCode());
                    Toast.makeText(mContext, R.string.toast_error_there_was_a_problem, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Query billing fail: " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.i(TAG, "onBillingServiceDisconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void loadListProducts() {
        if(billingClient.isReady()) {
            List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("support_lemon_tea")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("support_coffee")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("support_fresh_orange_juice")
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());

            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build();

            billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsResult) -> {
                Log.i(TAG, "queryProductDetailsAsync: code=" + billingResult.getResponseCode()
                        + " msg=" + billingResult.getDebugMessage());
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    this.productDetailsList = productDetailsResult.getProductDetailsList();
                    // PBL 8+: products that could not be fetched are reported separately
                    Log.i(TAG, "Unfetched products: " + productDetailsResult.getUnfetchedProductList());
                    if (this.productDetailsList != null && !this.productDetailsList.isEmpty()) {
                        Log.i(TAG, String.valueOf(this.productDetailsList.size()));
                        hideBillingStatus();
                        adapter = new ProductsSupportAdapter(this.productDetailsList);
                        lsProductsSupport.setAdapter(adapter);
                        adapter.setClickListener(this);
                    } else {
                        showBillingStatus(R.string.msg_billing_empty_products);
                    }
                } else {
                    showBillingStatus(R.string.msg_billing_query_failed, billingResult.getResponseCode());
                    Log.w(TAG, "Load products list fail: " + billingResult.getResponseCode());
                }
            });
        }
    }

    void handlePurchase(Purchase purchase) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        Log.d(TAG, "handle purchase " + purchase);

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation.
                Toast.makeText(mContext, R.string.toast_support_dev_success, Toast.LENGTH_SHORT).show();
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (productDetailsList == null || productDetailsList.size() <= position) return;
        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetailsList.get(position))
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();
        billingClient.launchBillingFlow(mActivity, billingFlowParams);
        Log.d(TAG, "item clicked");
    }
}
