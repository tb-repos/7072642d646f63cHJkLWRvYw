package com.tourbhook.api.controller;

import com.tourbhook.api.dto.common.ApiResponse;
import com.tourbhook.api.dto.profile.AddPaymentMethodRequest;
import com.tourbhook.api.dto.profile.PaymentMethodResponse;
import com.tourbhook.api.dto.profile.ProfileResponse;
import com.tourbhook.api.dto.profile.UpdateProfileRequest;
import com.tourbhook.api.service.FileService;
import com.tourbhook.api.service.ProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;
    @Mock
    private FileService fileService;

    private ProfileController controller;

    @BeforeEach
    void setUp() {
        controller = new ProfileController(profileService, fileService);
    }

    @Test
    void getProfile_returnsOkWithProfileBody() {
        ProfileResponse expected = new ProfileResponse(
                "user-1", "Test", "test@example.com", "9090909090",
                LocalDate.of(1996, 4, 12), "https://cdn/avatar.png",
                "Explorer", new BigDecimal("0.10"), 2, false);
        when(profileService.getProfile()).thenReturn(expected);

        ResponseEntity<ApiResponse<ProfileResponse>> response = controller.getProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(expected);
    }

    @Test
    void updateProfile_delegatesRequestToServiceAndReturnsUpdatedProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest("New Name", null, null, null);
        ProfileResponse expected = new ProfileResponse(
                "user-1", "New Name", "test@example.com", "9090909090",
                LocalDate.of(1996, 4, 12), "https://cdn/avatar.png",
                "Explorer", new BigDecimal("0.10"), 2, false);
        when(profileService.updateProfile(request)).thenReturn(expected);

        ResponseEntity<ApiResponse<ProfileResponse>> response = controller.updateProfile(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().name()).isEqualTo("New Name");
        verify(profileService).updateProfile(request);
    }

    @Test
    void addPaymentMethod_returnsCreatedStatus() {
        AddPaymentMethodRequest request = new AddPaymentMethodRequest(
                "visa", "4111111111111111", "Test", "09/29", true);
        PaymentMethodResponse expected = new PaymentMethodResponse("pm-1", "VISA", "1111", "Asha Rao", "09/29", true);
        when(profileService.addPaymentMethod(any(AddPaymentMethodRequest.class))).thenReturn(expected);

        ResponseEntity<ApiResponse<PaymentMethodResponse>> response = controller.addPaymentMethod(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getData().lastFour()).isEqualTo("1111");
    }

    @Test
    void getPayments_returnsListFromService() {
        List<PaymentMethodResponse> expected = List.of(
                new PaymentMethodResponse("pm-1", "VISA", "1111", "Test", "09/29", true));
        when(profileService.getPayments()).thenReturn(expected);

        ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> response = controller.getPayments();

        assertThat(response.getBody().getData()).hasSize(1);
    }

    @Test
    void deleteAccount_returnsOkWithConfirmationBody() {
        when(profileService.deleteAccount()).thenReturn(Map.of("ok", true, "message", "Account deleted successfully"));

        ResponseEntity<ApiResponse<Map<String, Object>>> response = controller.deleteAccount();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).containsEntry("ok", true);
    }
}
