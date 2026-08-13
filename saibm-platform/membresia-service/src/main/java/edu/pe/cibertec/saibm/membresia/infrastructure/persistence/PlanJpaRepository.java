package edu.pe.cibertec.saibm.membresia.infrastructure.persistence;
import java.time.Instant; import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface PlanJpaRepository extends JpaRepository<PlanJpaEntity,UUID>{@Query("select p from PlanJpaEntity p where p.active=true and p.validFrom<=:at and (p.validUntil is null or p.validUntil>:at) order by p.code,p.version desc")List<PlanJpaEntity> active(@Param("at")Instant at);}
