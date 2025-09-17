package com.namaste.Namaste.to.TM2.Repository;

import com.namaste.Namaste.to.TM2.Model.AbhaTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbhaTransactionRepository extends MongoRepository<AbhaTransaction, String> {
    Optional<AbhaTransaction> findByTxnId(String txnId);

    @Query("{'status': 'PENDING', 'expiresAt': {$lt: ?0}}")
    List<AbhaTransaction> findExpiredTransactions(LocalDateTime now);

    List<AbhaTransaction> findByHealthIdOrderByCreatedAtDesc(String healthId);
}
