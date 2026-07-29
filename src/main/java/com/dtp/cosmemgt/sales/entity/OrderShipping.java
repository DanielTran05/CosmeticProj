package com.dtp.cosmemgt.sales.entity;

import com.dtp.cosmemgt.core.baseEntity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "order_shipping")
public class OrderShipping extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    Order order;

    @Column(name = "tracking_number", length = 50)
    String trackingNumber;

    @Column(name = "shipping_provider", length = 50)
    String shippingProvider;

    String receiverName;
    String receiverPhone;
    String receiverAddress;
    String note;
}