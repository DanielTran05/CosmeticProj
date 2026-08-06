package com.dtp.cosmemgt.sales.review.entity;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.catalog.entity.Product;
import com.dtp.cosmemgt.catalog.entity.ProductVariant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "review")
public class Review{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "customer_id",
        foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    User customer;  //Customer → USER

    @NotNull
    @Builder.Default
    int ratingStar = 5;

    String comment;
}