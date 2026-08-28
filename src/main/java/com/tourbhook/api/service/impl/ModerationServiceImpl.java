package com.tourbhook.api.service.impl;

import com.tourbhook.api.config.ModerationProperties;
import com.tourbhook.api.dto.moderation.ModerationResult;
import com.tourbhook.api.service.ModerationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
public class ModerationServiceImpl implements ModerationService {

    private final Set<String> bannedTerms;
    private final Set<String> blockedDomains;

    public ModerationServiceImpl(ModerationProperties properties) {
        this.bannedTerms = normalize(properties.getBannedTerms());
        this.blockedDomains = normalize(properties.getBlockedDomains());

        log.info("Moderation service started with {} banned term(s) and {} blocked domain(s)",
                this.bannedTerms.size(), this.blockedDomains.size());
    }

    private Set<String> normalize(List<String> values) {
        return values.stream()
                .map(value -> value.toLowerCase(Locale.ROOT).trim())
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public ModerationResult checkText(String content) {
        if (content == null || content.isBlank()) {
            return ModerationResult.clean();
        }

        String normalized = content.toLowerCase(Locale.ROOT);
        for (String term : bannedTerms) {
            if (normalized.contains(term)) {
                return ModerationResult.flagged("Text matched a banned term");
            }
        }
        return ModerationResult.clean();
    }

    @Override
    public ModerationResult checkImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ModerationResult.clean();
        }
        return ModerationResult.clean();
    }

    @Override
    public ModerationResult checkLink(String url) {
        if (url == null || url.isBlank()) {
            return ModerationResult.clean();
        }

        String host;
        try {
            host = new URI(url.trim()).getHost();
        } catch (URISyntaxException e) {
            return ModerationResult.flagged("Malformed URL");
        }

        if (host == null) {
            return ModerationResult.flagged("Malformed URL");
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        boolean matchesBlockedDomain = blockedDomains.stream()
                .anyMatch(blocked -> normalizedHost.equals(blocked) || normalizedHost.endsWith("." + blocked));

        return matchesBlockedDomain
                ? ModerationResult.flagged("Link points to a blocked domain: " + normalizedHost)
                : ModerationResult.clean();
    }
}