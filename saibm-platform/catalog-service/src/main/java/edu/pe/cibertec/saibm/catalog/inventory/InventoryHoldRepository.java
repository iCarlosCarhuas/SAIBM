package edu.pe.cibertec.saibm.catalog.inventory;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface InventoryHoldRepository extends JpaRepository<InventoryHoldEntity, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "insert into inventory_holds "
            + "(operation_key, book_id, quantity, expires_at, request_hash, state) "
            + "values (:key, :bookId, :quantity, :expiresAt, :requestHash, 'HELD') "
            + "on conflict (operation_key) do nothing", nativeQuery = true)
    int insertIfAbsent(@org.springframework.data.repository.query.Param("key") String key,
            @org.springframework.data.repository.query.Param("bookId") Integer bookId,
            @org.springframework.data.repository.query.Param("quantity") int quantity,
            @org.springframework.data.repository.query.Param("expiresAt") java.time.Instant expiresAt,
            @org.springframework.data.repository.query.Param("requestHash") String requestHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select h from InventoryHoldEntity h where h.operationKey = :operationKey")
    Optional<InventoryHoldEntity> findByOperationKeyForUpdate(
            @org.springframework.data.repository.query.Param("operationKey") String operationKey);
}
