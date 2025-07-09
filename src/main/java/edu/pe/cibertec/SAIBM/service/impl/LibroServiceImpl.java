package edu.pe.cibertec.SAIBM.service.impl;

import edu.pe.cibertec.SAIBM.entity.LibroEntity;
import edu.pe.cibertec.SAIBM.repository.LibroRepository;
import edu.pe.cibertec.SAIBM.repository.ReservaRepository;
import edu.pe.cibertec.SAIBM.service.LibroService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroServiceImpl extends GenericServiceImpl<LibroEntity, Integer> implements LibroService {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Override
    public List<LibroEntity> buscarPorNombre(String name) {
        return libroRepository.findByNombreLibro(name);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        int countReservas = reservaRepository.countByLibroId(id);
        if (countReservas > 0) {
            throw new RuntimeException("No se puede eliminar el libro porque tiene reservas activas.");
        }
        libroRepository.deleteById(id);
    }


    @PostConstruct
    private void init() {
        super.repository = libroRepository;
    }
}
