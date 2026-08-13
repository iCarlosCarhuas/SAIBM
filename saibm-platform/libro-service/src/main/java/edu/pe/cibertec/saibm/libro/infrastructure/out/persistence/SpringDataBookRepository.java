package edu.pe.cibertec.saibm.libro.infrastructure.out.persistence;
import java.util.UUID; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
interface SpringDataBookRepository extends JpaRepository<BookJpaEntity,UUID> { @Query("select b from BookJpaEntity b where b.active=true and (:q='' or lower(b.title) like lower(concat('%',:q,'%')) or lower(b.author) like lower(concat('%',:q,'%'))) order by b.title,b.id") Page<BookJpaEntity> active(@Param("q")String q,Pageable p); }
