package com.example.murom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.murom.Firebase.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId("528776764567-lk7vepg2lbpo4cg3h67h6u8svn9aetd5.apps.googleusercontent.com")
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        increaseCountInFirestore();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    try {
                        SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        String idToken = credential.getGoogleIdToken();

                        Auth.signIn(idToken, new Auth.AuthCallback() {
                            @Override
                            public void onSignInSuccess(FirebaseUser user) {
                                Log.d("-->", "Welcome " + user.getEmail());
                                navigateToMain();
                            }

                            @Override
                            public  void onSignInFailure() {
                                Log.d("-->", "Failed to signInWithCredential");
                            }
                        });
                    } catch (ApiException e) {
                        Log.d("-->", "ApiException " + e.toString());
                    }
                }
        );

        Button googleLoginBtn = findViewById(R.id.google_login_btn);
        googleLoginBtn.setOnClickListener(this::handleGoogleLogin);

        Button devLoginBtn = findViewById(R.id.dev_login_btn);
        devLoginBtn.setOnClickListener(this::handleDevLogin);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("-->", "Welcome back, " + currentUser.getEmail());
            navigateToMain();
        } else {
            Log.d("-->", "Not logged in");
        }
    }

    private void handleDevLogin(View view) {
        navigateToMain();
    }

    private void handleGoogleLogin(View view) {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(LoginActivity.this, result -> {
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder
                            (result.getPendingIntent().getIntentSender()).build();
                    activityResultLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(LoginActivity.this, e -> {
                    Log.d("-->", "onFailure " + Objects.requireNonNull(e.getLocalizedMessage()));
                });
    }

    private void navigateToMain() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void increaseCountInFirestore() {
        db.collection("test")
                .document("test_id")
                .update("count", FieldValue.increment(1))
                .addOnCompleteListener(runnable -> {
                    Log.d("-->", "Increase count successfully");
                })
                .addOnFailureListener(e -> {
                    Log.d("-->", "Increase count failed: " + e);
                });
    }
}