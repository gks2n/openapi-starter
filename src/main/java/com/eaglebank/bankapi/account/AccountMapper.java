package com.eaglebank.bankapi.account;

import com.eaglebank.generated.model.BankAccountResponse;
import com.eaglebank.generated.model.CreateBankAccountRequest;
import com.eaglebank.generated.model.UpdateBankAccountRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccountMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "sortCode", ignore = true)
	@Mapping(target = "balance", ignore = true)
	@Mapping(target = "currency", ignore = true)
	@Mapping(target = "createdTimestamp", ignore = true)
	@Mapping(target = "updatedTimestamp", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "transactions", ignore = true)
	@Mapping(target = "accountType", source = "accountType", qualifiedByName = "createAccountTypeToValue")
	AccountEntity toEntity(CreateBankAccountRequest request);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", ignore = true)
	@Mapping(target = "sortCode", ignore = true)
	@Mapping(target = "balance", ignore = true)
	@Mapping(target = "currency", ignore = true)
	@Mapping(target = "createdTimestamp", ignore = true)
	@Mapping(target = "updatedTimestamp", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "transactions", ignore = true)
	@Mapping(target = "accountType", source = "accountType", qualifiedByName = "updateAccountTypeToValue")
	void updateEntity(UpdateBankAccountRequest request, @MappingTarget AccountEntity entity);

	@Mapping(target = "accountNumber", source = "id")
	@Mapping(target = "sortCode", source = "sortCode", qualifiedByName = "sortCodeFromValue")
	@Mapping(target = "accountType", source = "accountType", qualifiedByName = "accountTypeFromValue")
	@Mapping(target = "currency", source = "currency", qualifiedByName = "currencyFromValue")
	BankAccountResponse toResponse(AccountEntity entity);

	@Named("createAccountTypeToValue")
	static String createAccountTypeToValue(CreateBankAccountRequest.AccountTypeEnum type) {
		return type == null ? null : type.getValue();
	}

	@Named("updateAccountTypeToValue")
	static String updateAccountTypeToValue(UpdateBankAccountRequest.AccountTypeEnum type) {
		return type == null ? null : type.getValue();
	}

	@Named("accountTypeFromValue")
	static BankAccountResponse.AccountTypeEnum accountTypeFromValue(String value) {
		return value == null ? null : BankAccountResponse.AccountTypeEnum.fromValue(value);
	}

	@Named("currencyFromValue")
	static BankAccountResponse.CurrencyEnum currencyFromValue(String value) {
		return value == null ? null : BankAccountResponse.CurrencyEnum.fromValue(value);
	}

	@Named("sortCodeFromValue")
	static BankAccountResponse.SortCodeEnum sortCodeFromValue(String value) {
		return value == null ? null : BankAccountResponse.SortCodeEnum.fromValue(value);
	}
}
