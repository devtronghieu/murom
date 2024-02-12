package com.example.murom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

public class LoginActivity extends AppCompatActivity {
    CredentialManager credentialManager;
    GetGoogleIdOption googleIdOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup credentials
        credentialManager = CredentialManager.create(this);
        googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId("CLIENT_ID")
                .build();

        // Add button functions
        Button devLoginBtn = findViewById(R.id.dev_login_btn);
        devLoginBtn.setOnClickListener(this::handleDevLogin);
        
        Button googleLoginBtn = findViewById(R.id.google_login_btn);
        googleLoginBtn.setOnClickListener(this::handleGoogleLogin);
    }
    
    private void handleGoogleLogin(View view) {
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Log.d("-->", "Hello I'm here");

        credentialManager.getCredentialAsync(
            this,
            request,
            null,
            null,
            new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    handleSignIn(result);
                }

                @Override
                public void onError(GetCredentialException e) {
                    Log.d("-->", "On error " + e.toString());
                }
            }
        );
    }

    public void handleSignIn(GetCredentialResponse result) {
        // Handle the successfully returned credential.
        Credential credential = result.getCredential();

        Log.d("-->", "Welcome to handleSignIn");

        if (credential instanceof PublicKeyCredential) {
            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
            // Share responseJson i.e. a GetCredentialResponse on your server to validate and authenticate
        } else if (credential instanceof PasswordCredential) {
            String username = ((PasswordCredential) credential).getId();
            String password = ((PasswordCredential) credential).getPassword();
            // Use id and password to send to your server to validate and authenticate
        } else if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                // Use googleIdTokenCredential and extract id to validate and
                // authenticate on your server
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
            } else {
                // Catch any unrecognized custom credential type here.
                Log.d("-->", "Unexpected type of credential");
            }
        } else {
            // Catch any unrecognized credential type here.
            Log.d("-->", "Unexpected type of credential");
        }
    }

    private void handleDevLogin(View view) {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }
}