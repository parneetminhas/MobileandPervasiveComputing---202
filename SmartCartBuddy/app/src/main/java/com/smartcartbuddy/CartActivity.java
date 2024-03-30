package com.smartcartbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smartcartbuddy.adapters.CartAdapter;
import com.smartcartbuddy.models.CartItem;
import com.smartcartbuddy.models.StockItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CartActivity extends AppCompatActivity implements CartAdapter.GrandTotalListener {
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;

    private TextView emptyView;
    private TextView items_total;

    private List<CartItem> addedToCart;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        addedToCart = new ArrayList<>();

        initializeFireBaseDB();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.cartRecyclerView);
        emptyView = findViewById(R.id.empty_view);
        items_total = findViewById(R.id.items_total);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.back));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching cart items...");
        progressDialog.setCancelable(false);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CartAdapter(CartActivity.this, addedToCart,this);
        recyclerView.setAdapter(adapter);

        readDataIntoList();


    }

    private void updateEmptyViewVisibility() {
        if (addedToCart == null || addedToCart.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back arrow click
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initializeFireBaseDB() {
        Common common = new Common(getApplicationContext());
        String userId = common.getUserId();
        if (userId != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference("carts").child(userId);
        } else {
            Log.e("Error", "User ID is null");
        }
    }

    public void readDataIntoList() {
        progressDialog.show();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                addedToCart.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    CartItem cartItem = childSnapshot.getValue(CartItem.class);
                    addedToCart.add(cartItem);
                }

                adapter.notifyDataSetChanged();
                updateEmptyViewVisibility();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                System.err.println("Error reading data from Firebase: " + databaseError.getMessage());
            }
        });
    }

    public void updateQuantityInFirebase(String userId, CartItem currentItem) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
        DatabaseReference itemRef = cartRef.child(String.valueOf(currentItem.getProductId()));
        itemRef.child("quantity").setValue(currentItem.getQuantity())
                .addOnSuccessListener(aVoid -> {
                     Log.d("Firebase", "Quantity updated successfully");
                })
                .addOnFailureListener(e -> {
                     Log.e("Firebase", "Failed to update quantity", e);
                });
    }

    public void removeItemFromFirebase(String userId, CartItem currentItem) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId);
        DatabaseReference itemRef = cartRef.child(String.valueOf(currentItem.getProductId()));
        itemRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Quantity updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to update quantity", e);
                });
    }


    @Override
    public void onGrandTotalChanged(double grandTotal) {
        items_total.setText(String.format(Locale.getDefault(), "$%.2f", grandTotal));
    }
}