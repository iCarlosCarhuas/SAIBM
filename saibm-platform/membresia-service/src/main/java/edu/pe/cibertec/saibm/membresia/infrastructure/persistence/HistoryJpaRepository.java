package edu.pe.cibertec.saibm.membresia.infrastructure.persistence;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface HistoryJpaRepository extends JpaRepository<HistoryJpaEntity,UUID>{List<HistoryJpaEntity> findByUserIdOrderByOccurredAtDesc(String userId);}
