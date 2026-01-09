package com.eaglebank.bankapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final SecretKey key;
	@Getter
    private final long expirationSeconds;

	public JwtService(
			@Value("${app.security.jwt.secret}") String secret,
			@Value("${app.security.jwt.expiration-seconds:3600}") long expirationSeconds
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationSeconds = expirationSeconds;
	}

	public String generateToken(String subject, String userId) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(subject)
				.claim("userId", userId)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expirationSeconds)))
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}

	public Claims parseToken(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

}
