package edu.pe.cibertec.saibm.catalog.migration;

public class BackfillValidationException extends RuntimeException {
    public BackfillValidationException(String message) {
        super(message);
    }
}
