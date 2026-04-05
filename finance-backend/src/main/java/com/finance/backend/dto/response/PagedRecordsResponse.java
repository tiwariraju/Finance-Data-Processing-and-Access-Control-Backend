package com.finance.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedRecordsResponse {

    private List<RecordResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
