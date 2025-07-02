package edu.pe.cibertec.SAIBM.service;

import edu.pe.cibertec.SAIBM.entity.MembresiaEntity;

import java.util.Optional;

public interface MembresiaService extends GenericService<MembresiaEntity, Integer> {

    Optional<MembresiaEntity> encontrarPorNombre(String nombreMembresia);
}
