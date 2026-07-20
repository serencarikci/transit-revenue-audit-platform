package com.transit.audit.identity.infrastructure.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.transit.audit.identity.domain.model.User;
import com.transit.audit.identity.infrastructure.persistence.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(),
				user.isEnabled(), true, true, true, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
	}
}
