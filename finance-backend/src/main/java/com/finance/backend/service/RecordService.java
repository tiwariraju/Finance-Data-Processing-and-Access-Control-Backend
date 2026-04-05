package com.finance.backend.service;

import com.finance.backend.dto.request.RecordRequest;
import com.finance.backend.dto.response.PagedRecordsResponse;
import com.finance.backend.dto.response.RecordResponse;
import com.finance.backend.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * CRUD and query operations for financial records with soft-delete semantics.
 */
public interface RecordService {

    /**
     * Creates a new financial record owned by the given user id (typically current user).
     */
    RecordResponse createRecord(RecordRequest request, Long creatorUserId);

    /**
     * Paginated list with optional filters; excludes soft-deleted rows.
     */
    PagedRecordsResponse getAllRecords(
            TransactionType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            Pageable pageable);

    /**
     * Fetches a single non-deleted record.
     */
    RecordResponse getRecordById(Long id);

    /**
     * Updates an existing non-deleted record.
     */
    RecordResponse updateRecord(Long id, RecordRequest request);

    /**
     * Marks the record as deleted ({@code isDeleted = true}).
     */
    void softDeleteRecord(Long id);
}
