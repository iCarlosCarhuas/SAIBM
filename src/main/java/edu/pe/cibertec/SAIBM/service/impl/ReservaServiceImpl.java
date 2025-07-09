package edu.pe.cibertec.SAIBM.service.impl;

import edu.pe.cibertec.SAIBM.entity.ReservaEntity;
import edu.pe.cibertec.SAIBM.repository.LibroRepository;
import edu.pe.cibertec.SAIBM.repository.ReservaRepository;
import edu.pe.cibertec.SAIBM.service.ReservaService;
import edu.pe.cibertec.SAIBM.util.DateUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Override
    public boolean existeReserva(Integer libroId, Integer usuarioId) {
        //acceso a BD para saber si existen reservas
        return reservaRepository.existsByLibroIdAndUsuarioId(libroId, usuarioId);
    }

    @Override
    public int contarReservasPorUsuario(Integer usuarioId) {
        return reservaRepository.countByUsuarioId(usuarioId);
    }

    @Transactional
    @Override
    public void confirmarReserva(ReservaEntity reserva) {
        Integer usuarioId = reserva.getUsuario().getId();
        int cantidadActual = reservaRepository.countByUsuarioId(usuarioId);

        // Obtener nombre de membresía
        String nombreMembresia = reserva.getUsuario().getMembresia().getNombreMembresia();

        // Determinar límite de reservas según membresía
        int limite = switch (nombreMembresia.toLowerCase()) {
            case "super premium plus" -> 8;
            case "super premium" -> 4;
            default -> 2;
        };

        // Validar límite
        if (cantidadActual + 1 > limite) {
            throw new RuntimeException("Has excedido tu límite de reservas según tu membresía (" + nombreMembresia + ").");
        }

        // Disminuir stock del libro
        var libro = reserva.getLibro();
        if (libro.getStock() <= 0) {
            throw new RuntimeException("El libro no tiene stock disponible.");
        }
        libro.setStock(libro.getStock() - 1);
        libroRepository.save(libro);

        // Guardar reserva
        LocalDate fechaReserva = LocalDate.now();
        LocalDate fechaExpiracion = DateUtil.sumarMeses(fechaReserva, 2);

        reserva.setFechaReserva(Date.valueOf(fechaReserva).toLocalDate());
        reserva.setFechaExpiracion(Date.valueOf(fechaExpiracion).toLocalDate());
        reservaRepository.save(reserva);

    }


    @Override
    public List<ReservaEntity> conseguirPorUsuario(Integer usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public void cancelarReserva(Integer libroId, Integer usuarioId) {
        reservaRepository.deleteByLibroIdAndUsuarioId(libroId, usuarioId);
    }

    @Override
    public List<Integer> conseguirIdsReservadosPorUsuario(Integer usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(r -> r.getLibro().getId())
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservaEntity> conseguirReserva() {
        return reservaRepository.findAll();
    }

    @Override
    public void eliminar(Integer id) {
        reservaRepository.deleteById(id);
    }

}
