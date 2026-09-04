package com.tourbhook.api.service;

import com.tourbhook.api.dto.language.LanguagePreferenceResponse;
import com.tourbhook.api.dto.language.SupportedLanguageResponse;

import java.util.List;

public interface LanguageService {

    List<SupportedLanguageResponse> getSupportedLanguages();

    LanguagePreferenceResponse getMyLanguagePreference();

    LanguagePreferenceResponse updateMyLanguagePreference(String code);
}