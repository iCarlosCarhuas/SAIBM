package edu.pe.cibertec.SAIBM.service.impl;

import edu.pe.cibertec.SAIBM.entity.security.PerfilEntity;
import edu.pe.cibertec.SAIBM.repository.PerfilRepository;
import edu.pe.cibertec.SAIBM.service.PerfilService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerfilServiceImpl extends GenericServiceImpl<PerfilEntity, Integer> implements PerfilService {

    @Autowired
    private PerfilRepository repository;

    @PostConstruct
    private void init() {
        super.repository = repository;
    }
}
