package com.example.murom;

import static com.example.murom.Firebase.PasswordHashing.hashPassword;

import android.content.Intent;
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

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.murom.Firebase.Auth;
import com.example.murom.Firebase.Database;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.Key;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextEmail, editTextUsername;
    TextInputEditText editTextPassword, editTextConfirmPassword;
    Button registerBtn, toLoginBtn;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final int MIN_PASSWORD_LEN = 6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.register_email);
        editTextPassword = findViewById(R.id.register_password);
        editTextConfirmPassword = findViewById(R.id.register_confirm_password);
        editTextUsername = findViewById(R.id.register_username);
        registerBtn = findViewById(R.id.register_btn);
        toLoginBtn = findViewById(R.id.register_to_login_btn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, confirmpassword, username;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                confirmpassword = editTextConfirmPassword.getText().toString();
                username = editTextUsername.getText().toString();
                //check inputs
                String errorText = validateInputs(email, username, password, confirmpassword);
                if (!errorText.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, errorText, Toast.LENGTH_SHORT).show();
                    return;
                }
                String hashedPassword = hashPassword(password);
                Database.checkUsernameAvailability(username, new Database.checkUsernameCallback() {
                            @Override
                            public void onUsernameAvailable() {
                                Auth.register(email, password, username, new Auth.RegistrationCallback() {
                                    @Override
                                    public void onRegistrationSuccess() {
                                        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                    @Override
                                    public void onRegistrationFailure(String errorMessage) {
                                        Log.d("-->", "Registration failed");
                                    }
                                });
                            }
                            @Override
                            public void onUsernameUnavailable() {
                                Toast.makeText(RegisterActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onUsernameCheckError(String errorMessage) {
                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCheckFailure() {
                                Toast.makeText(RegisterActivity.this, "Check failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        toLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidPassword(String password) {
        return password.length() >= MIN_PASSWORD_LEN;
    }
    private String validateInputs(String email, String username, String password, String confirmpassword) {
        // Check null
        if (TextUtils.isEmpty(email)) {
            return "Enter email";
        }
        if (TextUtils.isEmpty(password)) {
            return "Enter password";
        }
        if (TextUtils.isEmpty(confirmpassword)) {
            return "Enter confirm password";
        }
        if (TextUtils.isEmpty(username)) {
            return "Enter username";
        }
        
        // Check valid
        if (!isValidEmail(email)) {
            return "Enter a valid email address";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters";
        }
        if (!password.equals(confirmpassword)) {
            return "Passwords do not match";
        }
        return "";
    }
}
