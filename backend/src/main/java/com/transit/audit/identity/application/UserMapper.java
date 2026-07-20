package com.transit.audit.identity.application;

import org.mapstruct.Mapper;

import com.transit.audit.identity.domain.model.User;
import com.transit.audit.identity.web.response.UserResponse;

@Mapper
public interface UserMapper {

	UserResponse toResponse(User user);
}
