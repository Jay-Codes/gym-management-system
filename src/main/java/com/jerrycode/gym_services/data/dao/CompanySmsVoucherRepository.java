package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.CompanySmsVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanySmsVoucherRepository extends JpaRepository<CompanySmsVoucher,Long> {

    Optional<CompanySmsVoucher> findByCompanyName(String companyName);
}
