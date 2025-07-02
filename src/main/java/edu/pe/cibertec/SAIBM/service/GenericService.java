package edu.pe.cibertec.SAIBM.service;

import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;

import java.util.List;

public interface GenericService<T, ID> {
    T conseguirPorID(ID id);
    List <T> conseguirTodo();
    T crear (T entity);
    void modificar (T entity);
    void eliminar (ID entity);
}
