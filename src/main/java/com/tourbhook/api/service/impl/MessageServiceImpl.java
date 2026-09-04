package com.tourbhook.api.service.impl;

import com.tourbhook.api.entity.User;
import com.tourbhook.api.service.AuthenticatedUserService;
import com.tourbhook.api.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final String DEFAULT_LANGUAGE = "en";

    private final MessageSource messageSource;
    private final AuthenticatedUserService authenticatedUserService;

    @Override
    public String get(String key, Object... args) {
        return getForLanguage(key, resolveCurrentUserLanguage(), args);
    }

    @Override
    public String getForLanguage(String key, String languageCode, Object... args) {
        Locale locale = toLocale(languageCode);
        Object[] resolvedArgs = (args == null || args.length == 0) ? null : args;
        try {
            return messageSource.getMessage(key, resolvedArgs, locale);
        } catch (NoSuchMessageException e) {
            log.warn("Missing translation key '{}' for locale '{}'", key, locale);
            return key;
        }
    }

    private Locale toLocale(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return Locale.forLanguageTag(DEFAULT_LANGUAGE);
        }
        return Locale.forLanguageTag(languageCode.trim());
    }

    private String resolveCurrentUserLanguage() {
        try {
            User user = authenticatedUserService.getCurrentUser();
            return user.getPreferredLanguage() != null ? user.getPreferredLanguage() : DEFAULT_LANGUAGE;
        } catch (Exception e) {
            return DEFAULT_LANGUAGE;
        }
    }
}