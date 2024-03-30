package com.example.murom.Firebase;

import static com.example.murom.Firebase.PasswordHashing.hashPassword;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.murom.LoginActivity;
import com.example.murom.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class Auth {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface AuthCallback {
        void onSignInSuccess(FirebaseUser user);
        void onSignInFailure();
    }

    public static void signIn(String idToken, AuthCallback callback) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(firebaseCredential).addOnCompleteListener(runnable -> {
            if (runnable.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                callback.onSignInSuccess(user);
            } else {
                callback.onSignInFailure();
            }
        });
    }

    public static void signOut() {
        auth.signOut();
    }

    public static FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    public interface RegistrationCallback {
        void onRegistrationSuccess();
        void onRegistrationFailure(String errorMessage);
    }
    public static void register(String email, String password, String username, RegistrationCallback callback){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String hashedPassword = PasswordHashing.hashPassword(password);
                            Database.addUserToFirestore(user.getUid(), email, hashedPassword, username, callback);
                        }
                    } else {
                        Log.w("-->", "createUserWithEmail:failure", task.getException());
                        callback.onRegistrationFailure("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    public interface  SignInWithEmailPasswordCallback {
        void onSignInSuccess();
        void onSignInFailure(Exception e);
    }
    public static void signInWithEmailPassword(String email, String password, SignInWithEmailPasswordCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSignInSuccess();
                        } else {
                            callback.onSignInFailure(task.getException());
                        }
                });
    }
}
