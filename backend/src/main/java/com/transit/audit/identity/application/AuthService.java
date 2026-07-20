package com.transit.audit.identity.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.config.JwtProperties;
import com.transit.audit.identity.domain.model.User;
import com.transit.audit.identity.infrastructure.persistence.UserRepository;
import com.transit.audit.identity.web.response.LoginResponse;

@Service
public class AuthService {

	private static final String ISSUER = "transit-revenue-audit-platform";

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
			JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.jwtEncoder = jwtEncoder;
		this.jwtProperties = jwtProperties;
	}

	public LoginResponse login(String username, String password) {




		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new BusinessException("Unauthorized", "Invalid username or password", 401));

		Instant now = Instant.now();
		Instant expiresAt = now.plus(jwtProperties.accessTokenMinutes(), ChronoUnit.MINUTES);

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(ISSUER).issuedAt(now).expiresAt(expiresAt)
				.subject(user.getUsername()).claim("userId", user.getId()).claim("role", user.getRole().name()).build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		long expiresInSeconds = jwtProperties.accessTokenMinutes() * 60;

		return new LoginResponse(token, "Bearer", expiresInSeconds, user.getUsername(), user.getRole());
	}
}
