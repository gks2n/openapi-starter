package com.eaglebank.bankapi.account;

import com.eaglebank.generated.api.AccountApi;
import com.eaglebank.generated.model.BankAccountResponse;
import com.eaglebank.generated.model.CreateBankAccountRequest;
import com.eaglebank.generated.model.ListBankAccountsResponse;
import com.eaglebank.generated.model.UpdateBankAccountRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AccountApi {
	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public ResponseEntity<BankAccountResponse> createAccount(CreateBankAccountRequest createBankAccountRequest) {
		BankAccountResponse response = accountService.create(createBankAccountRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Override
	public ResponseEntity<ListBankAccountsResponse> listAccounts() {
		return ResponseEntity.ok(accountService.list());
	}

	@Override
	public ResponseEntity<BankAccountResponse> fetchAccountByAccountNumber(String accountNumber) {
		return ResponseEntity.ok(accountService.fetchByAccountNumber(accountNumber));
	}

	@Override
	public ResponseEntity<BankAccountResponse> updateAccountByAccountNumber(
			String accountNumber,
			UpdateBankAccountRequest updateBankAccountRequest
	) {
		return ResponseEntity.ok(accountService.updateByAccountNumber(accountNumber, updateBankAccountRequest));
	}

	@Override
	public ResponseEntity<Void> deleteAccountByAccountNumber(String accountNumber) {
		accountService.deleteByAccountNumber(accountNumber);
		return ResponseEntity.noContent().build();
	}
}
