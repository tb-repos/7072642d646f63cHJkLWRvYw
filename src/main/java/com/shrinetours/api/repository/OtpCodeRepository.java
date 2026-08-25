package com.shrinetours.api.repository;

import com.shrinetours.api.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, String> {
    Optional<OtpCode> findTopByEmailAndCodeAndConsumedFalseOrderByExpiresAtDesc(String email, String code);
    Optional<OtpCode> findTopByEmailAndConsumedFalseOrderByExpiresAtDesc(String email);
    List<OtpCode> findByEmailAndConsumedFalse(String email);
}
