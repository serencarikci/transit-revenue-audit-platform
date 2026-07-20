package com.transit.audit.terminal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.transit.audit.common.web.GlobalExceptionHandler;
import com.transit.audit.terminal.application.TerminalService;
import com.transit.audit.terminal.domain.model.TerminalStatus;
import com.transit.audit.terminal.web.request.CreateAssignmentRequest;
import com.transit.audit.terminal.web.request.CreateTerminalRequest;
import com.transit.audit.terminal.web.response.AssignmentResponse;
import com.transit.audit.terminal.web.response.TerminalResponse;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TerminalControllerTest.TestConfig.class)
@WebAppConfiguration
class TerminalControllerTest {

	@Configuration
	@EnableWebMvc
	@EnableMethodSecurity
	@EnableSpringDataWebSupport
	static class TestConfig {

		@Bean
		TerminalService terminalService() {
			return mock(TerminalService.class);
		}

		@Bean
		TerminalController terminalController(TerminalService terminalService) {
			return new TerminalController(terminalService);
		}

		@Bean
		GlobalExceptionHandler globalExceptionHandler() {
			return new GlobalExceptionHandler();
		}
	}

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private TerminalService terminalService;

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@BeforeEach
	void setUp() {
		reset(terminalService);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	@WithMockUser(roles = "OPERATIONS_USER")
	void createTerminal_returnsCreated_forOperationsUser() throws Exception {
		TerminalResponse response = sampleTerminal();
		when(terminalService.createTerminal(any())).thenReturn(response);

		mockMvc.perform(post("/api/v1/terminals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateTerminalRequest("9999", "SN-9999"))))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.terminalNumber").value("9999"));
	}

	@Test
	@WithMockUser(roles = "AUDITOR")
	void createTerminal_forbidden_forAuditor() throws Exception {
		mockMvc.perform(post("/api/v1/terminals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateTerminalRequest("9999", "SN-9999"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void createTerminal_unauthorized_withoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/v1/terminals").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateTerminalRequest("9999", "SN-9999"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "AUDITOR")
	void listTerminals_allowsAnyAuthenticatedRole() throws Exception {
		when(terminalService.listTerminals(isNull(), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(sampleTerminal())));

		mockMvc.perform(get("/api/v1/terminals")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].terminalNumber").value("9999"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void listAssignments_returnsHistory() throws Exception {
		when(terminalService.listAssignments(1L))
				.thenReturn(List.of(new AssignmentResponse(10L, 1L, 2L, LocalDate.of(2026, 4, 1), null, Instant.now()),
						new AssignmentResponse(9L, 1L, 1L, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31),
								Instant.now())));

		mockMvc.perform(get("/api/v1/terminals/1/assignments")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].depotId").value(2)).andExpect(jsonPath("$[1].validTo").value("2026-03-31"));
	}

	@Test
	@WithMockUser(roles = "FINANCE_USER")
	void createAssignment_returnsCreated() throws Exception {
		when(terminalService.createAssignment(eq(1L), any()))
				.thenReturn(new AssignmentResponse(11L, 1L, 3L, LocalDate.of(2026, 5, 1), null, Instant.now()));

		mockMvc.perform(post("/api/v1/terminals/1/assignments").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CreateAssignmentRequest(3L, LocalDate.of(2026, 5, 1)))))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.depotId").value(3));
	}

	private static TerminalResponse sampleTerminal() {
		Instant now = Instant.now();
		return new TerminalResponse(1L, "9999", "SN-9999", TerminalStatus.HEALTHY, null, null, 0, 0, true, now, now,
				0L);
	}
}
