package edu.pe.cibertec.SAIBM.service.impl;

import edu.pe.cibertec.SAIBM.entity.MembresiaEntity;
import edu.pe.cibertec.SAIBM.repository.MembresiaRepository;
import edu.pe.cibertec.SAIBM.service.MembresiaService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MembresiaServiceImpl extends GenericServiceImpl<MembresiaEntity, Integer> implements MembresiaService {

    @Autowired
    private MembresiaRepository membresiaRepository;

    @PostConstruct
    private void init() {
        super.repository = membresiaRepository;
    }

    @Override
    public Optional<MembresiaEntity> encontrarPorNombre(String nombreMembresia) {
        return membresiaRepository.findByNombreMembresia(nombreMembresia);
    }
}
