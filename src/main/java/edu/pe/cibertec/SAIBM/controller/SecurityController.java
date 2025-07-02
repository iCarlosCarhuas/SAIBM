package edu.pe.cibertec.SAIBM.controller;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import edu.pe.cibertec.SAIBM.service.MembresiaService;
import edu.pe.cibertec.SAIBM.service.PerfilService;
import edu.pe.cibertec.SAIBM.service.RolService;
import edu.pe.cibertec.SAIBM.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/security")
public class SecurityController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MembresiaService membresiaService;

    @Autowired
    private RolService rolService;

    @Autowired
    private PerfilService perfilService;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "security/login";
    }

    @PostMapping("/login")
    public String procesarLogueo(
            @RequestParam String email,
            @RequestParam String contraseña,
            HttpSession session,
            Model model
    ){
        return usuarioService.validarLogin(email, contraseña)
                .map(usuario -> {
                    session.setAttribute("usuarioLogueado", usuario); //SeguridadInterceptor
                    session.setAttribute("usuario", usuario);
                    session.setAttribute("accesos", usuario.getPerfil().getAccesos());
                    model.addAttribute("usuario", usuario);
                    return "redirect:/biblioteca/listar_libros";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Usuario o contraseña incorrectos");
                    return "security/login";
                });
    }

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new UsuarioEntity());
        model.addAttribute("membresias", membresiaService.conseguirTodo());
        return "security/register";
    }

    @PostMapping("/register")
    public String crear(@ModelAttribute UsuarioEntity usuario, Model model) {
        if (usuarioService.encontrarPorCorreo(usuario.getCorreo()).isPresent()) {
            model.addAttribute("error", "El correo ya está registrado");
            return "security/register";
        }

        // Asigna un perfil por defecto antes de crear
        usuario.setPerfil(perfilService.conseguirPorID(2)); // 2: Cliente
        usuario.setRol(rolService.conseguirPorID(2)); // 2: Reserva Libros
        usuarioService.crear(usuario);
        model.addAttribute("mensaje", "Usuario registrado correctamente");
        return "redirect:/security/login";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/security/login";
    }
}
