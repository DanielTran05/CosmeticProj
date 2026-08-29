package com.dtp.cosmemgt.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
    @NotBlank(message = "ROLE_NAME_REQUIRED")
    @Size(min = 2, max = 50, message = "ROLE_NAME_INVALID_LENGTH")
    String name;

    @Size(max = 255, message = "DESCRIPTION_INVALID_LENGTH")
    String description;

    Set<String> permissions;
}
