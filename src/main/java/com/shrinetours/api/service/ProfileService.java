package com.shrinetours.api.service;

import com.shrinetours.api.dto.profile.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProfileService {
    ProfileResponse getProfile();
    ProfileResponse updateProfile(UpdateProfileRequest request);
    UploadAvatarResponse uploadAvatar(MultipartFile file);
    List<PaymentMethodResponse> getPayments();
    PaymentMethodResponse addPaymentMethod(AddPaymentMethodRequest request);
    List<PaymentMethodResponse> setPrimaryPaymentMethod(String paymentMethodId);
    List<PaymentMethodResponse> deletePaymentMethod(String paymentMethodId);
    SubscriptionResponse getSubscription();
    List<StaticPageResponse> getPages();
    StaticPageResponse getPage(String slug);
    Map<String, Object> deleteAccount();
}