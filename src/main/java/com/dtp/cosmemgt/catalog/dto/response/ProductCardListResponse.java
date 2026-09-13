package com.dtp.cosmemgt.catalog.dto.response;

import lombok.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardListResponse implements Serializable {
    private List<ProductCardResponse> items;

    public static ProductCardListResponse of(List<ProductCardResponse> items) {
        return new ProductCardListResponse(items);
    }
}