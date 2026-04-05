package com.finance.backend.service;

import com.finance.backend.dto.response.CategorySummaryResponse;
import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.RecordResponse;
import com.finance.backend.dto.response.TrendResponse;

import java.util.List;

/**
 * Aggregations and summaries for analyst/admin dashboards.
 */
public interface DashboardService {

    /**
     * Overall totals and record count (non-deleted only).
     */
    DashboardSummaryResponse getSummary();

    /**
     * Per-category income and expense totals.
     */
    List<CategorySummaryResponse> getCategoryBreakdown();

    /**
     * Time-bucketed trends; {@code period} is typically {@code monthly} or {@code yearly}.
     */
    List<TrendResponse> getTrends(String period);

    /**
     * Most recent non-deleted records ordered by date (then id).
     */
    List<RecordResponse> getRecentRecords(int limit);
}
