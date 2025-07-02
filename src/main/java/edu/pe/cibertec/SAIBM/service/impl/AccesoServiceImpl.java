package edu.pe.cibertec.SAIBM.service.impl;

import edu.pe.cibertec.SAIBM.entity.security.AccesoEntity;
import edu.pe.cibertec.SAIBM.repository.AccesoRepository;
import edu.pe.cibertec.SAIBM.service.AccesoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccesoServiceImpl extends GenericServiceImpl<AccesoEntity, Long> implements AccesoService {

    @Autowired
    private AccesoRepository repository;

    @PostConstruct
    private void init() {
        super.repository = repository;
    }
}

