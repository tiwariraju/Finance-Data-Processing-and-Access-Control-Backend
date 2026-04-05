package com.finance.backend.service.impl;

import com.finance.backend.dto.request.RecordRequest;
import com.finance.backend.dto.response.PagedRecordsResponse;
import com.finance.backend.dto.response.RecordResponse;
import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.entity.User;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.exception.ResourceNotFoundException;
import com.finance.backend.repository.FinancialRecordRepository;
import com.finance.backend.repository.UserRepository;
import com.finance.backend.repository.specification.FinancialRecordSpecification;
import com.finance.backend.service.RecordService;
import com.finance.backend.util.RecordDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final UserRepository userRepository;
    private final RecordDtoMapper recordDtoMapper;

    @Override
    @Transactional
    public RecordResponse createRecord(RecordRequest request, Long creatorUserId) {
        User creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorUserId));
        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .isDeleted(false)
                .createdBy(creator)
                .build();
        record = financialRecordRepository.save(record);
        log.info("Created financial record {} by user {}", record.getId(), creatorUserId);
        return recordDtoMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedRecordsResponse getAllRecords(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            Pageable pageable) {

        Specification<FinancialRecord> spec = Specification
                .where(FinancialRecordSpecification.notDeleted())
                .and(FinancialRecordSpecification.hasType(type))
                .and(FinancialRecordSpecification.hasCategory(category))
                .and(FinancialRecordSpecification.dateOnOrAfter(startDate))
                .and(FinancialRecordSpecification.dateOnOrBefore(endDate))
                .and(FinancialRecordSpecification.descriptionContains(search));

        Page<FinancialRecord> page = financialRecordRepository.findAll(spec, pageable);
        var content = page.getContent().stream().map(recordDtoMapper::toResponse).toList();
        return PagedRecordsResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RecordResponse getRecordById(Long id) {
        FinancialRecord record = financialRecordRepository.findActiveByIdWithCreatedBy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));
        return recordDtoMapper.toResponse(record);
    }

    @Override
    @Transactional
    public RecordResponse updateRecord(Long id, RecordRequest request) {
        FinancialRecord record = financialRecordRepository.findActiveByIdWithCreatedBy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());
        record = financialRecordRepository.save(record);
        log.info("Updated financial record {}", id);
        return recordDtoMapper.toResponse(record);
    }

    @Override
    @Transactional
    public void softDeleteRecord(Long id) {
        FinancialRecord record = financialRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id: " + id));
        if (Boolean.TRUE.equals(record.getIsDeleted())) {
            throw new ResourceNotFoundException("Financial record not found with id: " + id);
        }
        record.setIsDeleted(true);
        financialRecordRepository.save(record);
        log.info("Soft-deleted financial record {}", id);
    }
}
