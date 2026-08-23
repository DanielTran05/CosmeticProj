package com.dtp.cosmemgt.catalog.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateRequest {
    @Positive(message = "PARENT_ID_INVALID")
    Integer parentId;

    @Size(min=1, max=100)
    String name;
}