package edu.pe.cibertec.saibm.catalog.inventory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

@Component
public class RequestHasher {
    public String hold(int bookId, int quantity, String expiresAt) {
        return sha256(bookId + "|" + quantity + "|" + expiresAt);
    }

    public String transition(int bookId, String operation) {
        return sha256(bookId + "|" + operation);
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
