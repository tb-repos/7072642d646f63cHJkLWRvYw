package com.tourbhook.api.service;

import com.tourbhook.api.dto.moderation.ModerationResult;
import org.springframework.web.multipart.MultipartFile;

public interface ModerationService {

    ModerationResult checkText(String content);

    ModerationResult checkImage(MultipartFile file);

    ModerationResult checkLink(String url);
}