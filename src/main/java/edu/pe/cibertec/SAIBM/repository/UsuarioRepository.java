package edu.pe.cibertec.SAIBM.repository;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    @Query("SELECT u FROM UsuarioEntity u WHERE u.correo = :correo AND u.contraseña = :contraseña")
    Optional<UsuarioEntity> validarLogin(@Param("correo") String correo, @Param("contraseña") String contraseña);

    Optional<UsuarioEntity> findByCorreo(String correo);

    List<UsuarioEntity> findByNombre(String nombre);

}
