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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.smartcartbuddy.CartActivity;
import com.smartcartbuddy.Common;
import com.smartcartbuddy.MainActivity;
import com.smartcartbuddy.R;
import com.smartcartbuddy.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> shoppingCart;
    private final GrandTotalListener listener;
    private Context context;

    public CartAdapter(Context context,
                       List<CartItem> shoppingCart,
                       GrandTotalListener listener) {
        this.context = context;
        this.shoppingCart = shoppingCart;
        this.listener = listener;

        updateGrandTotal();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_layout, parent, false);
        return new CartViewHolder(view);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem currentItem = shoppingCart.get(position);
        holder.itemName.setText(currentItem.getProductName());
        holder.itemQuantity.setText("Quantity: " + currentItem.getQuantity());
        holder.itemPrice.setText(String.format("$%.2f", currentItem.getPrice()));
        holder.itemsTotal.setText(String.format("$%.2f", currentItem.getPrice() * currentItem.getQuantity()));

        holder.removeButton.setOnClickListener(v -> {
            int currentQuantity = currentItem.getQuantity();
            if (currentQuantity > 1) {
                currentItem.setQuantity(currentQuantity - 1);
                updateQuantity(currentItem);
                notifyItemChanged(position);
            } else {
                shoppingCart.remove(position);
                removeItem(currentItem);
                notifyItemRemoved(position);
            }
        });

        holder.updateButton.setOnClickListener(v -> {
            int newQuantity = currentItem.getQuantity() + 1;
            currentItem.setQuantity(newQuantity);
            updateQuantity(currentItem);
            notifyItemChanged(position);
        });

    }

    @Override
    public int getItemCount() {
        return shoppingCart.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemQuantity;
        TextView itemPrice;
        TextView itemsTotal;
        CardView removeButton;
        CardView updateButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemsTotal = itemView.findViewById(R.id.items_total);
            removeButton = itemView.findViewById(R.id.remove_button);
            updateButton = itemView.findViewById(R.id.update_button);
        }
    }

    public double calculateGrandTotal() {
        double grandTotal = 0.0;
        for (CartItem item : shoppingCart) {
            grandTotal += item.getPrice() * item.getQuantity();
        }
        return grandTotal;
    }

    private void updateGrandTotal() {
        double grandTotal = calculateGrandTotal();
        if (listener != null) {
            listener.onGrandTotalChanged(grandTotal);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateQuantity(CartItem currentItem) {
        if (context != null) {
            Common common = new Common(context.getApplicationContext());
            String userId = common.getUserId();
            Log.d("err", "User ID: " + userId);
            if (context instanceof CartActivity) {
                ((CartActivity) context).updateQuantityInFirebase(userId, currentItem);
                Log.d("err", "updateQuantityInFirebase() called from CartActivity");
            } else {
                Log.e("err", "Context is not an instance of CartActivity");
            }

            notifyDataSetChanged();
            updateGrandTotal();

        } else {
            Log.e("err", "Context is null");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void removeItem(CartItem currentItem) {
        if (context != null) {
            Common common = new Common(context.getApplicationContext());
            String userId = common.getUserId();
            Log.d("err", "User ID: " + userId);
            if (context instanceof CartActivity) {
                ((CartActivity) context).removeItemFromFirebase(userId, currentItem);
                Log.d("err", "removeItemFromFirebase() called from CartActivity");
            } else {
                Log.e("err", "Context is not an instance of CartActivity");
            }

            notifyDataSetChanged();
            updateGrandTotal();

        } else {
            Log.e("err", "Context is null");
        }
    }

    public interface GrandTotalListener {
        void onGrandTotalChanged(double grandTotal);
    }
}
