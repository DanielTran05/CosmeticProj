package com.dtp.cosmemgt.sales.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingOrderCreationRequest {
    @NotBlank(message = "RECEIVER_NAME_REQUIRED")
    @Size(min = 1, max = 100, message = "RECEIVER_NAME_INVALID_LENGTH")
    String receiverName;

    @NotBlank(message = "RECEIVER_PHONE_REQUIRED")
    @Pattern(regexp = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$", message = "RECEIVER_PHONE_INVALID_FORMAT")
    String receiverPhone;

    @NotBlank(message = "RECEIVER_ADDRESS_REQUIRED")
    @Size(min = 1, max = 255, message = "RECEIVER_ADDRESS_INVALID_LENGTH")
    String receiverAddress;
}
