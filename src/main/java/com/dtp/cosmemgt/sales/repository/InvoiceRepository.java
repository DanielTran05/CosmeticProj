package com.dtp.cosmemgt.sales.repository;

import com.dtp.cosmemgt.sales.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
}
