package com.eaglebank.bankapi.user;

import com.eaglebank.bankapi.account.AccountEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {
	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Column(name = "name", nullable = false, length = 200)
	private String name;

	@Column(name = "address_line1", nullable = false, length = 200)
	private String addressLine1;

	@Column(name = "address_line2", length = 200)
	private String addressLine2;

	@Column(name = "address_line3", length = 200)
	private String addressLine3;

	@Column(name = "address_town", nullable = false, length = 100)
	private String addressTown;

	@Column(name = "address_county", nullable = false, length = 100)
	private String addressCounty;

	@Column(name = "address_postcode", nullable = false, length = 20)
	private String addressPostcode;

	@Column(name = "phone_number", nullable = false, length = 32)
	private String phoneNumber;

	@Column(name = "email", nullable = false, length = 320)
	private String email;

	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@Column(name = "created_timestamp", nullable = false)
	@CreationTimestamp
	private OffsetDateTime createdTimestamp;

	@Column(name = "updated_timestamp", nullable = false)
	@UpdateTimestamp
	private OffsetDateTime updatedTimestamp;

	@OneToMany(mappedBy = "user")
	private List<AccountEntity> accounts;
}
