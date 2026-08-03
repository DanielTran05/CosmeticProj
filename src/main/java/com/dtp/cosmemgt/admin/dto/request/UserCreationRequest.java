package com.dtp.cosmemgt.admin.dto.request;

import com.dtp.cosmemgt.admin.validator.DobConstraint;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
//    @Size(min = 4, message = "USERNAME_INVALID")
//    String username;
    String email;

    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    String fullName;

    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;
}
