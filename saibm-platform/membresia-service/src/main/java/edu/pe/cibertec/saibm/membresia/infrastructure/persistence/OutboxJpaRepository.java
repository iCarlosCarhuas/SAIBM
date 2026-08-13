package edu.pe.cibertec.saibm.membresia.infrastructure.persistence;
import java.util.*; import org.springframework.data.domain.Pageable; import org.springframework.data.jpa.repository.*;
public interface OutboxJpaRepository extends JpaRepository<OutboxJpaEntity,UUID>{@Query("select o from OutboxJpaEntity o where o.published=false order by o.occurredAt")List<OutboxJpaEntity> pending(Pageable page);}
