package com.tourbhook.api.service;

import com.tourbhook.api.entity.User;

public interface AccountEnforcementService {

    void blockPermanently(User user, String reason);
}