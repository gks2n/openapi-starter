package com.eaglebank.bankapi.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, String> {
	@Query(value = "select count(*) from accounts where user_id = :userId", nativeQuery = true)
	long countAccountsByUserId(@Param("userId") String userId);

	Optional<UserEntity> findByEmail(String email);
}
