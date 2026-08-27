package com.dtp.cosmemgt.sales.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationRequest {
    @NotBlank
    String productId;

    @NotNull
    @Min(1)
    @Max(5)
    Integer ratingStar;

    @NotBlank
    private String comment;
}
