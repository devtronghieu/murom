package com.example.murom;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.murom.Firebase.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
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
    EditText editTextEmail;
    TextInputEditText editTextPassword;
    Button loginBtn, toRegisterBtn;

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
                                Log.d("-->", "Your UID: " + user.getUid());
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

        TextView appName = findViewById(R.id.app_name);
        int startColor = ContextCompat.getColor(this, R.color.primary_100);
        int endColor = ContextCompat.getColor(this, R.color.secondary_100);
        Shader textShader = new LinearGradient(0, 0, 0, appName.getTextSize(), startColor, endColor, Shader.TileMode.CLAMP);
        appName.getPaint().setShader(textShader);
        Button googleLoginBtn = findViewById(R.id.google_login_btn);
        googleLoginBtn.setOnClickListener(this::handleGoogleLogin);

        editTextEmail = findViewById(R.id.login_username);
        editTextPassword = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);
        toRegisterBtn = findViewById(R.id.login_to_register_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("-->", "signInWithEmail:success");
                                    navigateToMain();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("-->", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });
        toRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d("-->", "Welcome back, " + currentUser.getEmail());
            Log.d("-->", "Your UID: " + currentUser.getUid());
            navigateToMain();
        } else {
            Log.d("-->", "Not logged in");
        }
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