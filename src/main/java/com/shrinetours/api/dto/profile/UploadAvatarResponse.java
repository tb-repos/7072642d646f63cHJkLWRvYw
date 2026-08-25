package com.shrinetours.api.dto.profile;


public class UploadAvatarResponse {

    private String avatarUrl;

    public UploadAvatarResponse() {
    }

    public UploadAvatarResponse(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
