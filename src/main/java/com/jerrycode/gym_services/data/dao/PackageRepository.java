package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.Packages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Packages, Long> {
    boolean existsByName(String name);
    Optional<Packages> findByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
