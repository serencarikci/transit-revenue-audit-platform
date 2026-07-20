package com.transit.audit.depot.application;

import org.mapstruct.Mapper;

import com.transit.audit.depot.domain.model.Depot;
import com.transit.audit.depot.web.response.DepotResponse;

@Mapper
public interface DepotMapper {

	DepotResponse toResponse(Depot depot);
}
