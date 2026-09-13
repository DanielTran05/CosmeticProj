package com.dtp.cosmemgt.catalog.dto.response;

import com.dtp.cosmemgt.catalog.entity.Product;

public interface BestSellerProductProjection {
    Product getProduct();
    Long getTotalSold();
}