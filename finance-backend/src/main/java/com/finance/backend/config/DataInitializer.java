package com.finance.backend.config;

import com.finance.backend.entity.FinancialRecord;
import com.finance.backend.entity.User;
import com.finance.backend.enums.Role;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.enums.UserStatus;
import com.finance.backend.repository.FinancialRecordRepository;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@finance.com";

    private final UserRepository userRepository;
    private final FinancialRecordRepository financialRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = ensureAdminUser();
        if (financialRecordRepository.countNonDeleted() == 0) {
            seedFinancialRecords(admin);
            log.info("Seeded sample financial records");
        }
    }

    private User ensureAdminUser() {
        return userRepository.findByEmail(ADMIN_EMAIL).orElseGet(() -> {
            User user = User.builder()
                    .name("System Admin")
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            User saved = userRepository.save(user);
            log.info("Created default admin user {}", ADMIN_EMAIL);
            return saved;
        });
    }

    private void seedFinancialRecords(User admin) {
        List<FinancialRecord> batch = new ArrayList<>();
        batch.add(record(admin, "5000.00", TransactionType.INCOME, "Salary", "2025-01-05", "Monthly salary"));
        batch.add(record(admin, "1200.00", TransactionType.EXPENSE, "Rent", "2025-01-07", "January rent"));
        batch.add(record(admin, "350.50", TransactionType.EXPENSE, "Food", "2025-01-12", "Groceries"));
        batch.add(record(admin, "800.00", TransactionType.INCOME, "Investment", "2025-01-20", "Dividend payout"));
        batch.add(record(admin, "5000.00", TransactionType.INCOME, "Salary", "2025-02-05", "Monthly salary"));
        batch.add(record(admin, "1200.00", TransactionType.EXPENSE, "Rent", "2025-02-07", "February rent"));
        batch.add(record(admin, "420.00", TransactionType.EXPENSE, "Food", "2025-02-14", "Dining out"));
        batch.add(record(admin, "5000.00", TransactionType.INCOME, "Salary", "2025-03-05", "Monthly salary"));
        batch.add(record(admin, "1200.00", TransactionType.EXPENSE, "Rent", "2025-03-07", "March rent"));
        batch.add(record(admin, "250.00", TransactionType.EXPENSE, "Utilities", "2025-03-15", "Electric bill"));
        batch.add(record(admin, "5000.00", TransactionType.INCOME, "Salary", "2025-04-01", "Monthly salary"));
        batch.add(record(admin, "900.00", TransactionType.EXPENSE, "Food", "2025-04-03", "Bulk groceries"));
        batch.add(record(admin, "1500.00", TransactionType.EXPENSE, "Investment", "2025-04-10", "Broker fees"));
        batch.add(record(admin, "300.00", TransactionType.INCOME, "Investment", "2025-04-18", "Interest income"));
        batch.add(record(admin, "200.00", TransactionType.EXPENSE, "Food", "2025-04-22", "Coffee and snacks"));
        financialRecordRepository.saveAll(batch);
    }

    private static FinancialRecord record(
            User admin,
            String amount,
            TransactionType type,
            String category,
            String date,
            String description) {
        return FinancialRecord.builder()
                .amount(new BigDecimal(amount))
                .type(type)
                .category(category)
                .date(LocalDate.parse(date))
                .description(description)
                .isDeleted(false)
                .createdBy(admin)
                .build();
    }
}
