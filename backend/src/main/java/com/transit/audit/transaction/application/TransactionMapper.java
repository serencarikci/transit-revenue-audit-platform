package com.transit.audit.transaction.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.transit.audit.common.util.CardAliasMasker;
import com.transit.audit.transaction.domain.model.CardTransaction;
import com.transit.audit.transaction.web.response.TransactionResponse;

@Mapper
public interface TransactionMapper {

	@Mapping(target = "cardAlias", expression = "java(maskCardAlias(tx.getCardAlias()))")
	TransactionResponse toResponse(CardTransaction tx);

	default String maskCardAlias(String cardAlias) {
		return CardAliasMasker.mask(cardAlias);
	}
}
