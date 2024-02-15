package com.example.murom.Firebase;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

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
}
