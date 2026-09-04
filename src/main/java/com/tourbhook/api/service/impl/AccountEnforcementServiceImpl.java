package com.tourbhook.api.service.impl;

import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.UserRepository;
import com.tourbhook.api.service.AccountEnforcementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountEnforcementServiceImpl implements AccountEnforcementService {

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void blockPermanently(User user, String reason) {
        if (user.isBlocked()) {
            return;
        }

        user.setBlocked(true);
        user.setBlockedReason(reason);
        user.setBlockedAt(Instant.now());
        userRepository.save(user);

        log.warn("PERMANENT BLOCK applied to user {} — reason: {}", user.getId(), reason);
    }
}