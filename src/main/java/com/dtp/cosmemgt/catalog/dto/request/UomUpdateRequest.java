package com.dtp.cosmemgt.catalog.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UomUpdateRequest {
    @Size(min = 1, max = 10)
    String name;
}
