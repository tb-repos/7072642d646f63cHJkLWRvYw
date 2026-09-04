package com.tourbhook.api.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "language")
public class LanguageProperties {

    private List<Entry> supported = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Entry {
        private String code;
        private String englishName;
        private String nativeName;
        private int tier;
    }
}