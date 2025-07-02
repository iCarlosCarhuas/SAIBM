package edu.pe.cibertec.SAIBM.service.impl;

import edu.pe.cibertec.SAIBM.entity.security.RolEntity;
import edu.pe.cibertec.SAIBM.repository.RolRepository;
import edu.pe.cibertec.SAIBM.service.RolService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class RolServiceImpl extends GenericServiceImpl<RolEntity, Integer> implements RolService {

    public RolServiceImpl(RolRepository repository) { // Usa tu RolRepository directamente
        this.repository = repository;
    }
}
