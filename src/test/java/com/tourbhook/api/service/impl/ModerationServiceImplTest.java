package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.moderation.ModerationResult;
import com.tourbhook.api.config.ModerationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

class ModerationServiceImplTest {

    private final ModerationServiceImpl moderationService = new ModerationServiceImpl(testProperties());

    private static ModerationProperties testProperties() {
        ModerationProperties properties = new ModerationProperties();
        properties.setBannedTerms(List.of("scam", "spam-link"));
        properties.setBlockedDomains(List.of("malicious-example.test"));
        return properties;
    }

    @Test
    void checkText_withCleanContent_isNotFlagged() {
        ModerationResult result = moderationService.checkText("Loved this temple, highly recommend visiting at sunrise");
        assertThat(result.flagged()).isFalse();
    }

    @Test
    void checkText_withBannedTerm_isFlaggedRegardlessOfCase() {
        ModerationResult result = moderationService.checkText("This place is a total SCAM, don't go");
        assertThat(result.flagged()).isTrue();
        assertThat(result.reason()).isNotBlank();
    }

    @Test
    void checkText_withNullOrBlank_isNotFlagged() {
        assertThat(moderationService.checkText(null).flagged()).isFalse();
        assertThat(moderationService.checkText("   ").flagged()).isFalse();
    }

    @Test
    void checkLink_toBlockedDomain_isFlagged() {
        ModerationResult result = moderationService.checkLink("https://malicious-example.test/offer");
        assertThat(result.flagged()).isTrue();
    }

    @Test
    void checkLink_toBlockedSubdomain_isFlagged() {
        ModerationResult result = moderationService.checkLink("https://sub.malicious-example.test/offer");
        assertThat(result.flagged()).isTrue();
    }

    @Test
    void checkLink_toAllowedDomain_isNotFlagged() {
        ModerationResult result = moderationService.checkLink("https://en.wikipedia.org/wiki/Madurai");
        assertThat(result.flagged()).isFalse();
    }

    @Test
    void checkLink_malformed_isFlagged() {
        ModerationResult result = moderationService.checkLink("ht!tp://not a url");
        assertThat(result.flagged()).isTrue();
    }

    @Test
    void checkLink_nullOrBlank_isNotFlagged() {
        assertThat(moderationService.checkLink(null).flagged()).isFalse();
        assertThat(moderationService.checkLink("").flagged()).isFalse();
    }

    @Test
    void checkImage_emptyFile_isNotFlagged() {
        MockMultipartFile empty = new MockMultipartFile("file", new byte[0]);
        assertThat(moderationService.checkImage(empty).flagged()).isFalse();
    }

    @Test
    void checkImage_currentlyNeverFlagsContent_becauseNoVisionVendorIsWiredInYet() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "bytes".getBytes());
        assertThat(moderationService.checkImage(file).flagged()).isFalse();
    }
}