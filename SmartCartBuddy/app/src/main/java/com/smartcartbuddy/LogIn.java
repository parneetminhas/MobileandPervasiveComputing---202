package com.smartcartbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText editTextEmail, editTextPassword;
    private Button signInButton;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        signInButton = findViewById(R.id.buttonSignIn);
        sharedPreferences = getSharedPreferences("user_pref", Context.MODE_PRIVATE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");

        signInButton.setOnClickListener(v -> signInUser());
    }

    private void signInUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                         FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            saveUserId(userId);
                            startActivity(new Intent(LogIn.this, MainActivity.class));
                            finish();
                        }
                    } else {
                         Toast.makeText(LogIn.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void register(View view) {
        startActivity(new Intent(LogIn.this, Register.class));
        finish();
    }

    private void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", userId);
        editor.apply();
    }
}