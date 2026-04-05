package com.finance.backend.controller;

import com.finance.backend.dto.ApiResponse;
import com.finance.backend.dto.response.CategorySummaryResponse;
import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.DataListResponse;
import com.finance.backend.dto.response.RecordResponse;
import com.finance.backend.dto.response.TrendResponse;
import com.finance.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ANALYST','ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> summary() {
        DashboardSummaryResponse data = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<DataListResponse<CategorySummaryResponse>>> byCategory() {
        List<CategorySummaryResponse> list = dashboardService.getCategoryBreakdown();
        return ResponseEntity.ok(ApiResponse.ok(DataListResponse.<CategorySummaryResponse>builder().data(list).build()));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<DataListResponse<TrendResponse>>> trends(
            @RequestParam(defaultValue = "monthly") String period) {
        List<TrendResponse> list = dashboardService.getTrends(period);
        return ResponseEntity.ok(ApiResponse.ok(DataListResponse.<TrendResponse>builder().data(list).build()));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<DataListResponse<RecordResponse>>> recent(
            @RequestParam(defaultValue = "5") int limit) {
        List<RecordResponse> list = dashboardService.getRecentRecords(limit);
        return ResponseEntity.ok(ApiResponse.ok(DataListResponse.<RecordResponse>builder().data(list).build()));
    }
}
