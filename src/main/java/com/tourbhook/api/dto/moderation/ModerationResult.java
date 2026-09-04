package com.tourbhook.api.dto.moderation;

public record ModerationResult(boolean flagged, String reason) {

    public static ModerationResult clean() {
        return new ModerationResult(false, null);
    }

    public static ModerationResult flagged(String reason) {
        return new ModerationResult(true, reason);
    }
}