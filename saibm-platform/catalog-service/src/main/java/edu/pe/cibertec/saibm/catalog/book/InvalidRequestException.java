package edu.pe.cibertec.saibm.catalog.book;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
