package com.dtp.cosmemgt.sales.entity;

import com.dtp.cosmemgt.sales.enums.PaymentMethodEnum;
import com.dtp.cosmemgt.sales.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethodEnum paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatusEnum paymentStatus;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}