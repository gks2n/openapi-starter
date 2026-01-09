package com.eaglebank.bankapi.user;

import com.eaglebank.generated.model.CreateUserRequest;
import com.eaglebank.generated.model.UserResponse;
import com.eaglebank.generated.model.UpdateUserRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdTimestamp", ignore = true)
	@Mapping(target = "updatedTimestamp", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "accounts", ignore = true)
	@Mapping(target = "addressLine1", source = "address.line1")
	@Mapping(target = "addressLine2", source = "address.line2")
	@Mapping(target = "addressLine3", source = "address.line3")
	@Mapping(target = "addressTown", source = "address.town")
	@Mapping(target = "addressCounty", source = "address.county")
	@Mapping(target = "addressPostcode", source = "address.postcode")
	UserEntity toEntity(CreateUserRequest request);

	@Mapping(target = "address.line1", source = "addressLine1")
	@Mapping(target = "address.line2", source = "addressLine2")
	@Mapping(target = "address.line3", source = "addressLine3")
	@Mapping(target = "address.town", source = "addressTown")
	@Mapping(target = "address.county", source = "addressCounty")
	@Mapping(target = "address.postcode", source = "addressPostcode")
	UserResponse toResponse(UserEntity entity);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdTimestamp", ignore = true)
	@Mapping(target = "updatedTimestamp", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "accounts", ignore = true)
	@Mapping(target = "addressLine1", source = "address.line1")
	@Mapping(target = "addressLine2", source = "address.line2")
	@Mapping(target = "addressLine3", source = "address.line3")
	@Mapping(target = "addressTown", source = "address.town")
	@Mapping(target = "addressCounty", source = "address.county")
	@Mapping(target = "addressPostcode", source = "address.postcode")
	void updateEntity(UpdateUserRequest request, @MappingTarget UserEntity entity);
}
