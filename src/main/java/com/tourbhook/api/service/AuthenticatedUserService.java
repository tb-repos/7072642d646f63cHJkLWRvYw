package com.tourbhook.api.service;

import com.tourbhook.api.entity.User;

public interface AuthenticatedUserService {
    User getCurrentUser();
}
