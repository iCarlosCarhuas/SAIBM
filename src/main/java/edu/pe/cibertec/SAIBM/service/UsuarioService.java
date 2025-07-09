package edu.pe.cibertec.SAIBM.service;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;

import java.util.List;
import java.util.Optional;

public interface UsuarioService extends GenericService<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> encontrarPorCorreo(String correo);
    Optional<UsuarioEntity> validarLogin(String correo, String contraseña);
    List<UsuarioEntity> buscarPorNombre(String nombre);

}
