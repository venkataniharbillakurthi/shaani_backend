package com.example.shaani.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class JwtService {

	private final Algorithm algorithm;
	private final long expirationMs;

	public JwtService(
			@Value("${shaani.jwt.secret}") String secret,
			@Value("${shaani.jwt.expiration-ms}") long expirationMs) {
		this.algorithm = Algorithm.HMAC256(secret);
		this.expirationMs = expirationMs;
	}

	public String createToken(String username) {
		Date now = new Date();
		return JWT.create()
				.withSubject(username)
				.withIssuedAt(now)
				.withExpiresAt(new Date(now.getTime() + expirationMs))
				.sign(algorithm);
	}

	public String verify(String token) {
		return JWT.require(algorithm).build().verify(token).getSubject();
	}
}
