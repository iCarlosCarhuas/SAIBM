package edu.pe.cibertec.SAIBM.service.impl;


import edu.pe.cibertec.SAIBM.entity.security.UsuarioEntity;
import edu.pe.cibertec.SAIBM.service.GenericService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GenericServiceImpl<T,ID> implements GenericService<T,ID> {
    protected JpaRepository<T, ID> repository;

    @Override
    public T conseguirPorID(ID id){ return repository.findById(id).get(); }

    @Override
    public List<T> conseguirTodo() { return repository.findAll(); }

    @Override
    public T crear(T entity) { repository.save(entity);
        return null;
    }

    @Override
    public void modificar (T entity) { repository.save(entity); }

    @Override
    public void eliminar (ID id) { repository.deleteById(id); }

}
