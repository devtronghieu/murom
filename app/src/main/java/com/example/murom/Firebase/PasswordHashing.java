package com.example.murom.Firebase;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHashing {

    public static String hashPassword(String password) {
        try {
            // Get instance of SHA-256 message digest algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Apply the hashing function to the password
            byte[] hashedBytes = digest.digest(password.getBytes());

            // Encode the hashed bytes as a base64 string
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

