package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    boolean existsByName(String name);
    List<Discount> findByActiveTrue();

    boolean existsByNameAndIdNot(String name, Long id);
}
