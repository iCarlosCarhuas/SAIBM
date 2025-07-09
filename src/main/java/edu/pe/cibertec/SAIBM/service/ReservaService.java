package edu.pe.cibertec.SAIBM.service;

import edu.pe.cibertec.SAIBM.entity.ReservaEntity;

import java.util.List;

public interface ReservaService {
    boolean existeReserva(Integer libroId, Integer usuarioId);
    int contarReservasPorUsuario(Integer usuarioId);
    void confirmarReserva(ReservaEntity reserva);
    void cancelarReserva(Integer libroId, Integer usuarioId);
    List<Integer> conseguirIdsReservadosPorUsuario(Integer usuarioId);

    List<ReservaEntity> conseguirPorUsuario(Integer usuarioId);

    List<ReservaEntity> conseguirReserva();

    void eliminar(Integer id);
}
