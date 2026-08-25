package com.shrinetours.api.service;

import com.shrinetours.api.entity.User;

public interface AuthenticatedUserService {
    User getCurrentUser();
}
