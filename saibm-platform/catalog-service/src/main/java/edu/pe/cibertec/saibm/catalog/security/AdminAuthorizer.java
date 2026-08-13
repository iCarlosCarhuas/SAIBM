package edu.pe.cibertec.saibm.catalog.security;

import jakarta.servlet.http.HttpServletRequest;

public interface AdminAuthorizer {
    void require(HttpServletRequest request);
}
