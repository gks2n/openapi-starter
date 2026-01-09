package com.eaglebank.bankapi.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaglebank.bankapi.security.JwtService;
import com.eaglebank.generated.model.BankAccountResponse;
import com.eaglebank.generated.model.CreateBankAccountRequest;
import com.eaglebank.generated.model.ListBankAccountsResponse;
import com.eaglebank.generated.model.UpdateBankAccountRequest;
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

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AccountService accountService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void createAccountReturnsCreated() throws Exception {
		when(accountService.create(any(CreateBankAccountRequest.class))).thenReturn(sampleResponse());

		mockMvc.perform(post("/v1/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleCreateRequest())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accountNumber").value("01234567"));
	}

	@Test
	void listAccountsReturnsOk() throws Exception {
		ListBankAccountsResponse response = new ListBankAccountsResponse()
				.accounts(List.of(sampleResponse()));
		when(accountService.list()).thenReturn(response);

		mockMvc.perform(get("/v1/accounts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accounts").isArray())
				.andExpect(jsonPath("$.accounts[0].accountNumber").value("01234567"));
	}

	@Test
	void fetchAccountReturnsOk() throws Exception {
		when(accountService.fetchByAccountNumber("01234567")).thenReturn(sampleResponse());

		mockMvc.perform(get("/v1/accounts/01234567"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber").value("01234567"));
	}

	@Test
	void updateAccountReturnsOk() throws Exception {
		when(accountService.updateByAccountNumber(any(String.class), any(UpdateBankAccountRequest.class)))
				.thenReturn(sampleResponse());

		UpdateBankAccountRequest update = new UpdateBankAccountRequest().name("Updated Account");

		mockMvc.perform(patch("/v1/accounts/01234567")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber").value("01234567"));
	}

	@Test
	void deleteAccountReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/v1/accounts/01234567"))
				.andExpect(status().isNoContent());

		verify(accountService).deleteByAccountNumber("01234567");
	}

	private static CreateBankAccountRequest sampleCreateRequest() {
		return new CreateBankAccountRequest()
				.name("Personal Account")
				.accountType(CreateBankAccountRequest.AccountTypeEnum.PERSONAL);
	}

	private static BankAccountResponse sampleResponse() {
		return new BankAccountResponse()
				.accountNumber("01234567")
				.sortCode(BankAccountResponse.SortCodeEnum._10_10_10)
				.name("Personal Account")
				.accountType(BankAccountResponse.AccountTypeEnum.PERSONAL)
				.balance(new BigDecimal("100.00"))
				.currency(BankAccountResponse.CurrencyEnum.GBP)
				.createdTimestamp(OffsetDateTime.now())
				.updatedTimestamp(OffsetDateTime.now());
	}
}
