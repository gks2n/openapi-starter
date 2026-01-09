package com.eaglebank.bankapi.transaction;

import com.eaglebank.bankapi.account.AccountEntity;
import com.eaglebank.bankapi.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class TransactionEntity {
	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "type", nullable = false, length = 16)
	private String type;

	@Column(name = "reference")
	private String reference;

	@Column(name = "created_timestamp", nullable = false)
	@CreationTimestamp
	private OffsetDateTime createdTimestamp;
}
