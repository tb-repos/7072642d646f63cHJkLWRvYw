package com.tourbhook.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "moderation")
public class ModerationProperties {
    private List<String> bannedTerms = new ArrayList<>();
    private List<String> blockedDomains = new ArrayList<>();
}