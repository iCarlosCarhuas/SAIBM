package edu.pe.cibertec.SAIBM.service;

import edu.pe.cibertec.SAIBM.entity.LibroEntity;

import java.util.List;

public interface LibroService extends GenericService<LibroEntity, Integer>  {

    List<LibroEntity> buscarPorNombre(String nombre);
}
