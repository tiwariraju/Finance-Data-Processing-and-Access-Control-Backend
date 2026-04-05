package com.finance.backend.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageableSortUtil {

    private static final Set<String> RECORD_SORT_FIELDS = Set.of(
            "date", "amount", "category", "type", "createdAt", "id");

    private PageableSortUtil() {
    }

    public static Pageable recordPageable(int page, int size, String sortBy, String sortDirection) {
        String field = RECORD_SORT_FIELDS.contains(sortBy) ? sortBy : "date";
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(dir, field));
    }
}
