package com.eaglebank.bankapi.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaglebank.generated.model.CreateUserRequest;
import com.eaglebank.generated.model.CreateUserRequestAddress;
import com.eaglebank.generated.model.UpdateUserRequest;
import com.eaglebank.generated.model.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.eaglebank.bankapi.security.JwtService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private JwtService jwtService;

	@Test
	void createUserReturnsCreated() throws Exception {
		UserResponse response = sampleResponse();
		when(userService.create(any(CreateUserRequest.class))).thenReturn(response);

		mockMvc.perform(post("/v1/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(sampleCreateRequest())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("usr-abc123"));

		verify(userService).create(any(CreateUserRequest.class));
	}

	@Test
	void fetchUserReturnsOk() throws Exception {
		when(userService.fetchById("usr-abc123")).thenReturn(sampleResponse());

		mockMvc.perform(get("/v1/users/usr-abc123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("usr-abc123"));
	}

	@Test
	void updateUserReturnsOk() throws Exception {
		when(userService.updateById(any(String.class), any(UpdateUserRequest.class)))
				.thenReturn(sampleResponse());

		UpdateUserRequest update = new UpdateUserRequest().name("Updated");

		mockMvc.perform(patch("/v1/users/usr-abc123")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(update)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("usr-abc123"));
	}

	@Test
	void deleteUserReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/v1/users/usr-abc123"))
				.andExpect(status().isNoContent());

		verify(userService).deleteById("usr-abc123");
	}

	private static CreateUserRequest sampleCreateRequest() {
		CreateUserRequestAddress address = new CreateUserRequestAddress()
				.line1("1 High Street")
				.town("London")
				.county("Greater London")
				.postcode("SW1A 1AA");
		return new CreateUserRequest()
				.name("Test User")
				.address(address)
				.phoneNumber("+447700900123")
				.email("test.user@example.com");
	}

	private static UserResponse sampleResponse() {
		CreateUserRequestAddress address = new CreateUserRequestAddress()
				.line1("1 High Street")
				.town("London")
				.county("Greater London")
				.postcode("SW1A 1AA");
		return new UserResponse()
				.id("usr-abc123")
				.name("Test User")
				.address(address)
				.phoneNumber("+447700900123")
				.email("test.user@example.com")
				.createdTimestamp(OffsetDateTime.now())
				.updatedTimestamp(OffsetDateTime.now());
	}
}
