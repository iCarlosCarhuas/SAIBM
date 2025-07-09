
package edu.pe.cibertec.SAIBM.controller;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import edu.pe.cibertec.SAIBM.service.MembresiaService;
import edu.pe.cibertec.SAIBM.service.PerfilService;
import edu.pe.cibertec.SAIBM.service.RolService;
import edu.pe.cibertec.SAIBM.service.UsuarioService;
import edu.pe.cibertec.SAIBM.util.HashUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolService rolService;

    @Autowired
    private MembresiaService membresiaService;

    @Autowired
    private PerfilService perfilService;

    @GetMapping("/listar_usuarios")
    public String listarUsuarios(@RequestParam(name = "nombre", required = false) String nombre, Model model) {
        if (nombre != null && !nombre.isEmpty()) {
            model.addAttribute("usuarios", usuarioService.buscarPorNombre(nombre));
        } else {
            model.addAttribute("usuarios", usuarioService.conseguirTodo());
        }
        return "usuario/listar_usuarios";
    }



    @GetMapping("/nuevo")
    public String mostrarFormularioRegistro(Model model) {
        UsuarioEntity usuario = new UsuarioEntity();

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.conseguirTodo());
        model.addAttribute("membresias", membresiaService.conseguirTodo());
        model.addAttribute("perfiles", perfilService.conseguirTodo());
        return "usuario/editar_usuario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        UsuarioEntity usuario = usuarioService.conseguirPorID(id);
        if (usuario == null) {
            usuario = new UsuarioEntity();
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", rolService.conseguirTodo());
        model.addAttribute("membresias", membresiaService.conseguirTodo());
        model.addAttribute("perfiles", perfilService.conseguirTodo());
        return "usuario/editar_usuario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute UsuarioEntity usuario) {
        if (usuario.getId() == null || usuario.getId() == 0) {
            usuario.setContraseña(HashUtil.Nuevo(usuario.getContraseña()));
            usuarioService.crear(usuario);
        } else {
            UsuarioEntity usuarioActual = usuarioService.conseguirPorID(usuario.getId());

            if (usuario.getContraseña() == null || usuario.getContraseña().isEmpty()) {
                usuario.setContraseña(usuarioActual.getContraseña());
            } else {
                usuario.setContraseña(HashUtil.Nuevo(usuario.getContraseña()));
            }
            usuarioService.modificar(usuario);
        }
        return "redirect:/usuario/listar_usuarios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el usuario porque tiene reservas registradas.");
        }
        return "redirect:/usuario/listar_usuarios";
    }
}