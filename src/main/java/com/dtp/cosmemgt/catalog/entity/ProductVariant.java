package com.dtp.cosmemgt.catalog.entity;

import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product_variant")
@SQLDelete(sql = "UPDATE product_variant SET deleted_at = NOW() WHERE id = ?")
public class ProductVariant extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_id")
    UnitOfMeasure unitOfMeasure;

    @Basic(optional = false)
    @NotNull
    String sku;

    String barcode;

    @Basic(optional = false)
    @NotNull
    String variantName;

    String img;
}