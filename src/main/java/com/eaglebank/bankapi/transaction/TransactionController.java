package com.eaglebank.bankapi.transaction;

import com.eaglebank.generated.api.TransactionApi;
import com.eaglebank.generated.model.CreateTransactionRequest;
import com.eaglebank.generated.model.ListTransactionsResponse;
import com.eaglebank.generated.model.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController implements TransactionApi {
	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Override
	public ResponseEntity<TransactionResponse> createTransaction(
			String accountNumber,
			CreateTransactionRequest createTransactionRequest
	) {
		TransactionResponse response = transactionService.create(accountNumber, createTransactionRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Override
	public ResponseEntity<ListTransactionsResponse> listAccountTransaction(String accountNumber) {
		return ResponseEntity.ok(transactionService.list(accountNumber));
	}

	@Override
	public ResponseEntity<TransactionResponse> fetchAccountTransactionByID(
			String accountNumber,
			String transactionId
	) {
		return ResponseEntity.ok(transactionService.fetchById(accountNumber, transactionId));
	}
}
