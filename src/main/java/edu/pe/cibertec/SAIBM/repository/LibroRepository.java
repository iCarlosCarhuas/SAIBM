package edu.pe.cibertec.SAIBM.repository;

import edu.pe.cibertec.SAIBM.entity.LibroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<LibroEntity, Integer> {
    List<LibroEntity> findByNombreLibro(String name);

}
