package com.dtp.cosmemgt.sales.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MoMoStatusResponse {
    private boolean isPaid;
    private String transId;
}