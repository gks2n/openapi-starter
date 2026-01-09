package com.eaglebank.bankapi.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eaglebank.bankapi.error.NotFoundException;
import com.eaglebank.bankapi.security.CurrentUserService;
import com.eaglebank.bankapi.user.UserEntity;
import com.eaglebank.bankapi.user.UserRepository;
import com.eaglebank.generated.model.BankAccountResponse;
import com.eaglebank.generated.model.CreateBankAccountRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

class AccountServiceTest {

	@Test
	void createAssignsDefaultsAndUser() {
		AccountRepository repository = mock(AccountRepository.class);
		AccountMapper mapper = mock(AccountMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		UserRepository userRepository = mock(UserRepository.class);
		AccountService service = new AccountService(repository, mapper, currentUserService, userRepository);

		UserEntity user = new UserEntity();
		user.setId("usr-1");
		AccountEntity entity = new AccountEntity();
		BankAccountResponse response = new BankAccountResponse().accountNumber("01234567");

		when(currentUserService.getCurrentUserId()).thenReturn("usr-1");
		when(userRepository.findById("usr-1")).thenReturn(Optional.of(user));
		when(mapper.toEntity(any(CreateBankAccountRequest.class))).thenReturn(entity);
		when(repository.save(any(AccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(mapper.toResponse(any(AccountEntity.class))).thenReturn(response);

		BankAccountResponse result = service.create(new CreateBankAccountRequest().name("Account").accountType(
				CreateBankAccountRequest.AccountTypeEnum.PERSONAL
		));

		ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
		verify(repository).save(captor.capture());
		AccountEntity saved = captor.getValue();
		assertThat(saved.getId()).startsWith("01");
		assertThat(saved.getId()).hasSize(8);
		assertThat(saved.getUser()).isEqualTo(user);
		assertThat(saved.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(saved.getCurrency()).isEqualTo("GBP");
		assertThat(saved.getSortCode()).isEqualTo("10-10-10");
		assertThat(result.getAccountNumber()).isEqualTo("01234567");
	}

	@Test
	void createThrowsWhenUserMissing() {
		AccountRepository repository = mock(AccountRepository.class);
		AccountMapper mapper = mock(AccountMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		UserRepository userRepository = mock(UserRepository.class);
		AccountService service = new AccountService(repository, mapper, currentUserService, userRepository);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-missing");
		when(userRepository.findById("usr-missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.create(new CreateBankAccountRequest()))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("User was not found");
	}

	@Test
	void listReturnsAccountsForCurrentUser() {
		AccountRepository repository = mock(AccountRepository.class);
		AccountMapper mapper = mock(AccountMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		UserRepository userRepository = mock(UserRepository.class);
		AccountService service = new AccountService(repository, mapper, currentUserService, userRepository);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-1");
		when(repository.findAllByUser_Id("usr-1")).thenReturn(List.of(new AccountEntity()));
		when(mapper.toResponse(any(AccountEntity.class))).thenReturn(new BankAccountResponse());

		assertThat(service.list().getAccounts()).hasSize(1);
	}

	@Test
	void fetchByAccountNumberThrowsWhenNotOwner() {
		AccountRepository repository = mock(AccountRepository.class);
		AccountMapper mapper = mock(AccountMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		UserRepository userRepository = mock(UserRepository.class);
		AccountService service = new AccountService(repository, mapper, currentUserService, userRepository);

		UserEntity other = new UserEntity();
		other.setId("usr-other");
		AccountEntity entity = new AccountEntity();
		entity.setUser(other);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-1");
		when(repository.findById("01234567")).thenReturn(Optional.of(entity));

		assertThatThrownBy(() -> service.fetchByAccountNumber("01234567"))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("The user is not allowed to access the bank account details");
	}
}
