package edu.pe.cibertec.SAIBM.controller;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

@ControllerAdvice
public class SeguridadInterceptor {

    @ModelAttribute
    public void verificarSesion(HttpSession session, HttpServletResponse response) throws IOException {
        String url = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI();
        UsuarioEntity usuarioLogueado = (UsuarioEntity) session.getAttribute("usuarioLogueado");
        if (session.getAttribute("usuarioLogueado") == null &&
                !url.contains("login") &&
                !url.contains("register") &&
                !url.equals("/") &&
                !url.equals("/index")) {
            response.sendRedirect("/security/login");
        }
        if (url.startsWith("/usuario") &&
                (usuarioLogueado == null ||
                        !usuarioLogueado.getRol().getNombreRol().equals("ROLE_ADMIN"))) {
            response.sendRedirect("/biblioteca/listar_libros");
        }
    }
}
