package com.dtp.cosmemgt.catalog.entity;

import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import com.dtp.cosmemgt.warehouse.entity.Supplier;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "product")
@SQLDelete(sql = "UPDATE product SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @Basic(optional = false)
    @NotNull
    String name;

    @Basic(optional = false)
    @NotNull
    @Column(precision = 19, scale = 4)
    BigDecimal basePrice;

    @Basic(optional = false)
    @NotNull
    @Column(name = "abc_class", length = 5)
    String abcClass;

    String avatar;

    String description;
}