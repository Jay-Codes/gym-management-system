package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.Invoices;
import com.jerrycode.gym_services.data.vo.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoices, Long> {
    List<Invoices> findAll(Sort sort);
    @Query("SELECT COUNT(DISTINCT i.memberId) FROM Invoices i WHERE i.paid = true")
    long countDistinctMemberByPaidTrue();
    @Query("SELECT COUNT(DISTINCT i.memberId) FROM Invoices i WHERE i.paid = true AND i.createdAt BETWEEN :start AND :end")
    long countDistinctMemberByPaidTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    @Query("SELECT SUM(i.amountPaid) FROM Invoices i WHERE i.createdAt BETWEEN :start AND :end")
    Double sumAmountPaidByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByDiscountPercentageGreaterThan(double value);
    Long countByCreatedAtBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
    List<Invoices> findByMemberId(Long memberId);
    List<Invoices> findByStatus(String status);
    List<Invoices> findByStatusAndCreatedAtAfter(String status, LocalDateTime createdAt);
    List<Invoices> findByEndDateBeforeAndStatusNot(LocalDate endDate, String status);

    List<Invoices> findByCreatedAtBetween(LocalDateTime atStartOfDay, LocalDateTime atStartOfDay1);
}
