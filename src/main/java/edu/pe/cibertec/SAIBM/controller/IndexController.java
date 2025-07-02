package edu.pe.cibertec.SAIBM.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping({"/", "/index"})
    public String index(HttpSession session) {
        if (session.getAttribute("usuarioLogueado") == null) {
            return "index";
        } else {
            return "redirect:/biblioteca/listar_libros";
        }
    }
}
