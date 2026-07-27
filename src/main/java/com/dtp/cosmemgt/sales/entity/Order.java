package com.dtp.cosmemgt.sales.entity;

import com.dtp.cosmemgt.admin.entity.User;
import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import com.dtp.cosmemgt.sales.enums.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "`order`")
public class Order extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    User customer;

    @Column(precision = 19, scale = 4)
    BigDecimal totalAmount;

    @Column(precision = 19, scale = 4)
    BigDecimal totalCogs;

    @Enumerated(EnumType.STRING)
    OrderStatusEnum orderStatus;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private OrderShipping orderShipping;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Invoice invoice;
}