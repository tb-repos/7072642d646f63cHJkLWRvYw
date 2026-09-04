package com.tourbhook.api.controller;

import com.tourbhook.api.dto.common.ApiResponse;
import com.tourbhook.api.dto.language.LanguagePreferenceResponse;
import com.tourbhook.api.dto.language.SupportedLanguageResponse;
import com.tourbhook.api.dto.language.UpdateLanguagePreferenceRequest;
import com.tourbhook.api.service.LanguageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/languages")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupportedLanguageResponse>>> getSupportedLanguages() {
        return ResponseEntity.ok(
                ApiResponse.<List<SupportedLanguageResponse>>builder()
                        .success(true)
                        .message("Supported languages fetched successfully")
                        .data(languageService.getSupportedLanguages())
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/languages")
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LanguagePreferenceResponse>> getMyLanguagePreference() {
        return ResponseEntity.ok(
                ApiResponse.<LanguagePreferenceResponse>builder()
                        .success(true)
                        .message("Language preference fetched successfully")
                        .data(languageService.getMyLanguagePreference())
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/languages/me")
                        .build()
        );
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<LanguagePreferenceResponse>> updateMyLanguagePreference(
            @Valid @RequestBody UpdateLanguagePreferenceRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<LanguagePreferenceResponse>builder()
                        .success(true)
                        .message("Language preference updated successfully")
                        .data(languageService.updateMyLanguagePreference(request.code()))
                        .timestamp(LocalDateTime.now())
                        .path("/api/v1/languages/me")
                        .build()
        );
    }
}