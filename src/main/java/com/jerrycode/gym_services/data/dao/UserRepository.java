package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.Member;
import com.jerrycode.gym_services.data.vo.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll(Sort sort);
    Optional<User> findByName(String name);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNUmber, Long id);
}
