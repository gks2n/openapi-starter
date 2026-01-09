package com.eaglebank.bankapi.auth;

import com.eaglebank.bankapi.security.JwtService;
import com.eaglebank.bankapi.user.UserEntity;
import com.eaglebank.bankapi.user.UserRepository;
import com.eaglebank.generated.api.AuthApi;
import com.eaglebank.generated.model.AuthRequest;
import com.eaglebank.generated.model.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserRepository userRepository;

	public AuthController(
			AuthenticationManager authenticationManager,
			JwtService jwtService,
			UserRepository userRepository
	) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@Override
	public ResponseEntity<AuthResponse> authenticateUser(AuthRequest authRequest) {
		if (authRequest == null
				|| authRequest.getEmail() == null
				|| authRequest.getPassword() == null) {
			return ResponseEntity.badRequest().build();
		}

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							authRequest.getEmail(),
							authRequest.getPassword()
					)
			);
		} catch (AuthenticationException ex) {
			throw new AuthenticationCredentialsNotFoundException("Invalid email or password");
		}

		UserEntity user = userRepository.findByEmail(authRequest.getEmail())
				.orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Invalid email or password"));

		String token = jwtService.generateToken(authRequest.getEmail(), user.getId());
		AuthResponse response = new AuthResponse()
				.accessToken(token)
				.tokenType("Bearer")
				.expiresIn(jwtService.getExpirationSeconds());
		return ResponseEntity.ok(response);
	}
}
