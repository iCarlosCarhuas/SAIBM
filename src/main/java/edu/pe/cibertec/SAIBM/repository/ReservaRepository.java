package edu.pe.cibertec.SAIBM.repository;

import edu.pe.cibertec.SAIBM.entity.ReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservaRepository extends JpaRepository<ReservaEntity, Integer> {
    boolean existsByLibroIdAndUsuarioId(Integer libroId, Integer usuarioId);
    int countByUsuarioId(Integer usuarioId);
    void deleteByLibroIdAndUsuarioId(Integer libroId, Integer usuarioId);
    List<ReservaEntity> findByUsuarioId(Integer usuarioId);
    int countByLibroId(Integer libroId);


    @Modifying
    @Query("DELETE FROM ReservaEntity r WHERE r.libro.id = :libroId")
    void deleteByLibroId(@Param("libroId") Integer libroId);
}
