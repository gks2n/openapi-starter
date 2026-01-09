package com.eaglebank.bankapi.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {
	public String getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null
				|| authentication.getName().isBlank()) {
			throw new AuthenticationCredentialsNotFoundException("Access token is missing or invalid");
		}
		return authentication.getName();
	}
}
