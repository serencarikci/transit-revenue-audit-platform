package com.transit.audit.depot.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.transit.audit.common.exception.BusinessException;
import com.transit.audit.common.exception.ResourceNotFoundException;
import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.depot.infrastructure.persistence.DepotRepository;
import com.transit.audit.depot.web.request.CreateDepotRequest;
import com.transit.audit.depot.web.request.SetDepotActiveRequest;
import com.transit.audit.depot.web.request.UpdateDepotRequest;
import com.transit.audit.depot.web.response.DepotResponse;

@ExtendWith(MockitoExtension.class)
class DepotServiceTest {

	@Mock
	private DepotRepository depotRepository;

	private DepotService depotService;

	private static final DepotMapper DEPOT_MAPPER = depot -> new DepotResponse(depot.getId(), depot.getCode(),
			depot.getName(), depot.isActive(), depot.getCreatedAt(), depot.getUpdatedAt(), depot.getVersion());

	@BeforeEach
	void setUp() {
		depotService = new DepotService(depotRepository, DEPOT_MAPPER);
	}

	@Test
	void createDepot_savesNewDepot_whenCodeIsUnique() {
		when(depotRepository.existsByCode("NEL")).thenReturn(false);
		when(depotRepository.save(any(Depot.class))).thenAnswer(invocation -> invocation.getArgument(0));

		DepotResponse response = depotService.createDepot(new CreateDepotRequest("NEL", "Nelspruit Depot"));

		assertThat(response.code()).isEqualTo("NEL");
		assertThat(response.name()).isEqualTo("Nelspruit Depot");
		assertThat(response.active()).isTrue();
	}

	@Test
	void createDepot_throwsConflict_whenCodeAlreadyExists() {
		when(depotRepository.existsByCode("NEL")).thenReturn(true);

		assertThatThrownBy(() -> depotService.createDepot(new CreateDepotRequest("NEL", "Nelspruit Depot")))
				.isInstanceOf(BusinessException.class).hasMessageContaining("NEL");
	}

	@Test
	void getDepot_throwsNotFound_whenDepotDoesNotExist() {
		when(depotRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> depotService.getDepot(99L)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateDepot_updatesName_whenVersionMatches() {
		Depot depot = depotWithVersion("Old Name", 3L);
		when(depotRepository.findById(1L)).thenReturn(Optional.of(depot));
		when(depotRepository.save(any(Depot.class))).thenAnswer(invocation -> invocation.getArgument(0));

		DepotResponse response = depotService.updateDepot(1L, new UpdateDepotRequest("New Name", 3L));

		assertThat(response.name()).isEqualTo("New Name");
	}

	@Test
	void updateDepot_throwsConflict_whenVersionDoesNotMatchStaleClientState() {
		Depot depot = depotWithVersion("Old Name", 3L);
		when(depotRepository.findById(1L)).thenReturn(Optional.of(depot));

		assertThatThrownBy(() -> depotService.updateDepot(1L, new UpdateDepotRequest("New Name", 2L)))
				.isInstanceOf(BusinessException.class).hasMessageContaining("changed by another user");
	}

	@Test
	void setActive_deactivatesDepot_whenVersionMatches() {
		Depot depot = depotWithVersion("Depot", 0L);
		when(depotRepository.findById(1L)).thenReturn(Optional.of(depot));
		when(depotRepository.save(any(Depot.class))).thenAnswer(invocation -> invocation.getArgument(0));

		DepotResponse response = depotService.setActive(1L, new SetDepotActiveRequest(false, 0L));

		assertThat(response.active()).isFalse();
	}

	private static Depot depotWithVersion(String name, Long version) {
		Depot depot = new Depot("NEL", name);
		ReflectionTestUtils.setField(depot, "version", version);
		return depot;
	}
}
