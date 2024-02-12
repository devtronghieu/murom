package com.example.murom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button devLoginBtn = findViewById(R.id.dev_login_btn);
        devLoginBtn.setOnClickListener(this::handleDevLogin);
        
        Button googleLoginBtn = findViewById(R.id.google_login_btn);
        googleLoginBtn.setOnClickListener(this::handleGoogleLogin);
    }
    
    private void handleGoogleLogin(View view) {

    }

    private void handleDevLogin(View view) {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }
}