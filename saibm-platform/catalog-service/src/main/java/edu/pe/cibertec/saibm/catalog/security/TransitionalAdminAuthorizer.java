package edu.pe.cibertec.saibm.catalog.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Transitional internal-header adapter; replace with ADMIN JWT authorization. */
@Component
public class TransitionalAdminAuthorizer implements AdminAuthorizer {
    private final String expected;

    public TransitionalAdminAuthorizer(@Value("${SAIBM_CATALOG_INTERNAL_ADMIN_SECRET:}") String expected) {
        this.expected = expected;
    }

    @Override
    public void require(HttpServletRequest request) {
        String supplied = request.getHeader("X-SAIBM-Internal-Admin");
        if (expected.isBlank() || supplied == null || !constantTimeEquals(expected, supplied)) {
            throw new AdminAuthorizationException();
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
