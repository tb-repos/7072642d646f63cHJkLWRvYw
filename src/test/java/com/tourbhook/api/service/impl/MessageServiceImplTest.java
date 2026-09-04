package com.tourbhook.api.service.impl;

import com.tourbhook.api.entity.User;
import com.tourbhook.api.service.AuthenticatedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n-test/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(false);

        messageService = new MessageServiceImpl(messageSource, authenticatedUserService);
    }

    @Test
    void get_usesCurrentUsersPreferredLanguage() {
        User user = User.builder().id("u1").preferredLanguage("de").build();
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);

        assertThat(messageService.get("greeting")).isEqualTo("Hallo");
    }

    @Test
    void get_whenNoAuthenticatedUser_fallsBackToEnglish() {
        when(authenticatedUserService.getCurrentUser()).thenThrow(new RuntimeException("no auth context"));

        assertThat(messageService.get("greeting")).isEqualTo("Hello");
    }

    @Test
    void getForLanguage_resolvesExplicitLanguageRegardlessOfCurrentUser() {
        assertThat(messageService.getForLanguage("greeting", "de")).isEqualTo("Hallo");
        assertThat(messageService.getForLanguage("greeting", "en")).isEqualTo("Hello");
    }

    @Test
    void getForLanguage_substitutesArguments() {
        String result = messageService.getForLanguage("otp-message", "en", "123456");
        assertThat(result).isEqualTo("Your code is 123456");
    }

    @Test
    void getForLanguage_withMissingKey_fallsBackToTheKeyItselfRatherThanThrowing() {
        String result = messageService.getForLanguage("this.key.does.not.exist", "en");
        assertThat(result).isEqualTo("this.key.does.not.exist");
    }

    @Test
    void getForLanguage_missingInTargetLanguageButPresentInEnglish_fallsBackToEnglish() {
        String result = messageService.getForLanguage("english-only-key", "de");
        assertThat(result).isEqualTo("English only");
    }

    @Test
    void getForLanguage_withZeroArgs_doesNotMangleLiteralApostrophes() {
        String result = messageService.getForLanguage("message-with-apostrophe", "en");
        assertThat(result).isEqualTo("Traveler's guide");
    }

    @Test
    void toLocale_handlesRajasthaniNonStandardCode() {
        String result = messageService.getForLanguage("greeting", "raj");
        assertThat(result).isNotNull();
    }
}