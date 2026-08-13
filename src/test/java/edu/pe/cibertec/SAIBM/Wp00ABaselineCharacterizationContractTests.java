package edu.pe.cibertec.SAIBM;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import edu.pe.cibertec.SAIBM.controller.SecurityController;
import edu.pe.cibertec.SAIBM.controller.UsuarioController;
import edu.pe.cibertec.SAIBM.dto.PedidoDetalleLibroDto;
import edu.pe.cibertec.SAIBM.entity.LibroEntity;
import edu.pe.cibertec.SAIBM.entity.MembresiaEntity;
import edu.pe.cibertec.SAIBM.entity.ReservaEntity;
import edu.pe.cibertec.SAIBM.entity.security.PerfilEntity;
import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import edu.pe.cibertec.SAIBM.repository.LibroRepository;
import edu.pe.cibertec.SAIBM.repository.ReservaRepository;
import edu.pe.cibertec.SAIBM.service.UsuarioService;
import edu.pe.cibertec.SAIBM.service.impl.LibroServiceImpl;
import edu.pe.cibertec.SAIBM.service.impl.ReservaServiceImpl;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

class Wp00ABaselineCharacterizationContractTests {

    @Test
    void authenticationRegistrationAndAdministrationKeepHappyAndRejectedPaths() {
        UsuarioService users = mock(UsuarioService.class);
        SecurityController security = new SecurityController();
        UsuarioEntity user = user("premium");
        PerfilEntity profile = new PerfilEntity();
        profile.setAccesos(List.of());
        user.setPerfil(profile);
        ReflectionTestUtils.setField(security, "usuarioService", users);
        when(users.validarLogin("ok", "pw")).thenReturn(Optional.of(user));
        Model success = new ExtendedModelMap();
        assertEquals("redirect:/biblioteca/listar_libros", security.procesarLogueo("ok", "pw",
                mock(jakarta.servlet.http.HttpSession.class), success));
        assertEquals(user, success.getAttribute("usuario"));
        when(users.validarLogin("bad", "pw")).thenReturn(Optional.empty());
        assertEquals("security/login", security.procesarLogueo("bad", "pw",
                mock(jakarta.servlet.http.HttpSession.class), new ExtendedModelMap()));

        UsuarioEntity duplicate = user("premium");
        duplicate.setCorreo("taken");
        when(users.encontrarPorCorreo("taken")).thenReturn(Optional.of(duplicate));
        Model registration = new ExtendedModelMap();
        assertEquals("security/register", security.crear(duplicate, registration));
        assertEquals("El correo ya está registrado", registration.getAttribute("error"));

        UsuarioController administration = new UsuarioController();
        ReflectionTestUtils.setField(administration, "usuarioService", users);
        when(users.conseguirTodo()).thenReturn(List.of(user));
        Model listing = new ExtendedModelMap();
        assertEquals("usuario/listar_usuarios", administration.listarUsuarios("", listing));
        assertEquals(List.of(user), listing.getAttribute("usuarios"));
        doThrow(new DataIntegrityViolationException("reserved")).when(users).eliminar(7);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        administration.eliminarUsuario(7, redirect);
        assertTrue(redirect.getFlashAttributes().containsKey("error"));
    }

    @Test
    void catalogSearchAndCirculationPreserveStockQuotaAndRejectionInvariants() {
        LibroRepository books = mock(LibroRepository.class);
        ReservaRepository reservations = mock(ReservaRepository.class);
        LibroServiceImpl catalog = new LibroServiceImpl();
        ReflectionTestUtils.setField(catalog, "libroRepository", books);
        ReflectionTestUtils.setField(catalog, "reservaRepository", reservations);
        LibroEntity book = book(1, 1);
        when(books.findByNombreLibro("book")).thenReturn(List.of(book));
        assertEquals(book, catalog.buscarPorNombre("book").get(0));
        when(reservations.countByLibroId(1)).thenReturn(1);
        assertTrue(assertThrows(RuntimeException.class, () -> catalog.eliminar(1)).getMessage().contains("reservas"));

        ReservaServiceImpl circulation = new ReservaServiceImpl();
        ReflectionTestUtils.setField(circulation, "reservaRepository", reservations);
        ReflectionTestUtils.setField(circulation, "libroRepository", books);
        ReservaEntity reservation = new ReservaEntity();
        reservation.setUsuario(user("premium"));
        reservation.setLibro(book);
        when(reservations.countByUsuarioId(1)).thenReturn(0, 0, 2);
        circulation.confirmarReserva(reservation);
        assertEquals(0, book.getStock());
        verify(books).save(book);
        book.setStock(0);
        assertTrue(assertThrows(RuntimeException.class, () -> circulation.confirmarReserva(reservation))
                .getMessage().contains("stock"));
        book.setStock(1);
        assertTrue(assertThrows(RuntimeException.class, () -> circulation.confirmarReserva(reservation))
                .getMessage().contains("límite"));
    }

    @Test
    void reportingKeepsPopulatedAndEmptyDataSourcePaths() {
        PedidoDetalleLibroDto row = new PedidoDetalleLibroDto(1, "book", 3, "SAIBM");
        assertTrue(new JRBeanCollectionDataSource(List.of(row)).next());
        assertEquals("book", row.getNombre_libro());
        assertTrue(!new JRBeanCollectionDataSource(List.of()).next());
    }

    private LibroEntity book(int id, int stock) {
        LibroEntity book = new LibroEntity();
        book.setId(id);
        book.setStock(stock);
        book.setNombreLibro("book");
        return book;
    }

    private UsuarioEntity user(String membershipName) {
        UsuarioEntity user = new UsuarioEntity();
        user.setId(1);
        MembresiaEntity membership = new MembresiaEntity();
        membership.setNombreMembresia(membershipName);
        user.setMembresia(membership);
        return user;
    }
}
