package edu.pe.cibertec.SAIBM.controller;

import edu.pe.cibertec.SAIBM.entity.LibroEntity;
import edu.pe.cibertec.SAIBM.entity.ReservaEntity;
import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import edu.pe.cibertec.SAIBM.service.LibroService;
import edu.pe.cibertec.SAIBM.service.ReCaptchaService;
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
@RequestMapping("/reserva")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private LibroService libroService;

    @Autowired
    private ReCaptchaService reCaptchaService;


    @ModelAttribute("carritoReservas")
    public List<ReservaEntity> carritoReservas(HttpSession session) {
        List<ReservaEntity> carrito = (List<ReservaEntity>) session.getAttribute("carritoReservas");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carritoReservas", carrito);
        }
        return carrito;
    }

    @PostMapping("/agregar")
    public String agregarReserva(@RequestParam("libroId") Integer libroId,
                                 @ModelAttribute("carritoReservas") List<ReservaEntity> carrito,
                                 RedirectAttributes redirectAttributes) {

        LibroEntity libro = libroService.conseguirPorID(libroId);

        boolean yaReservado = carrito.stream()
                .anyMatch(r -> r.getLibro().getId().equals(libroId));

        if (yaReservado) {
            redirectAttributes.addFlashAttribute("error", "Este libro ya está en tu carrito de reservas.");
        } else {
            ReservaEntity reserva = new ReservaEntity();
            reserva.setLibro(libro);
            carrito.add(reserva);
        }

        return "redirect:/biblioteca/listar_libros";
    }

    @PostMapping("/cancelar")
    public String cancelarReserva(@RequestParam("libroId") Integer libroId,
                                  @ModelAttribute("carritoReservas") List<ReservaEntity> carrito) {

        carrito.removeIf(r -> r.getLibro().getId().equals(libroId));
        return "redirect:/biblioteca/listar_libros";
    }

    @PostMapping("/confirmar")
    public String confirmarReservas(@ModelAttribute("carritoReservas") List<ReservaEntity> carrito,
                                    @RequestParam("g-recaptcha-response") String recaptchaResponse,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes, Model model) {
    try {
        if (!reCaptchaService.validateCaptcha(recaptchaResponse)) {
            redirectAttributes.addFlashAttribute("error", "ReCAPTCHA inválido. Inténtalo de nuevo.");
            return "redirect:/biblioteca/listar_libros";
        }

        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuario");
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para confirmar reservas.");
            return "redirect:/security/login";
        }

        List<Integer> reservasConfirmadas = new ArrayList<>();
        for (ReservaEntity reserva : carrito) {
            reserva.setUsuario(usuario);
            reservaService.confirmarReserva(reserva);
            reservasConfirmadas.add(reserva.getLibro().getId());
        }

        carrito.clear();
        redirectAttributes.addFlashAttribute("reservasConfirmadas", reservasConfirmadas);

        List<Integer> librosReservadosPorUsuario = reservaService.conseguirIdsReservadosPorUsuario(usuario.getId());
        redirectAttributes.addFlashAttribute("librosReservadosPorUsuario", librosReservadosPorUsuario);

        return "redirect:/reserva/mis_reservas";
    } catch (RuntimeException e) {
        model.addAttribute("error", e.getMessage());
        return "biblioteca/listar_libros";
    }
    }



    @GetMapping("/mis_reservas")
    public String misReservas(Model model, HttpSession session) {
        UsuarioEntity usuario = (UsuarioEntity) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/security/login";
        }

        List<ReservaEntity> reservas = reservaService.conseguirPorUsuario(usuario.getId());
        model.addAttribute("reservas", reservas);
        return "biblioteca/listar_reservas";
    }



}
