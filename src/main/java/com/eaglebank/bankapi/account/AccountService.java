package com.eaglebank.bankapi.account;

import com.eaglebank.bankapi.error.NotFoundException;
import com.eaglebank.bankapi.security.CurrentUserService;
import com.eaglebank.bankapi.user.UserEntity;
import com.eaglebank.bankapi.user.UserRepository;
import com.eaglebank.generated.model.BankAccountResponse;
import com.eaglebank.generated.model.CreateBankAccountRequest;
import com.eaglebank.generated.model.ListBankAccountsResponse;
import com.eaglebank.generated.model.UpdateBankAccountRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
	private static final String DEFAULT_SORT_CODE = "10-10-10";
	private static final String DEFAULT_CURRENCY = "GBP";

	private final AccountRepository accountRepository;
	private final AccountMapper accountMapper;
	private final CurrentUserService currentUserService;
	private final UserRepository userRepository;

	public AccountService(
			AccountRepository accountRepository,
			AccountMapper accountMapper,
			CurrentUserService currentUserService,
			UserRepository userRepository
	) {
		this.accountRepository = accountRepository;
		this.accountMapper = accountMapper;
		this.currentUserService = currentUserService;
		this.userRepository = userRepository;
	}

	@Transactional
	public BankAccountResponse create(CreateBankAccountRequest request) {
		String userId = currentUserService.getCurrentUserId();
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User was not found"));
		AccountEntity entity = accountMapper.toEntity(request);
		entity.setId(generateAccountNumber());
		entity.setUser(user);
		entity.setSortCode(DEFAULT_SORT_CODE);
		entity.setBalance(BigDecimal.ZERO);
		entity.setCurrency(DEFAULT_CURRENCY);
		AccountEntity saved = accountRepository.save(entity);
		return accountMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public ListBankAccountsResponse list() {
		String userId = currentUserService.getCurrentUserId();
		List<BankAccountResponse> accounts = accountRepository.findAllByUser_Id(userId)
				.stream()
				.map(accountMapper::toResponse)
				.toList();
		return new ListBankAccountsResponse().accounts(accounts);
	}

	@Transactional(readOnly = true)
	public BankAccountResponse fetchByAccountNumber(String accountNumber) {
		AccountEntity entity = accountRepository.findById(accountNumber)
				.orElseThrow(() -> new NotFoundException("Bank account was not found"));
		validateOwnership(entity, "The user is not allowed to access the bank account details");
		return accountMapper.toResponse(entity);
	}

	@Transactional
	public BankAccountResponse updateByAccountNumber(String accountNumber, UpdateBankAccountRequest request) {
		AccountEntity entity = accountRepository.findById(accountNumber)
				.orElseThrow(() -> new NotFoundException("Bank account was not found"));
		validateOwnership(entity, "The user is not allowed to update the bank account details");
		accountMapper.updateEntity(request, entity);
		AccountEntity saved = accountRepository.save(entity);
		return accountMapper.toResponse(saved);
	}

	@Transactional
	public void deleteByAccountNumber(String accountNumber) {
		AccountEntity entity = accountRepository.findById(accountNumber)
				.orElseThrow(() -> new NotFoundException("Bank account was not found"));
		validateOwnership(entity, "The user is not allowed to delete the bank account details");
		accountRepository.delete(entity);
	}

	private void validateOwnership(AccountEntity entity, String message) {
		String userId = currentUserService.getCurrentUserId();
		if (!userId.equals(entity.getUser().getId())) {
			throw new AccessDeniedException(message);
		}
	}

	private static String generateAccountNumber() {
		int suffix = ThreadLocalRandom.current().nextInt(0, 1_000_000);
		return "01" + String.format("%06d", suffix);
	}
}
