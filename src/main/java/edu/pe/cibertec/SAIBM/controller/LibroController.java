package edu.pe.cibertec.SAIBM.controller;

import edu.pe.cibertec.SAIBM.entity.LibroEntity;
import edu.pe.cibertec.SAIBM.entity.ReservaEntity;
import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import edu.pe.cibertec.SAIBM.repository.LibroRepository;
import edu.pe.cibertec.SAIBM.service.LibroService;
import edu.pe.cibertec.SAIBM.service.ReservaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/biblioteca")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private LibroRepository libroRepository;

    @Value("${recaptcha.sitekey}")
    private String recaptchaSiteKey;

    @GetMapping("/listar_libros")
    public String listarLibros(@RequestParam(name = "nombre", required = false) String nombre, Model model, HttpSession session) {
        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/security/login";
        }

        // Si se buscó un nombre de libro, filtra
        if (nombre != null && !nombre.isEmpty()) {
            model.addAttribute("libros", libroService.buscarPorNombre(nombre));
        } else {
            model.addAttribute("libros", libroService.conseguirTodo());
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        model.addAttribute("reservasConfirmadas", reservaService.conseguirIdsReservadosPorUsuario(usuario.getId()));

        List<ReservaEntity> carrito = (List<ReservaEntity>) session.getAttribute("carritoReservas");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carritoReservas", carrito);
        }
        model.addAttribute("carritoReservas", carrito);

        int limite = switch (usuario.getMembresia().getNombreMembresia().toLowerCase()) {
            case "super premium plus" -> 8;
            case "super premium" -> 4;
            default -> 2;
        };
        model.addAttribute("limiteReservas", limite);

        List<Integer> librosReservados = carrito.stream()
                .map(r -> r.getLibro().getId())
                .toList();
        model.addAttribute("librosReservados", librosReservados);

        if (!model.containsAttribute("reservasConfirmadas")) {
            model.addAttribute("reservasConfirmadas", new ArrayList<Integer>());
        }

        int countReservas = reservaService.contarReservasPorUsuario(usuario.getId());
        model.addAttribute("countReservas", countReservas);

        return "biblioteca/listar_libros";
    }


    @GetMapping("/nuevo")
    public String nuevoLibro(Model model) {
        model.addAttribute("libro", new LibroEntity());
        return "mantenimiento/editar_libro";
    }

    @GetMapping("/editar/{id}")
    public String editarLibro(@PathVariable Integer id, Model model) {
        LibroEntity libro = libroService.conseguirPorID(id);
        model.addAttribute("libro", libro);
        return "mantenimiento/editar_libro";
    }

    @PostMapping("/guardar")
    public String guardarLibro(@ModelAttribute LibroEntity libro) {
        libroService.crear(libro);
        return "redirect:/biblioteca/listar_libros";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarLibro(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            libroService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Libro eliminado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/biblioteca/listar_libros";
    }

}
