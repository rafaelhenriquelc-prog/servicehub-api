package com.petservicehub.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String generateToken(User user) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.claim("role", user.getRole().name())
				.issuedAt(now)
				.expiration(expiration)
				.signWith(secretKey)
				.compact();
	}

	public Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public Long expirationMs() {
		return expirationMs;
	}

	public UserRole roleFrom(Claims claims) {
		return UserRole.valueOf(claims.get("role", String.class));
	}

	public Long userIdFrom(Claims claims) {
		Number userId = claims.get("userId", Number.class);
		return userId == null ? null : userId.longValue();
	}
}
