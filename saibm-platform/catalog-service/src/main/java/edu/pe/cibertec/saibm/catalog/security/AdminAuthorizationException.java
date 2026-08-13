package edu.pe.cibertec.saibm.catalog.security;

public class AdminAuthorizationException extends RuntimeException {
    public AdminAuthorizationException() {
        super("Catalog write authorization failed");
    }
}
