package com.eaglebank.bankapi.transaction;

import com.eaglebank.generated.model.CreateTransactionRequest;
import com.eaglebank.generated.model.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdTimestamp", ignore = true)
	@Mapping(target = "account", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "currency", source = "currency", qualifiedByName = "currencyToValue")
	@Mapping(target = "type", source = "type", qualifiedByName = "typeToValue")
	TransactionEntity toEntity(CreateTransactionRequest request);

	@Mapping(target = "currency", source = "currency", qualifiedByName = "currencyFromValue")
	@Mapping(target = "type", source = "type", qualifiedByName = "typeFromValue")
	@Mapping(target = "userId", source = "user.id")
	TransactionResponse toResponse(TransactionEntity entity);

	@Named("currencyToValue")
	static String currencyToValue(CreateTransactionRequest.CurrencyEnum currency) {
		return currency == null ? null : currency.getValue();
	}

	@Named("typeToValue")
	static String typeToValue(CreateTransactionRequest.TypeEnum type) {
		return type == null ? null : type.getValue();
	}

	@Named("currencyFromValue")
	static TransactionResponse.CurrencyEnum currencyFromValue(String value) {
		return value == null ? null : TransactionResponse.CurrencyEnum.fromValue(value);
	}

	@Named("typeFromValue")
	static TransactionResponse.TypeEnum typeFromValue(String value) {
		return value == null ? null : TransactionResponse.TypeEnum.fromValue(value);
	}
}
