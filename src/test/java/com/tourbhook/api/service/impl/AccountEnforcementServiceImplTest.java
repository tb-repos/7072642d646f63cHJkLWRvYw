package com.tourbhook.api.service.impl;

import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountEnforcementServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountEnforcementServiceImpl accountEnforcementService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id("user-1").blocked(false).build();
    }

    @Test
    void blockPermanently_onFirstOffense_blocksImmediatelyWithNoIntermediateState() {
        accountEnforcementService.blockPermanently(user, "Flagged content");

        assertThat(user.isBlocked()).isTrue();
        assertThat(user.getBlockedReason()).isEqualTo("Flagged content");
        assertThat(user.getBlockedAt()).isNotNull();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void blockPermanently_calledTwice_isIdempotentAndDoesNotOverwriteTheOriginalRecord() {
        accountEnforcementService.blockPermanently(user, "First violation");
        accountEnforcementService.blockPermanently(user, "A different reason on a second call");

        assertThat(user.getBlockedReason()).isEqualTo("First violation");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void thereIsNoWarningMethodOnThisService() {
        assertThat(AccountEnforcementServiceImpl.class.getDeclaredMethods())
                .extracting(java.lang.reflect.Method::getName)
                .doesNotContain("warn", "flagForReview", "issueStrike");
    }
}