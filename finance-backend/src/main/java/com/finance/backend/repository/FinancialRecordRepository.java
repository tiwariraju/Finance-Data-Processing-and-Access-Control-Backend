package com.finance.backend.repository;

import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.enums.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>,
        JpaSpecificationExecutor<FinancialRecord> {

    @Query("SELECT r FROM FinancialRecord r JOIN FETCH r.createdBy WHERE r.id = :id AND r.isDeleted = false")
    Optional<FinancialRecord> findActiveByIdWithCreatedBy(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type AND r.isDeleted = false")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT COUNT(r) FROM FinancialRecord r WHERE r.isDeleted = false")
    long countNonDeleted();

    @Query("SELECT r.category, r.type, COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.isDeleted = false GROUP BY r.category, r.type")
    List<Object[]> getCategoryBreakdownRaw();

    @Query(value = """
            SELECT DATE_FORMAT(r.date, '%Y-%m') AS period, r.type, SUM(r.amount)
            FROM financial_records r
            WHERE r.is_deleted = 0
            GROUP BY period, r.type
            ORDER BY period
            """, nativeQuery = true)
    List<Object[]> getMonthlyTrendsRaw();

    @Query(value = """
            SELECT DATE_FORMAT(r.date, '%Y') AS period, r.type, SUM(r.amount)
            FROM financial_records r
            WHERE r.is_deleted = 0
            GROUP BY period, r.type
            ORDER BY period
            """, nativeQuery = true)
    List<Object[]> getYearlyTrendsRaw();

    @Query("SELECT r FROM FinancialRecord r JOIN FETCH r.createdBy WHERE r.isDeleted = false ORDER BY r.date DESC, r.id DESC")
    List<FinancialRecord> findRecentNonDeleted(Pageable pageable);
}
