package com.example.murom;

import static com.example.murom.Firebase.PasswordHashing.hashPassword;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
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
import com.example.murom.Firebase.Database;
import com.example.murom.Firebase.Storage;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
                                checkUserDatabase(user.getUid(), user.getEmail());
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
                Auth.signInWithEmailPassword(email, password, new Auth.SignInWithEmailPasswordCallback() {
                    @Override
                    public void onSignInSuccess() {
                        Log.d("-->", "signInWithEmail:success");
                        navigateToMain();
                    }

                    @Override
                    public void onSignInFailure(Exception e) {
                        Log.w("-->", "signInWithEmail:failure");
                        Toast.makeText(LoginActivity.this, "Authentication failed." + e,
                                Toast.LENGTH_SHORT).show();

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
    private void checkUserDatabase(String userId, String userEmail) {
        DocumentReference userDocRef = db.collection("User").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                createNewUserDocument(userId, userEmail);
            }
            else {
                navigateToMain();
            }
        }).addOnFailureListener(e -> {
            Log.d("-->", "Error reading user data: " + e.getMessage());
        });
    }

    // Create user document
    private void createNewUserDocument(String userId, String userEmail) {
        String username = userEmail.split("@")[0];
        getUniqueUsername(username, new UniqueUsernameCallback() {
            @Override
            public void onUniqueUsernameFound(String uniqueUsername) {
                DocumentReference userDocRef = db.collection("User").document(userId);
                Map<String, String> userDefaultInfo = new HashMap<>();
                userDefaultInfo.put("id", userId);
                userDefaultInfo.put("email", userEmail);
                userDefaultInfo.put("password", "");
                userDefaultInfo.put("username", uniqueUsername);
                userDefaultInfo.put("status","Public");
                String storagePath = "avatar/"+ userEmail;
                Uri defaultImageUri = Uri.parse("android.resource://com.example.murom/" + R.drawable.default_avatar);
                Storage.uploadAvatarAsset(defaultImageUri, storagePath, new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        userDefaultInfo.put("profile_picture", uri.toString());
                        userDocRef.set(userDefaultInfo)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("-->", "Document successfully created!");
                                    navigateToMain();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("-->", "Failed to create new user document: " + e.getMessage());
                                });
                    }
                });

            }
        });
    }

    public void getUniqueUsername(String username, UniqueUsernameCallback callback) {
        checkUsernameAvailability(username, 1, callback);
    }

    private void checkUsernameAvailability(String username, int counter, UniqueUsernameCallback callback) {
        String newUsername = username + counter;
        isUsernameExists(newUsername, exists -> {
            if (!exists) {
                // Username is available, invoke the callback with the unique username
                callback.onUniqueUsernameFound(newUsername);
            } else {
                // Username is not available, recursively check the next username
                checkUsernameAvailability(username, counter + 1, callback);
            }
        });
    }

    public interface UniqueUsernameCallback {
        void onUniqueUsernameFound(String uniqueUsername);
    }

    public interface UsernameCheckListener {
        void onUsernameChecked(boolean exists);
    }
    public void isUsernameExists(String username, UsernameCheckListener listener) {
        db.collection("User")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean exists = !task.getResult().isEmpty();
                        listener.onUsernameChecked(exists);
                    } else {
                        Log.d("-->", "Error checking username: " + task.getException().getMessage());
                        listener.onUsernameChecked(false);
                    }
                });
    }

}