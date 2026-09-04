package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.LanguageProperties;
import com.tourbhook.api.dto.language.LanguagePreferenceResponse;
import com.tourbhook.api.dto.language.SupportedLanguageResponse;
import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.UserRepository;
import com.tourbhook.api.repository.exception.BadRequestException;
import com.tourbhook.api.service.AuthenticatedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private UserRepository userRepository;

    private LanguageProperties languageProperties;
    private LanguageServiceImpl languageService;
    private User user;

    @BeforeEach
    void setUp() {
        languageProperties = new LanguageProperties();
        languageProperties.setSupported(List.of(
                entry("en", "English", "English", 1),
                entry("de", "German", "Deutsch", 2),
                entry("kn", "Kannada", "ಕನ್ನಡ", 2)
        ));
        languageService = new LanguageServiceImpl(languageProperties, authenticatedUserService, userRepository);

        user = User.builder().id("user-1").preferredLanguage("en").build();
        lenient().when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        lenient().when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
    }

    private LanguageProperties.Entry entry(String code, String englishName, String nativeName, int tier) {
        LanguageProperties.Entry entry = new LanguageProperties.Entry();
        entry.setCode(code);
        entry.setEnglishName(englishName);
        entry.setNativeName(nativeName);
        entry.setTier(tier);
        return entry;
    }

    @Test
    void getSupportedLanguages_returnsFullCatalogInOrder() {
        List<SupportedLanguageResponse> result = languageService.getSupportedLanguages();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(SupportedLanguageResponse::code).containsExactly("en", "de", "kn");
        assertThat(result.get(2).nativeName()).isEqualTo("ಕನ್ನಡ");
        assertThat(result.get(2).tier()).isEqualTo(2);
    }

    @Test
    void getMyLanguagePreference_returnsCurrentUsersLanguage() {
        LanguagePreferenceResponse response = languageService.getMyLanguagePreference();

        assertThat(response.code()).isEqualTo("en");
        assertThat(response.englishName()).isEqualTo("English");
    }

    @Test
    void updateMyLanguagePreference_toSupportedLanguage_updatesAndReturnsIt() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LanguagePreferenceResponse response = languageService.updateMyLanguagePreference("de");

        assertThat(response.code()).isEqualTo("de");
        assertThat(response.nativeName()).isEqualTo("Deutsch");
        assertThat(user.getPreferredLanguage()).isEqualTo("de");
        verify(userRepository).save(user);
    }

    @Test
    void updateMyLanguagePreference_isCaseInsensitiveAndTrims() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LanguagePreferenceResponse response = languageService.updateMyLanguagePreference("  DE  ");

        assertThat(response.code()).isEqualTo("de"); // normalized to the catalog's stored casing
    }

    @Test
    void updateMyLanguagePreference_toUnsupportedLanguage_throwsBadRequest() {
        assertThatThrownBy(() -> languageService.updateMyLanguagePreference("fr"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("fr");

        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
    }

    @Test
    void updateMyLanguagePreference_withBlankCode_throwsBadRequest() {
        assertThatThrownBy(() -> languageService.updateMyLanguagePreference(""))
                .isInstanceOf(BadRequestException.class);
    }
}