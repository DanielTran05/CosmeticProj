package com.dtp.cosmemgt.catalog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryCreationRequest {
    @Positive(message = "PARENT_ID_INVALID")
    Integer parentId;

    @NotBlank(message = "CATEGORY_MUST_NOT_BE_BLANK")
    @Size(min=1, max=100, message = "CATEGORY_NAME_INVALID_LENGTH")
    String name;

    @Size(min=1, max=225, message = "CATEGORY_DES_INVALID_LENGTH")
    String description;

    String imgUrl;
}