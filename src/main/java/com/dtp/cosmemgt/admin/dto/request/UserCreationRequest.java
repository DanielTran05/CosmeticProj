package com.dtp.cosmemgt.admin.dto.request;

import com.dtp.cosmemgt.admin.validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "FULL_NAME_REQUIRED")
    @Size(min = 2, max = 100, message = "FULL_NAME_INVALID_LENGTH")
    String fullName;

    @NotBlank(message = "PHONE_NUM_REQUIRED")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "PHONE_NUM_INVALID_FORMAT")
    String phoneNum;

    @Size(max = 255, message = "ADDRESS_INVALID_LENGTH")
    String address;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID_FORMAT")
    String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    Set<String> roles;

    String avatar;
}