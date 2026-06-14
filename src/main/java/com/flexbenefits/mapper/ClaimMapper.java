package com.flexbenefits.mapper;

import com.flexbenefits.dto.ClaimResponse;
import com.flexbenefits.entity.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClaimMapper {

    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "employee.id", target = "employeeId")
    ClaimResponse toResponse(Claim claim);

    List<ClaimResponse> toResponseList(List<Claim> claims);
}


