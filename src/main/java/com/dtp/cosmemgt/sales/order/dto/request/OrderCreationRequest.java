package com.dtp.cosmemgt.sales.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreationRequest {
    @NotEmpty(message = "ORDER_DETAILS_REQUIRED")
    @Valid
    @Size(max=100)
    @UniqueElements
    List<@Valid OrderDetailRequest> orderDetailRequests;

    @Valid
    ShippingOrderCreationRequest shippingOrderCreationRequest;

    @NotBlank(message = "PAYMENT_METHOD_REQUIRED")
    String paymentMethod;

    String codeVoucher;

    @Size(max = 500, message = "NOTE_INVALID_LENGTH")
    String note;
}
