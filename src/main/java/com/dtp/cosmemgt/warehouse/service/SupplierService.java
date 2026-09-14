package com.dtp.cosmemgt.warehouse.service;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
import com.dtp.cosmemgt.warehouse.dto.request.SupplierCreationRequest;
import com.dtp.cosmemgt.warehouse.dto.response.SupplierResponse;
import com.dtp.cosmemgt.warehouse.entity.Supplier;
import com.dtp.cosmemgt.warehouse.mapper.SupplierMapper;
import com.dtp.cosmemgt.warehouse.repository.SupplierRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class SupplierService {
    SupplierRepository supplierRepository;
    SupplierMapper supplierMapper;

    public SupplierResponse create(SupplierCreationRequest request) {
        Supplier s = supplierMapper.toSupplier(request);

        if (supplierRepository.existsByName(s.getName())) {
            throw new AppException(ErrorCode.SUPPLIER_EXISTED);
        }

        return supplierMapper.toSupplierResponse(supplierRepository.save(s));
    }

    public SupplierResponse getSplById(int supplierId){
        Supplier s = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));
        return supplierMapper.toSupplierResponse(s);
    }

    public List<SupplierResponse> getAll(){
        List<Supplier> suppliers = supplierRepository.findAll();
        return suppliers.stream()
                .map(supplierMapper::toSupplierResponse)
                .toList();
    }

    public SupplierResponse update(int supplierId, SupplierCreationRequest request){
        Supplier s = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));

        supplierMapper.updateSupplierfromRequest(request, s);

        return supplierMapper.toSupplierResponse(supplierRepository.save(s));
    }

    public void sftDelSpl(int supplierId) {
        Supplier s = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));
        supplierRepository.delete(s);
    }

    public void restoreSpl(int supplierId) {
        int rowsAffected = supplierRepository.restoreById(supplierId);
        if (rowsAffected == 0) {
            throw new AppException(ErrorCode.SUPPLIER_NOT_EXISTED);
        }
    }

    public void hardDelSpl(int supplierId) {
        Supplier s = supplierRepository.getById(supplierId)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_EXISTED));
        supplierRepository.hardDelById(s.getId());
    }
}
