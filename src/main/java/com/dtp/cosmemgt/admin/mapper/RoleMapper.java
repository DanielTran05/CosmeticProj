package com.dtp.cosmemgt.admin.mapper;

import com.dtp.cosmemgt.admin.dto.request.RoleRequest;
import com.dtp.cosmemgt.admin.dto.response.RoleResponse;
import com.dtp.cosmemgt.admin.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
