package com.smartcartbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");

    }

    public void signUp(View view) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        // Sign up success
                        Toast.makeText(Register.this, "Sign up successful. Log in to proceed.",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this, LogIn.class));
                        finish();
                    } else {
                        // Sign up failed
                        Toast.makeText(Register.this, "Sign up failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void logIn(View view) {
        startActivity(new Intent(Register.this, LogIn.class));
        finish();
    }


}