package com.dtp.cosmemgt.sales.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentStatusResponse {
    private boolean isPaid;
    private String transId;
}