package com.eaglebank.bankapi.transaction;

import com.eaglebank.bankapi.account.AccountEntity;
import com.eaglebank.bankapi.account.AccountRepository;
import com.eaglebank.bankapi.error.NotFoundException;
import com.eaglebank.bankapi.error.UnprocessableEntityException;
import com.eaglebank.bankapi.security.CurrentUserService;
import com.eaglebank.generated.model.CreateTransactionRequest;
import com.eaglebank.generated.model.ListTransactionsResponse;
import com.eaglebank.generated.model.TransactionResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Service
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final TransactionMapper transactionMapper;
	private final AccountRepository accountRepository;
	private final CurrentUserService currentUserService;

	public TransactionService(
			TransactionRepository transactionRepository,
			TransactionMapper transactionMapper,
			AccountRepository accountRepository,
			CurrentUserService currentUserService
	) {
		this.transactionRepository = transactionRepository;
		this.transactionMapper = transactionMapper;
		this.accountRepository = accountRepository;
		this.currentUserService = currentUserService;
	}

	@Transactional
	@Retryable(
			retryFor = {ObjectOptimisticLockingFailureException.class, OptimisticLockException.class},
			maxAttempts = 3,
			backoff = @Backoff(delay = 50)
	)
	public TransactionResponse create(String accountNumber, CreateTransactionRequest request) {
		AccountEntity account = loadAccount(accountNumber);
		validateOwnership(account, "The user is not allowed to delete the bank account details");

		TransactionEntity entity = transactionMapper.toEntity(request);
		entity.setId(generateTransactionId());
		entity.setAccount(account);
		entity.setUser(account.getUser());

		applyBalanceChange(account, entity);
		accountRepository.save(account);

		TransactionEntity saved = transactionRepository.save(entity);
		return transactionMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public ListTransactionsResponse list(String accountNumber) {
		AccountEntity account = loadAccount(accountNumber);
		validateOwnership(account, "The user is not allowed to access the transactions");

		List<TransactionResponse> transactions = transactionRepository.findAllByAccount_Id(account.getId())
				.stream()
				.map(transactionMapper::toResponse)
				.toList();
		return new ListTransactionsResponse().transactions(transactions);
	}

	@Transactional(readOnly = true)
	public TransactionResponse fetchById(String accountNumber, String transactionId) {
		AccountEntity account = loadAccount(accountNumber);
		validateOwnership(account, "The user is not allowed to access the transaction");

		TransactionEntity entity = transactionRepository.findByIdAndAccount_Id(transactionId, account.getId())
				.orElseThrow(() -> new NotFoundException("Bank account was not found"));
		return transactionMapper.toResponse(entity);
	}

	private AccountEntity loadAccount(String accountNumber) {
		return accountRepository.findById(accountNumber)
				.orElseThrow(() -> new NotFoundException("Bank account was not found"));
	}

	private void validateOwnership(AccountEntity entity, String message) {
		String userId = currentUserService.getCurrentUserId();
		if (!userId.equals(entity.getUser().getId())) {
			throw new AccessDeniedException(message);
		}
	}

	private void applyBalanceChange(AccountEntity account, TransactionEntity transaction) {
		BigDecimal amount = safeAmount(transaction.getAmount());
		BigDecimal balance = safeAmount(account.getBalance());
		String type = transaction.getType();
		if ("withdrawal".equalsIgnoreCase(type)) {
			if (balance.compareTo(amount) < 0) {
				throw new UnprocessableEntityException("Insufficient funds to process transaction");
			}
			account.setBalance(balance.subtract(amount));
		} else {
			account.setBalance(balance.add(amount));
		}
	}

	private static BigDecimal safeAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}

	private static String generateTransactionId() {
		return "tan-" + UUID.randomUUID().toString().replace("-", "");
	}
}
