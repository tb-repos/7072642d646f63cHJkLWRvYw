package com.shrinetours.api.controller;

import com.shrinetours.api.dto.common.ApiResponse;
import com.shrinetours.api.dto.profile.*;
import com.shrinetours.api.service.FileService;
import com.shrinetours.api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;
	private final FileService fileService;

	@GetMapping
	public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
		ProfileResponse response = profileService.getProfile();
		return ResponseEntity
				.ok(ApiResponse.<ProfileResponse>builder().success(true).message("Profile fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/profile").build());
	}

	@PutMapping("/update")
	public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
			@Valid @RequestBody UpdateProfileRequest request) {
		ProfileResponse response = profileService.updateProfile(request);
		return ResponseEntity
				.ok(ApiResponse.<ProfileResponse>builder().success(true).message("Profile updated successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/profile/update").build());
	}
	@PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<UploadAvatarResponse>> uploadAvatar(
	        @RequestParam("file") MultipartFile file) {

	    UploadAvatarResponse response = profileService.uploadAvatar(file);

	    return ResponseEntity.ok(
	            ApiResponse.<UploadAvatarResponse>builder()
	                    .success(true)
	                    .message("Avatar uploaded successfully")
	                    .data(response)
	                    .timestamp(LocalDateTime.now())
	                    .path("/api/v1/profile/upload-avatar")
	                    .build()
	    );
	}

	@GetMapping("/payments")
	public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPayments() {
		List<PaymentMethodResponse> response = profileService.getPayments();
		return ResponseEntity.ok(ApiResponse.<List<PaymentMethodResponse>>builder().success(true)
				.message("Payment methods fetched successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/profile/payments").build());
	}

	@PostMapping("/Addpayments")
	public ResponseEntity<ApiResponse<PaymentMethodResponse>> addPaymentMethod(
			@Valid @RequestBody AddPaymentMethodRequest request) {
		PaymentMethodResponse response = profileService.addPaymentMethod(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<PaymentMethodResponse>builder().success(true)
						.message("Payment method added successfully").data(response).timestamp(LocalDateTime.now())
						.path("/api/v1/profile/payments").build());
	}

	@PutMapping("/payments/{paymentMethodId}/primary")
	public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> setPrimaryPaymentMethod(
			@PathVariable String paymentMethodId) {
		List<PaymentMethodResponse> response = profileService.setPrimaryPaymentMethod(paymentMethodId);
		return ResponseEntity.ok(ApiResponse.<List<PaymentMethodResponse>>builder().success(true)
				.message("Primary payment method updated successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/profile/payments/" + paymentMethodId + "/primary").build());
	}

	@DeleteMapping("/payments/{paymentMethodId}")
	public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> deletePaymentMethod(
			@PathVariable String paymentMethodId) {
		List<PaymentMethodResponse> response = profileService.deletePaymentMethod(paymentMethodId);
		return ResponseEntity.ok(ApiResponse.<List<PaymentMethodResponse>>builder().success(true)
				.message("Payment method deleted successfully").data(response).timestamp(LocalDateTime.now())
				.path("/api/v1/profile/payments/" + paymentMethodId).build());
	}

	@GetMapping("/subscription")
	public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription() {
		SubscriptionResponse response = profileService.getSubscription();
		return ResponseEntity.ok(
				ApiResponse.<SubscriptionResponse>builder().success(true).message("Subscription fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/profile/subscription").build());
	}
	

	@GetMapping("/pages")
	public ResponseEntity<ApiResponse<List<StaticPageResponse>>> getPages() {
		List<StaticPageResponse> response = profileService.getPages();
		return ResponseEntity
				.ok(ApiResponse.<List<StaticPageResponse>>builder().success(true).message("Pages fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/profile/pages").build());
	}

	@GetMapping("/pages/{slug}")
	public ResponseEntity<ApiResponse<StaticPageResponse>> getPage(@PathVariable String slug) {
		StaticPageResponse response = profileService.getPage(slug);
		return ResponseEntity
				.ok(ApiResponse.<StaticPageResponse>builder().success(true).message("Page fetched successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/profile/pages/" + slug).build());
	}

	@DeleteMapping("/delete-account")
	public ResponseEntity<ApiResponse<Map<String, Object>>> deleteAccount() {
		Map<String, Object> response = profileService.deleteAccount();
		return ResponseEntity
				.ok(ApiResponse.<Map<String, Object>>builder().success(true).message("Account deleted successfully")
						.data(response).timestamp(LocalDateTime.now()).path("/api/v1/profile/delete-account").build());
	}
}
