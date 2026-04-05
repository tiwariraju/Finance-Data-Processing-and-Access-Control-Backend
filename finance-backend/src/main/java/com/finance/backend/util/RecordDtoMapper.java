package com.finance.backend.util;

import com.finance.backend.dto.response.RecordResponse;
import com.finance.backend.dto.response.UserSummaryResponse;
import com.finance.backend.entity.FinancialRecord;
import org.springframework.stereotype.Component;

@Component
public class RecordDtoMapper {

    public RecordResponse toResponse(FinancialRecord record) {
        if (record == null) {
            return null;
        }
        UserSummaryResponse createdBy = null;
        if (record.getCreatedBy() != null) {
            var u = record.getCreatedBy();
            createdBy = UserSummaryResponse.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .build();
        }
        return RecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .description(record.getDescription())
                .createdBy(createdBy)
                .createdAt(record.getCreatedAt())
                .build();
    }
}
