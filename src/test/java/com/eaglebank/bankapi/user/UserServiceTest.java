package com.eaglebank.bankapi.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eaglebank.bankapi.error.ConflictException;
import com.eaglebank.bankapi.error.NotFoundException;
import com.eaglebank.bankapi.security.CurrentUserService;
import com.eaglebank.generated.model.CreateUserRequest;
import com.eaglebank.generated.model.CreateUserRequestAddress;
import com.eaglebank.generated.model.UpdateUserRequest;
import com.eaglebank.generated.model.UserResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

	@Test
	void createAssignsIdAndReturnsResponse() {
		UserRepository repository = mock(UserRepository.class);
		UserMapper mapper = mock(UserMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		UserService service = new UserService(
				repository,
				mapper,
				currentUserService,
				passwordEncoder,
				"password123"
		);

		UserEntity entity = new UserEntity();
		CreateUserRequest request = sampleCreateRequest();
		UserResponse response = new UserResponse().id("usr-test");

		when(mapper.toEntity(request)).thenReturn(entity);
		when(passwordEncoder.encode("password123")).thenReturn("hashed");
		when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(mapper.toResponse(any(UserEntity.class))).thenReturn(response);

		UserResponse result = service.create(request);

		ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getId()).startsWith("usr-");
		assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
		assertThat(result.getId()).isEqualTo("usr-test");
	}

	@Test
	void fetchByIdThrowsWhenMissing() {
		UserRepository repository = mock(UserRepository.class);
		UserMapper mapper = mock(UserMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		UserService service = new UserService(
				repository,
				mapper,
				currentUserService,
				passwordEncoder,
				"password123"
		);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-missing");
		when(repository.findById("usr-missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.fetchById("usr-missing"))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("User was not found");
	}

	@Test
	void fetchByIdThrowsWhenNotOwner() {
		UserRepository repository = mock(UserRepository.class);
		UserMapper mapper = mock(UserMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		UserService service = new UserService(
				repository,
				mapper,
				currentUserService,
				passwordEncoder,
				"password123"
		);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-owner");

		assertThatThrownBy(() -> service.fetchById("usr-other"))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("The user is not allowed to access the user details");
	}

	@Test
	void updateByIdThrowsWhenMissing() {
		UserRepository repository = mock(UserRepository.class);
		UserMapper mapper = mock(UserMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		UserService service = new UserService(
				repository,
				mapper,
				currentUserService,
				passwordEncoder,
				"password123"
		);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-missing");
		when(repository.findById("usr-missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateById("usr-missing", new UpdateUserRequest()))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	void updateByIdThrowsWhenNotOwner() {
		UserRepository repository = mock(UserRepository.class);
		UserMapper mapper = mock(UserMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		UserService service = new UserService(
				repository,
				mapper,
				currentUserService,
				passwordEncoder,
				"password123"
		);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-owner");

		assertThatThrownBy(() -> service.updateById("usr-other", new UpdateUserRequest()))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("The user is not allowed to update the user details");
	}

	@Test
	void deleteByIdThrowsConflictWhenAccountsExist() {
		UserRepository repository = mock(UserRepository.class);
		UserMapper mapper = mock(UserMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		UserService service = new UserService(
				repository,
				mapper,
				currentUserService,
				passwordEncoder,
				"password123"
		);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-has-accounts");
		when(repository.existsById("usr-has-accounts")).thenReturn(true);
		when(repository.countAccountsByUserId("usr-has-accounts")).thenReturn(1L);

		assertThatThrownBy(() -> service.deleteById("usr-has-accounts"))
				.isInstanceOf(ConflictException.class)
				.hasMessage("A user cannot be deleted when they are associated with a bank account");
	}

	@Test
	void deleteByIdThrowsWhenNotOwner() {
		UserRepository repository = mock(UserRepository.class);
		UserMapper mapper = mock(UserMapper.class);
		CurrentUserService currentUserService = mock(CurrentUserService.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		UserService service = new UserService(
				repository,
				mapper,
				currentUserService,
				passwordEncoder,
				"password123"
		);

		when(currentUserService.getCurrentUserId()).thenReturn("usr-owner");

		assertThatThrownBy(() -> service.deleteById("usr-other"))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("The user is not allowed to delete the user");
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
}
