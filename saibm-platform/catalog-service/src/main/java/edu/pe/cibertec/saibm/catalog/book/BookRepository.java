package edu.pe.cibertec.saibm.catalog.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<BookEntity, Integer> {
    @Query("select b from BookEntity b where b.active = true and "
            + "(:q = '' or lower(b.title) like lower(concat('%', :q, '%')) "
            + "or lower(b.author) like lower(concat('%', :q, '%'))) "
            + "order by b.title asc, b.id asc")
    Page<BookEntity> searchActive(String q, Pageable pageable);

    java.util.Optional<BookEntity> findByIdAndActiveTrue(Integer id);
}
