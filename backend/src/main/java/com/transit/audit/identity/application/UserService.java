package com.transit.audit.identity.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.common.concurrency.OptimisticLockSupport;
import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.identity.domain.model.User;
import com.transit.audit.identity.infrastructure.persistence.UserRepository;
import com.transit.audit.identity.web.request.CreateUserRequest;
import com.transit.audit.identity.web.request.SetUserEnabledRequest;
import com.transit.audit.identity.web.request.UpdateUserRoleRequest;
import com.transit.audit.identity.web.response.UserResponse;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userMapper = userMapper;
	}

	@Transactional(readOnly = true)
	public Page<UserResponse> listUsers(Pageable pageable) {
		return userRepository.findAll(pageable).map(userMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public UserResponse getUser(Long id) {
		return userMapper.toResponse(requireUser(id));
	}

	@Transactional
	public UserResponse createUser(CreateUserRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new BusinessException("Conflict", "Username already exists: " + request.username(), 409);
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new BusinessException("Conflict", "Email already exists: " + request.email(), 409);
		}
		User user = new User(request.username(), request.email(), passwordEncoder.encode(request.password()),
				request.role());
		return userMapper.toResponse(userRepository.save(user));
	}

	@Transactional
	public UserResponse changeRole(Long id, UpdateUserRoleRequest request) {
		User user = requireUser(id);
		OptimisticLockSupport.requireVersion(user.getVersion(), request.version(), "User");
		user.setRole(request.role());
		return userMapper.toResponse(userRepository.save(user));
	}

	@Transactional
	public UserResponse setEnabled(Long id, SetUserEnabledRequest request) {
		User user = requireUser(id);
		OptimisticLockSupport.requireVersion(user.getVersion(), request.version(), "User");
		user.setEnabled(request.enabled());
		return userMapper.toResponse(userRepository.save(user));
	}

	private User requireUser(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
	}
}
