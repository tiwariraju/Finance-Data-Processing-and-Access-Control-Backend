package com.finance.backend.service.impl;

import com.finance.backend.dto.response.CategorySummaryResponse;
import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.RecordResponse;
import com.finance.backend.dto.response.TrendResponse;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.repository.FinancialRecordRepository;
import com.finance.backend.service.DashboardService;
import com.finance.backend.util.RecordDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository financialRecordRepository;
    private final RecordDtoMapper recordDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        BigDecimal income = financialRecordRepository.sumAmountByType(TransactionType.INCOME);
        BigDecimal expenses = financialRecordRepository.sumAmountByType(TransactionType.EXPENSE);
        if (income == null) {
            income = BigDecimal.ZERO;
        }
        if (expenses == null) {
            expenses = BigDecimal.ZERO;
        }
        long count = financialRecordRepository.countNonDeleted();
        return DashboardSummaryResponse.builder()
                .totalIncome(income)
                .totalExpenses(expenses)
                .netBalance(income.subtract(expenses))
                .totalRecords(count)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCategoryBreakdown() {
        List<Object[]> rows = financialRecordRepository.getCategoryBreakdownRaw();
        Map<String, BigDecimal[]> totals = new HashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            TransactionType type = (TransactionType) row[1];
            BigDecimal sum = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            BigDecimal[] pair = totals.computeIfAbsent(
                    category, c -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (type == TransactionType.INCOME) {
                pair[0] = pair[0].add(sum);
            } else {
                pair[1] = pair[1].add(sum);
            }
        }
        List<CategorySummaryResponse> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : totals.entrySet()) {
            result.add(CategorySummaryResponse.builder()
                    .category(e.getKey())
                    .totalIncome(e.getValue()[0])
                    .totalExpense(e.getValue()[1])
                    .build());
        }
        result.sort(Comparator.comparing(CategorySummaryResponse::getCategory));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendResponse> getTrends(String period) {
        List<Object[]> raw = "yearly".equalsIgnoreCase(period)
                ? financialRecordRepository.getYearlyTrendsRaw()
                : financialRecordRepository.getMonthlyTrendsRaw();
        Map<String, BigDecimal[]> totals = new HashMap<>();
        for (Object[] row : raw) {
            String p = String.valueOf(row[0]);
            String typeStr = String.valueOf(row[1]).trim();
            BigDecimal sum = toBigDecimal(row[2]);
            TransactionType type;
            try {
                type = TransactionType.valueOf(typeStr);
            } catch (IllegalArgumentException ex) {
                log.warn("Skipping trend row with unknown type: {}", typeStr);
                continue;
            }
            BigDecimal[] pair = totals.computeIfAbsent(
                    p, key -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (type == TransactionType.INCOME) {
                pair[0] = pair[0].add(sum);
            } else {
                pair[1] = pair[1].add(sum);
            }
        }
        List<TrendResponse> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : totals.entrySet()) {
            result.add(TrendResponse.builder()
                    .period(e.getKey())
                    .totalIncome(e.getValue()[0])
                    .totalExpense(e.getValue()[1])
                    .build());
        }
        result.sort(Comparator.comparing(TrendResponse::getPeriod));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecordResponse> getRecentRecords(int limit) {
        int safe = Math.min(Math.max(limit, 1), 100);
        var pageable = PageRequest.of(0, safe);
        return financialRecordRepository.findRecentNonDeleted(pageable).stream()
                .map(recordDtoMapper::toResponse)
                .toList();
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(value.toString());
    }
}
