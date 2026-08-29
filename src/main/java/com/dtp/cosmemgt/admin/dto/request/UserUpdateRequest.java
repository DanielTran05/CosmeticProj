package com.dtp.cosmemgt.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    @Size(min = 2, max = 100, message = "FULL_NAME_INVALID_LENGTH")
    String fullName;

    @Size(max = 255, message = "ADDRESS_INVALID_LENGTH")
    String address;

    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "PHONE_NUM_INVALID_FORMAT")
    String phoneNum;
}