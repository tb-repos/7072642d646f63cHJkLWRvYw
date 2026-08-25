package com.shrinetours.api.service.impl;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shrinetours.api.entity.User;
import com.shrinetours.api.repository.UserRepository;
import com.shrinetours.api.repository.exception.ResourceNotFoundException;
import com.shrinetours.api.service.AuthenticatedUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return userRepository.findByEmailIgnoreCase("demo@shrinetours.com")
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        }

        String subject = authentication.getName();

        return userRepository.findById(subject)
                .or(() -> userRepository.findByEmailIgnoreCase(subject))
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}