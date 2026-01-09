package com.eaglebank.bankapi.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaglebank.bankapi.security.JwtService;
import com.eaglebank.generated.model.CreateTransactionRequest;
import com.eaglebank.generated.model.ListTransactionsResponse;
import com.eaglebank.generated.model.TransactionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private TransactionService transactionService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void createTransactionReturnsCreated() throws Exception {
		when(transactionService.create(any(String.class), any(CreateTransactionRequest.class)))
				.thenReturn(sampleResponse());

		mockMvc.perform(post("/v1/accounts/01234567/transactions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleCreateRequest())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("tan-abc123"));
	}

	@Test
	void listTransactionsReturnsOk() throws Exception {
		ListTransactionsResponse response = new ListTransactionsResponse()
				.transactions(List.of(sampleResponse()));
		when(transactionService.list("01234567")).thenReturn(response);

		mockMvc.perform(get("/v1/accounts/01234567/transactions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactions").isArray())
				.andExpect(jsonPath("$.transactions[0].id").value("tan-abc123"));
	}

	@Test
	void fetchTransactionReturnsOk() throws Exception {
		when(transactionService.fetchById("01234567", "tan-abc123")).thenReturn(sampleResponse());

		mockMvc.perform(get("/v1/accounts/01234567/transactions/tan-abc123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("tan-abc123"));
	}

	private static CreateTransactionRequest sampleCreateRequest() {
		return new CreateTransactionRequest()
				.amount(new BigDecimal("10.50"))
				.currency(CreateTransactionRequest.CurrencyEnum.GBP)
				.type(CreateTransactionRequest.TypeEnum.DEPOSIT)
				.reference("Top up");
	}

	private static TransactionResponse sampleResponse() {
		return new TransactionResponse()
				.id("tan-abc123")
				.amount(new BigDecimal("10.50"))
				.currency(TransactionResponse.CurrencyEnum.GBP)
				.type(TransactionResponse.TypeEnum.DEPOSIT)
				.reference("Top up")
				.userId("usr-abc123")
				.createdTimestamp(OffsetDateTime.now());
	}
}
