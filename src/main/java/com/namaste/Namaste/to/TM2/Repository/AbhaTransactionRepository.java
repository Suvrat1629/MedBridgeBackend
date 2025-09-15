package com.namaste.Namaste.to.TM2.Repository;

import com.namaste.Namaste.to.TM2.Model.AbhaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbhaTransactionRepository extends JpaRepository<AbhaTransaction, Long> {
    Optional<AbhaTransaction> findByTxnId(String txnId);

    @Query("SELECT t FROM AbhaTransaction t WHERE t.status = 'PENDING' AND t.expiresAt < :now")
    List<AbhaTransaction> findExpiredTransactions(LocalDateTime now);

    List<AbhaTransaction> findByHealthIdOrderByCreatedAtDesc(String healthId);
}
