package com.dtp.cosmemgt.admin.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String fullName;
    String phoneNum;
    String address;
    String email;
    Set<RoleResponse> roles;
    Boolean isActive;
}
