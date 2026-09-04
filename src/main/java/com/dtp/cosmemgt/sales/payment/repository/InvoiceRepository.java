package com.dtp.cosmemgt.sales.payment.repository;

import com.dtp.cosmemgt.sales.payment.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByPaymentRequestId(String paymentRequestId);
}