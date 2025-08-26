package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.Member;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findAll(Sort sort);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
}
