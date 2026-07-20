package com.transit.audit.depot.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.support.AbstractPostgresIntegrationTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class DepotRepositoryIT extends AbstractPostgresIntegrationTest {

	@Autowired
	private DepotRepository depotRepository;

	@Test
	void savesDepotAndFindsItById() {
		Depot saved = depotRepository.save(new Depot("TST1", "Test Depot One"));

		assertThat(depotRepository.findById(saved.getId())).isPresent();
		assertThat(depotRepository.existsByCode("TST1")).isTrue();
	}

	@Test
	void findsOnlyDepotsMatchingActiveFlag() {
		Depot active = new Depot("TST2", "Active Test Depot");
		Depot inactive = new Depot("TST3", "Inactive Test Depot");
		inactive.setActive(false);
		depotRepository.save(active);
		depotRepository.save(inactive);

		var activeDepots = depotRepository.findByActive(true, PageRequest.of(0, 50));
		var inactiveDepots = depotRepository.findByActive(false, PageRequest.of(0, 50));

		assertThat(activeDepots.getContent()).extracting(Depot::getCode).contains("TST2").doesNotContain("TST3");
		assertThat(inactiveDepots.getContent()).extracting(Depot::getCode).contains("TST3").doesNotContain("TST2");
	}

	@Test
	void rejectsDuplicateDepotCode() {
		depotRepository.saveAndFlush(new Depot("TST4", "First Depot"));

		assertThatThrownBy(() -> depotRepository.saveAndFlush(new Depot("TST4", "Duplicate Depot")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
