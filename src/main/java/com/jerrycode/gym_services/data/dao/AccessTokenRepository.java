package com.jerrycode.gym_services.data.dao;

import com.jerrycode.gym_services.data.vo.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByToken(String token);
    List<AccessToken> findByUserIdAndRevokedFalse(Long userId);
    @Modifying
    @Query("DELETE FROM AccessToken t WHERE t.expiresAt < ?1 AND t.expiresAt IS NOT NULL")
    void deleteExpiredTokens(Instant now);
    @Modifying
    @Query("DELETE FROM AccessToken t WHERE t.user.id = ?1 AND t.revoked = false")
    void deleteByUserIdAndRevokedFalse(Long userId);
}
