package edu.pe.cibertec.saibm.catalog.inventory;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryEntity> findByBookId(Integer bookId);
}
