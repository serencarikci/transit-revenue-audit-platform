package com.transit.audit.terminal.application;

import org.mapstruct.Mapper;

import com.transit.audit.terminal.domain.model.Terminal;
import com.transit.audit.terminal.domain.model.TerminalDepotAssignment;
import com.transit.audit.terminal.web.response.AssignmentResponse;
import com.transit.audit.terminal.web.response.TerminalResponse;

@Mapper
public interface TerminalMapper {

	TerminalResponse toResponse(Terminal terminal);

	AssignmentResponse toResponse(TerminalDepotAssignment assignment);
}
