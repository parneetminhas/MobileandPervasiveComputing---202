package com.smartcartbuddy;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smartcartbuddy.adapters.StockAdapter;
import com.smartcartbuddy.databinding.ActivityMainBinding;
import com.smartcartbuddy.models.StockItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private Button viewCartButton;

    private ActivityMainBinding binding;
    private DatabaseReference mDatabase;

    private List<StockItem> stockItemList;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        stockItemList = new ArrayList<>();

        initializeFireBaseDB();

        Toolbar toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.stockRecyclerView);
        viewCartButton = findViewById(R.id.viewCartButton);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.back));

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StockAdapter(MainActivity.this, stockItemList);
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching items...");
        progressDialog.setCancelable(false);

        readDataIntoList();

        viewCartButton.setOnClickListener(v -> viewCart());
    }

    public void readDataIntoList() {
        progressDialog.show();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stockItemList.clear();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    StockItem stockItem = childSnapshot.getValue(StockItem.class);
                    stockItemList.add(stockItem);
                    assert stockItem != null;
                }
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                System.err.println("Error reading data from Firebase: " + databaseError.getMessage());
            }
        });
    }

    public void initializeFireBaseDB() {
        mDatabase = FirebaseDatabase.getInstance().getReference("stockItems");
        List<StockItem> stockItems = List.of(
                new StockItem(0, "Tomatoes", 20.0, "Vegetable"),
                new StockItem(1, "Broccoli", 10.0, "Vegetable"),
                new StockItem(2, "Lettuce", 5.00, "Vegetable"),
                new StockItem(3, "Capsicum", 30.0, "Vegetable"),
                new StockItem(4, "Cucumber", 100.00, "Vegetable")
        );

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Push each stock item to Firebase if the node doesn't exist
                    for (StockItem item : stockItems) {
                        Map<String, Object> itemMap = new HashMap<>();

                        itemMap.put("productId", item.getProductId());
                        itemMap.put("productName", item.getProductName());
                        itemMap.put("price", item.getPrice());

                        addStockItems(itemMap);

                    }
                } else {
                    System.out.println("Data already exists in Firebase.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error reading data from Firebase: " + databaseError.getMessage());
            }
        });

    }

    public void addStockItems(Map<String, Object> itemMap) {
        mDatabase.push().setValue(itemMap, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                // Error handling
                System.err.println("Data could not be saved: " + databaseError.getMessage());
            } else {
                // Success
                System.out.println("Data saved successfully.");
            }
        });
    }

    public void loadUserCart(String userId, Map<String, Object> itemMap) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference().child("carts").child(userId);
        String productId = itemMap.get("productId").toString();

        DatabaseReference itemRef = cartRef.child(productId);

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("err", "Item already exists in user's cart");
                    int currentQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                    int newQuantity = currentQuantity + Integer.parseInt(itemMap.get("quantity").toString());
                    itemRef.child("quantity").setValue(newQuantity);
                    Toast.makeText(MainActivity.this, "Item Updated.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.d("err", "Adding new item to user's cart");
                    itemRef.setValue(itemMap)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MainActivity.this, "Added to cart.",
                                        Toast.LENGTH_LONG).show();
                                Log.d("err", "Item added to user's cart successfully");
                            })
                            .addOnFailureListener(e ->
                            {
                                Toast.makeText(MainActivity.this, "Error adding to cart.",
                                        Toast.LENGTH_LONG).show();
                                Log.e("err", "Failed to add item to user's cart", e);
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("err", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public void viewCart() {
        startActivity(new Intent(MainActivity.this, CartActivity.class));
    }


}