package com.eaglebank.bankapi.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {
	List<AccountEntity> findAllByUser_Id(String userId);
}
