package com.eaglebank.bankapi.transaction;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
	List<TransactionEntity> findAllByAccount_Id(String accountId);

	Optional<TransactionEntity> findByIdAndAccount_Id(String id, String accountId);
}
