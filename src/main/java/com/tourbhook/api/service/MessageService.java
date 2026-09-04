package com.tourbhook.api.service;

public interface MessageService {

    String get(String key, Object... args);

    String getForLanguage(String key, String languageCode, Object... args);
}