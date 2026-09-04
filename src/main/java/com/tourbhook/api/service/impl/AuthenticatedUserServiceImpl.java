package com.tourbhook.api.service.impl;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.UserRepository;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.AuthenticatedUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserServiceImpl implements AuthenticatedUserService {

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return userRepository.findByEmailIgnoreCase("demo@tourbhook.com")
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        }

        String subject = authentication.getName();

        return userRepository.findById(subject)
                .or(() -> userRepository.findByEmailIgnoreCase(subject))
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}