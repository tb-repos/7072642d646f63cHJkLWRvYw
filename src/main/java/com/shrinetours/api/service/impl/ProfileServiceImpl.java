package com.shrinetours.api.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shrinetours.api.dto.profile.AddPaymentMethodRequest;
import com.shrinetours.api.dto.profile.PaymentMethodResponse;
import com.shrinetours.api.dto.profile.ProfileResponse;
import com.shrinetours.api.dto.profile.StaticPageResponse;
import com.shrinetours.api.dto.profile.SubscriptionResponse;
import com.shrinetours.api.dto.profile.UpdateProfileRequest;
import com.shrinetours.api.dto.profile.UploadAvatarResponse;
import com.shrinetours.api.entity.PaymentMethod;
import com.shrinetours.api.entity.StaticPage;
import com.shrinetours.api.entity.Subscription;
import com.shrinetours.api.entity.User;
import com.shrinetours.api.repository.PaymentMethodRepository;
import com.shrinetours.api.repository.StaticPageRepository;
import com.shrinetours.api.repository.SubscriptionRepository;
import com.shrinetours.api.repository.UserRepository;
import com.shrinetours.api.repository.exception.BadRequestException;
import com.shrinetours.api.repository.exception.ResourceNotFoundException;
import com.shrinetours.api.service.AuthenticatedUserService;
import com.shrinetours.api.service.FileService;
import com.shrinetours.api.service.ProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StaticPageRepository staticPageRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final FileService fileService;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/webp",
            "image/gif",
            "image/bmp"
    );

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile() {
        return mapProfile(currentUser());
    }

    @Override
    public ProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser();

        if (request.name() != null) {
            user.setName(request.name().trim());
        }

        if (request.email() != null) {
            String normalizedEmail = request.email().trim().toLowerCase();
            userRepository.findByEmailIgnoreCase(normalizedEmail)
                    .filter(existingUser -> !existingUser.getId().equals(user.getId()))
                    .ifPresent(existingUser -> {
                        throw new BadRequestException("Email already registered");
                    });
            user.setEmail(normalizedEmail);
        }

        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }

        if (request.dob() != null) {
            user.setDob(request.dob());
        }

        return mapProfile(userRepository.save(user));
    }

    @Override
    public UploadAvatarResponse uploadAvatar(MultipartFile file) {
        User user = currentUser();

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only image files are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size must be less than 5 MB");
        }

        try {
            String avatarUrl = fileService.uploadAvatar(file);

            user.setAvatarUrl(avatarUrl);
            User savedUser = userRepository.save(user);

            return new UploadAvatarResponse(savedUser.getAvatarUrl());

        } catch (Exception e) {
            throw new BadRequestException("Failed to upload avatar");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getPayments() {
        return paymentMethodRepository.findByUserOrderByPrimaryMethodDescCreatedAtDesc(currentUser())
                .stream()
                .map(this::mapPayment)
                .toList();
    }

    @Override
    public PaymentMethodResponse addPaymentMethod(AddPaymentMethodRequest request) {
        User user = currentUser();
        String digitsOnly = request.card_number().replaceAll("\\s+", "");
        if (digitsOnly.length() < 4) {
            throw new BadRequestException("Card number must contain at least 4 digits");
        }

        boolean setPrimary = Boolean.TRUE.equals(request.is_primary())
                || paymentMethodRepository.findByUserOrderByPrimaryMethodDescCreatedAtDesc(user).isEmpty();

        if (setPrimary) {
            clearPrimaryForUser(user);
        }

        PaymentMethod paymentMethod = paymentMethodRepository.save(
                PaymentMethod.builder()
                        .user(user)
                        .type(request.type().trim().toUpperCase())
                        .lastFour(digitsOnly.substring(digitsOnly.length() - 4))
                        .holderName(request.holder_name().trim())
                        .expiry(request.expiry().trim())
                        .primaryMethod(setPrimary)
                        .build()
        );

        return mapPayment(paymentMethod);
    }

    @Override
    public List<PaymentMethodResponse> setPrimaryPaymentMethod(String paymentMethodId) {
        User user = currentUser();
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUser(paymentMethodId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        clearPrimaryForUser(user);
        paymentMethod.setPrimaryMethod(true);
        paymentMethodRepository.save(paymentMethod);
        return getPayments();
    }

    @Override
    public List<PaymentMethodResponse> deletePaymentMethod(String paymentMethodId) {
        User user = currentUser();
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndUser(paymentMethodId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        boolean wasPrimary = Boolean.TRUE.equals(paymentMethod.getPrimaryMethod());
        paymentMethodRepository.delete(paymentMethod);

        List<PaymentMethod> remaining = paymentMethodRepository.findByUserOrderByPrimaryMethodDescCreatedAtDesc(user);
        if (wasPrimary && !remaining.isEmpty()) {
            remaining.get(0).setPrimaryMethod(true);
            paymentMethodRepository.save(remaining.get(0));
        }
        return getPayments();
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription() {
        Subscription s = subscriptionRepository.findByUser(currentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        return new SubscriptionResponse(s.getPlan(), s.getStatus(), s.getRenewsAt(), s.getFeatures());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaticPageResponse> getPages() {
        return staticPageRepository.findAll().stream().map(this::mapPage).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StaticPageResponse getPage(String slug) {
        return staticPageRepository.findBySlug(slug)
                .map(this::mapPage)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));
    }

    @Override
    public Map<String, Object> deleteAccount() {
        User user = currentUser();
        user.setDeleted(true);
        userRepository.save(user);
        return Map.of("ok", true, "message", "Account deleted successfully");
    }

    private void clearPrimaryForUser(User user) {
        List<PaymentMethod> methods = paymentMethodRepository.findByUserOrderByPrimaryMethodDescCreatedAtDesc(user);
        for (PaymentMethod method : methods) {
            if (Boolean.TRUE.equals(method.getPrimaryMethod())) {
                method.setPrimaryMethod(false);
            }
        }
        paymentMethodRepository.saveAll(methods);
    }

    private User currentUser() {
        User authUser = authenticatedUserService.getCurrentUser();

        return userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProfileResponse mapProfile(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getDob(),
                user.getAvatarUrl(),
                user.getLevel(),
                user.getLevelProgress(),
                user.getTripsCompleted(),
                user.isPremium()
        );
    }

    private PaymentMethodResponse mapPayment(PaymentMethod p) {
        return new PaymentMethodResponse(
                p.getId(),
                p.getType(),
                p.getLastFour(),
                p.getHolderName(),
                p.getExpiry(),
                p.getPrimaryMethod()
        );
    }

    private StaticPageResponse mapPage(StaticPage p) {
        return new StaticPageResponse(p.getSlug(), p.getTitle(), p.getContent());
    }
}