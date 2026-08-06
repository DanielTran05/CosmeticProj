package com.dtp.cosmemgt.catalog.service;

import com.dtp.cosmemgt.catalog.entity.UnitOfMeasure;
import com.dtp.cosmemgt.catalog.dto.request.UomCreationRequest;
import com.dtp.cosmemgt.catalog.dto.response.UomResponse;
import com.dtp.cosmemgt.catalog.mapper.UomMapper;
import com.dtp.cosmemgt.catalog.repository.UomRepository;
import com.dtp.cosmemgt.core.exception.AppException;
import com.dtp.cosmemgt.core.exception.ErrorCode;
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
public class AdminUomService {
    UomRepository uomRepository;
    UomMapper uomMapper;

    public UomResponse create(UomCreationRequest request) {
        UnitOfMeasure u = uomMapper.toUom(request);

        if (uomRepository.existsByName(u.getName())) {
            throw new AppException(ErrorCode.UOM_EXISTED);
        }

        return uomMapper.toUomResponse(uomRepository.save(u));
    }

    public UomResponse getUomById(int uomId){
        UnitOfMeasure u = uomRepository.findById(uomId)
                .orElseThrow(() -> new AppException(ErrorCode.UOM_NOT_EXISTED));
        return uomMapper.toUomResponse(u);
    }

    public List<UomResponse> getAll(){
        List<UnitOfMeasure> uoms = uomRepository.findAll();
        return uoms.stream()
                .map(uomMapper::toUomResponse)
                .toList();
    }

    public UomResponse update(int uomId, UomCreationRequest request){
        UnitOfMeasure u = uomRepository.findById(uomId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        u.setName(request.getName());

        return uomMapper.toUomResponse(uomRepository.save(u));
    }

    public void sftDelUom(int uomId) {
        UnitOfMeasure u = uomRepository.findById(uomId)
                .orElseThrow(() -> new AppException(ErrorCode.UOM_NOT_EXISTED));
        uomRepository.delete(u);
    }

    public void restoreUom(int uomId) {
        int rowsAffected = uomRepository.restoreById(uomId);
        if (rowsAffected == 0) {
            throw new AppException(ErrorCode.UOM_NOT_EXISTED);
        }
    }

    public void hardDelUom(int uomId) {
        UnitOfMeasure u = uomRepository.getById(uomId)
                .orElseThrow(() -> new AppException(ErrorCode.UOM_NOT_EXISTED));
        uomRepository.hardDelById(u.getId());
    }
}