package edu.pe.cibertec.saibm.catalog.web;

import edu.pe.cibertec.saibm.catalog.book.BookNotFoundException;
import edu.pe.cibertec.saibm.catalog.book.InvalidRequestException;
import edu.pe.cibertec.saibm.catalog.security.AdminAuthorizationException;
import edu.pe.cibertec.saibm.catalog.inventory.InventoryConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CatalogExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setType(java.net.URI.create("urn:saibm:catalog:400"));
        problem.setTitle("Invalid request");
        problem.setInstance(java.net.URI.create(request.getRequestURI()));
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (a, b) -> a)));
        return problem;
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail invalid(InvalidRequestException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage(), request);
    }

    @ExceptionHandler(AdminAuthorizationException.class)
    ProblemDetail forbidden(AdminAuthorizationException exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", exception.getMessage(), request);
    }

    @ExceptionHandler(BookNotFoundException.class)
    ProblemDetail missing(BookNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Book not found", exception.getMessage(), request);
    }

    @ExceptionHandler(InventoryConflictException.class)
    ProblemDetail conflict(InventoryConflictException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Inventory conflict", exception.getMessage(), request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(java.net.URI.create("urn:saibm:catalog:" + status.value()));
        problem.setTitle(title);
        problem.setInstance(java.net.URI.create(request.getRequestURI()));
        return problem;
    }
}
