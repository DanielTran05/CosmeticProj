package com.dtp.cosmemgt.admin.mapper;

import com.dtp.cosmemgt.admin.dto.request.UserCreationRequest;
import com.dtp.cosmemgt.admin.dto.request.UserUpdateRequest;
import com.dtp.cosmemgt.admin.dto.response.UserResponse;
import com.dtp.cosmemgt.admin.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
