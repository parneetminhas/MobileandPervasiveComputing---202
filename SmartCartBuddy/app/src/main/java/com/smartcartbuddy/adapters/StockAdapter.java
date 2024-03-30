package com.smartcartbuddy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.smartcartbuddy.Common;
import com.smartcartbuddy.MainActivity;
import com.smartcartbuddy.R;
import com.smartcartbuddy.models.CartItem;
import com.smartcartbuddy.models.StockItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {
    private List<StockItem> stockItems;
    private Context context;

    public StockAdapter(Context context, List<StockItem> shoppingCart) {
        this.context = context;
        this.stockItems = shoppingCart;
    }

    @NonNull
    @Override
    public StockAdapter.StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item_layout, parent, false);
        return new StockAdapter.StockViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull StockAdapter.StockViewHolder holder, int position) {
        CartItem currentItem = new CartItem(stockItems.get(position).getProductId(),
                stockItems.get(position).getProductName(),
                1,
                stockItems.get(position).getPrice(),
                "");
        holder.itemName.setText(currentItem.getProductName());
        holder.itemPrice.setText(String.format("$%.2f", currentItem.getPrice()));
        holder.addButton.setOnClickListener(v -> addToCart(currentItem));
    }

    @Override
    public int getItemCount() {
        return stockItems.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemPrice;
        CardView addButton;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemPrice = itemView.findViewById(R.id.item_price);
            addButton = itemView.findViewById(R.id.add_button);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addToCart(CartItem cartItem) {
        Map<String, Object> itemMap = new HashMap<>();

        itemMap.put("productId", cartItem.getProductId());
        itemMap.put("productName", cartItem.getProductName());
        itemMap.put("price", cartItem.getPrice());
        itemMap.put("quantity", cartItem.getQuantity());
        Log.d("err", "xxx");

        if (context != null) {
            Common common = new Common(context.getApplicationContext());
            String userId = common.getUserId();
            Log.d("err", "User ID: " + userId);
            if (context instanceof MainActivity) {
                ((MainActivity) context).loadUserCart(userId, itemMap);
                Log.d("err", "loadUserCart() called from MainActivity");
            } else {
                Log.e("err", "Context is not an instance of MainActivity");
            }

            notifyDataSetChanged();
        } else {
            Log.e("err", "Context is null");
        }


    }
}
