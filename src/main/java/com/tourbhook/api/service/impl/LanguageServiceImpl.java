package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.LanguageProperties;
import com.tourbhook.api.dto.language.LanguagePreferenceResponse;
import com.tourbhook.api.dto.language.SupportedLanguageResponse;
import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.UserRepository;
import com.tourbhook.api.repository.exception.BadRequestException;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.AuthenticatedUserService;
import com.tourbhook.api.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LanguageServiceImpl implements LanguageService {

    private final LanguageProperties languageProperties;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SupportedLanguageResponse> getSupportedLanguages() {
        return languageProperties.getSupported().stream()
                .map(entry -> new SupportedLanguageResponse(
                        entry.getCode(), entry.getEnglishName(), entry.getNativeName(), entry.getTier()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LanguagePreferenceResponse getMyLanguagePreference() {
        return toPreferenceResponse(currentUser().getPreferredLanguage());
    }

    @Override
    public LanguagePreferenceResponse updateMyLanguagePreference(String code) {
        LanguageProperties.Entry entry = findSupportedEntry(code)
                .orElseThrow(() -> new BadRequestException(
                        "Unsupported language code: " + code + ". See GET /api/v1/languages for the supported list."));

        User user = currentUser();
        user.setPreferredLanguage(entry.getCode());
        userRepository.save(user);

        return new LanguagePreferenceResponse(entry.getCode(), entry.getEnglishName(), entry.getNativeName());
    }

    private LanguagePreferenceResponse toPreferenceResponse(String code) {
        return findSupportedEntry(code)
                .map(entry -> new LanguagePreferenceResponse(entry.getCode(), entry.getEnglishName(), entry.getNativeName()))
                .orElseGet(() -> new LanguagePreferenceResponse(code, code, code));
    }

    private Optional<LanguageProperties.Entry> findSupportedEntry(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return languageProperties.getSupported().stream()
                .filter(entry -> entry.getCode().equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    private User currentUser() {
        User authUser = authenticatedUserService.getCurrentUser();
        return userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}