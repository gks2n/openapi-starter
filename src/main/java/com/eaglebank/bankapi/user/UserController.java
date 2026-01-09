package com.eaglebank.bankapi.user;

import com.eaglebank.generated.api.UserApi;
import com.eaglebank.generated.model.CreateUserRequest;
import com.eaglebank.generated.model.UpdateUserRequest;
import com.eaglebank.generated.model.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserApi {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@Override
	public ResponseEntity<UserResponse> createUser(CreateUserRequest createUserRequest) {
		UserResponse response = userService.create(createUserRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Override
	public ResponseEntity<UserResponse> fetchUserByID(String userId) {
		return ResponseEntity.ok(userService.fetchById(userId));
	}

	@Override
	public ResponseEntity<UserResponse> updateUserByID(String userId, UpdateUserRequest updateUserRequest) {
		return ResponseEntity.ok(userService.updateById(userId, updateUserRequest));
	}

	@Override
	public ResponseEntity<Void> deleteUserByID(String userId) {
		userService.deleteById(userId);
		return ResponseEntity.noContent().build();
	}
}
