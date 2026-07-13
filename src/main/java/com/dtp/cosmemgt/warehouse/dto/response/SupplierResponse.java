package com.dtp.cosmemgt.warehouse.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupplierResponse {
    String name;
    String contactName;
    String phoneNum;
    String address;
}
