package com.transit.audit.depot.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

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
import com.transit.audit.common.web.GlobalExceptionHandler;
import com.transit.audit.depot.application.DepotService;
import com.transit.audit.depot.web.request.CreateDepotRequest;
import com.transit.audit.depot.web.response.DepotResponse;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DepotControllerTest.TestConfig.class)
@WebAppConfiguration
class DepotControllerTest {

    @Configuration
    @EnableWebMvc
    @EnableMethodSecurity
    @EnableSpringDataWebSupport
    static class TestConfig {

        @Bean
        DepotService depotService() {
            return mock(DepotService.class);
        }

        @Bean
        DepotController depotController(DepotService depotService) {
            return new DepotController(depotService);
        }

        @Bean
        GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DepotService depotService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        reset(depotService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDepot_returnsCreated_forAdmin() throws Exception {
        DepotResponse response = new DepotResponse(1L, "TST1", "Test Depot", true, Instant.now(), Instant.now(), 0L);
        when(depotService.createDepot(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/depots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDepotRequest("TST1", "Test Depot"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("TST1"));
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void createDepot_forbidden_forRoleWithoutWriteAccess() throws Exception {
        mockMvc.perform(post("/api/v1/depots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDepotRequest("TST1", "Test Depot"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDepot_unauthorized_withoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/depots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateDepotRequest("TST1", "Test Depot"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDepot_rejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/depots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"TST1\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "AUDITOR")
    void listDepots_allowsAnyAuthenticatedRole() throws Exception {
        DepotResponse response = new DepotResponse(1L, "TST1", "Test Depot", true, Instant.now(), Instant.now(), 0L);
        when(depotService.listDepots(isNull(), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(response)));

        mockMvc.perform(get("/api/v1/depots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("TST1"));
    }

    @Test
    void listDepots_unauthorized_withoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/depots")).andExpect(status().isUnauthorized());
	}
}
