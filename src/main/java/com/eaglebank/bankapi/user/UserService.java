package com.eaglebank.bankapi.user;

import com.eaglebank.bankapi.error.ConflictException;
import com.eaglebank.bankapi.error.NotFoundException;
import com.eaglebank.bankapi.security.CurrentUserService;
import com.eaglebank.generated.model.CreateUserRequest;
import com.eaglebank.generated.model.UpdateUserRequest;
import com.eaglebank.generated.model.UserResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final CurrentUserService currentUserService;
	private final PasswordEncoder passwordEncoder;
	private final String defaultPassword;

	public UserService(
			UserRepository userRepository,
			UserMapper userMapper,
			CurrentUserService currentUserService,
			PasswordEncoder passwordEncoder,
			@Value("${app.security.user.default-password}") String defaultPassword
	) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.currentUserService = currentUserService;
		this.passwordEncoder = passwordEncoder;
		this.defaultPassword = defaultPassword;
	}

	@Transactional
	public UserResponse create(CreateUserRequest request) {
		UserEntity entity = userMapper.toEntity(request);
		entity.setId(generateUserId());
		entity.setPasswordHash(passwordEncoder.encode(defaultPassword));
		UserEntity saved = userRepository.save(entity);
		return userMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public UserResponse fetchById(String userId) {
		validateOwnership(userId, "The user is not allowed to access the user details");
		return userRepository.findById(userId)
				.map(userMapper::toResponse)
				.orElseThrow(() -> new NotFoundException("User was not found"));
	}

	@Transactional
	public UserResponse updateById(String userId, UpdateUserRequest request) {
		validateOwnership(userId, "The user is not allowed to update the user details");
		UserEntity entity = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User was not found"));
		userMapper.updateEntity(request, entity);
		UserEntity saved = userRepository.save(entity);
		return userMapper.toResponse(saved);
	}

	@Transactional
	public void deleteById(String userId) {
		validateOwnership(userId, "The user is not allowed to delete the user");
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User was not found");
		}
		if (hasAccounts(userId)) {
			throw new ConflictException(
					"A user cannot be deleted when they are associated with a bank account"
			);
		}
		userRepository.deleteById(userId);
	}

	private void validateOwnership(String userId, String message) {
		String currentUserId = currentUserService.getCurrentUserId();
		if (!currentUserId.equals(userId)) {
			throw new AccessDeniedException(message);
		}
	}

	private boolean hasAccounts(String userId) {
		return userRepository.countAccountsByUserId(userId) > 0;
	}

	private static String generateUserId() {
		return "usr-" + UUID.randomUUID().toString().replace("-", "");
	}

}
