package com.eaglebank.bankapi.account;

import com.eaglebank.bankapi.transaction.TransactionEntity;
import com.eaglebank.bankapi.user.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountEntity {
    @Id
    @Column(name = "id", nullable = false, length = 8)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "sort_code", nullable = false, length = 8)
    private String sortCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "account_type", nullable = false, length = 32)
    private String accountType;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_timestamp", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    @UpdateTimestamp
    private OffsetDateTime updatedTimestamp;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    private Long version;

    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TransactionEntity> transactions;
}
