package com.dtp.cosmemgt.admin.mapper;

import com.dtp.cosmemgt.admin.dto.request.PermissionRequest;
import com.dtp.cosmemgt.admin.dto.response.PermissionResponse;
import com.dtp.cosmemgt.admin.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
