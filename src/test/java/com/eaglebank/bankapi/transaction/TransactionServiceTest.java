package com.eaglebank.bankapi.transaction;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eaglebank.bankapi.account.AccountEntity;
import com.eaglebank.bankapi.account.AccountRepository;
import com.eaglebank.bankapi.error.UnprocessableEntityException;
import com.eaglebank.bankapi.security.CurrentUserService;
import com.eaglebank.bankapi.user.UserEntity;
import com.eaglebank.generated.model.CreateTransactionRequest;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class TransactionServiceTest {

	@Test
	void createWithdrawalThrowsWhenInsufficientFunds() {
		TransactionRepository transactionRepository = mock(TransactionRepository.class);
		TransactionMapper mapper = mock(TransactionMapper.class);
		AccountRepository accountRepository = mock(AccountRepository.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		TransactionService service = new TransactionService(
				transactionRepository,
				mapper,
				accountRepository,
				currentUserService
		);

		UserEntity user = new UserEntity();
		user.setId("usr-1");
		AccountEntity account = new AccountEntity();
		account.setId("01234567");
		account.setUser(user);
		account.setBalance(new BigDecimal("10.00"));

		TransactionEntity entity = new TransactionEntity();
		entity.setAmount(new BigDecimal("50.00"));
		entity.setType("withdrawal");

		when(currentUserService.getCurrentUserId()).thenReturn("usr-1");
		when(accountRepository.findById("01234567")).thenReturn(Optional.of(account));
		when(mapper.toEntity(any(CreateTransactionRequest.class))).thenReturn(entity);

		assertThatThrownBy(() -> service.create(
				"01234567",
				new CreateTransactionRequest()
						.amount(new BigDecimal("50.00"))
						.currency(CreateTransactionRequest.CurrencyEnum.GBP)
						.type(CreateTransactionRequest.TypeEnum.WITHDRAWAL)
		))
				.isInstanceOf(UnprocessableEntityException.class)
				.hasMessage("Insufficient funds to process transaction");
		verify(transactionRepository, never()).save(any(TransactionEntity.class));
	}

	@Test
	void listThrowsWhenNotOwner() {
		TransactionRepository transactionRepository = mock(TransactionRepository.class);
		TransactionMapper mapper = mock(TransactionMapper.class);
		AccountRepository accountRepository = mock(AccountRepository.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		TransactionService service = new TransactionService(
				transactionRepository,
				mapper,
				accountRepository,
				currentUserService
		);

		UserEntity other = new UserEntity();
		other.setId("usr-other");
		AccountEntity account = new AccountEntity();
		account.setUser(other);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-1");
		when(accountRepository.findById("01234567")).thenReturn(Optional.of(account));

		assertThatThrownBy(() -> service.list("01234567"))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("The user is not allowed to access the transactions");
	}
}
