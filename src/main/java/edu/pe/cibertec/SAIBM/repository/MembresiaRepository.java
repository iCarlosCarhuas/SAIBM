package edu.pe.cibertec.SAIBM.repository;

import edu.pe.cibertec.SAIBM.entity.MembresiaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembresiaRepository extends JpaRepository<MembresiaEntity, Integer> {

    Optional<MembresiaEntity> findByNombreMembresia(String nombreMembresia);
}
