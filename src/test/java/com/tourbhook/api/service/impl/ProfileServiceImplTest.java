package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.profile.AddPaymentMethodRequest;
import com.tourbhook.api.dto.profile.PaymentMethodResponse;
import com.tourbhook.api.dto.profile.ProfileResponse;
import com.tourbhook.api.dto.profile.StaticPageResponse;
import com.tourbhook.api.dto.profile.SubscriptionResponse;
import com.tourbhook.api.dto.profile.UpdateProfileRequest;
import com.tourbhook.api.dto.profile.UploadAvatarResponse;
import com.tourbhook.api.entity.PaymentMethod;
import com.tourbhook.api.entity.StaticPage;
import com.tourbhook.api.entity.Subscription;
import com.tourbhook.api.entity.User;
import com.tourbhook.api.repository.PaymentMethodRepository;
import com.tourbhook.api.repository.StaticPageRepository;
import com.tourbhook.api.repository.SubscriptionRepository;
import com.tourbhook.api.repository.UserRepository;
import com.tourbhook.api.repository.exception.BadRequestException;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.AuthenticatedUserService;
import com.tourbhook.api.service.FileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProfileServiceImpl}. Every collaborator is mocked so
 * these run without a Spring context or a database.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentMethodRepository paymentMethodRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private StaticPageRepository staticPageRepository;
    @Mock
    private AuthenticatedUserService authenticatedUserService;
    @Mock
    private FileService fileService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-1")
                .name("Asha Rao")
                .email("asha@example.com")
                .phone("9876543210")
                .password("hashed-password")
                .dob(LocalDate.of(1996, 4, 12))
                .avatarUrl("https://cdn.tourbhook.local/avatar/default.png")
                .level("Explorer")
                .levelProgress(new BigDecimal("0.10"))
                .tripsCompleted(2)
                .premium(false)
                .deleted(false)
                .build();

        // Most service methods resolve the caller through this same path;
        // a couple (static page lookups) don't, so these are lenient rather
        // than strict stubs to avoid an UnnecessaryStubbingException there.
        lenient().when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        lenient().when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
    }

    // ---------------------------------------------------------------
    // getProfile / updateProfile
    // ---------------------------------------------------------------

    @Test
    void getProfile_returnsCurrentUserMappedToResponse() {
        ProfileResponse response = profileService.getProfile();

        assertThat(response.id()).isEqualTo("user-1");
        assertThat(response.name()).isEqualTo("Asha Rao");
        assertThat(response.email()).isEqualTo("asha@example.com");
        assertThat(response.premium()).isFalse();
    }

    @Test
    void getProfile_whenUserMissing_throwsResourceNotFound() {
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_updatesOnlyTheSuppliedFields() {
        UpdateProfileRequest request = new UpdateProfileRequest("  Asha K Rao  ", null, "9999999999", null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileResponse response = profileService.updateProfile(request);

        assertThat(response.name()).isEqualTo("Asha K Rao");
        assertThat(response.phone()).isEqualTo("9999999999");
        assertThat(response.email()).isEqualTo("asha@example.com");
        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    void updateProfile_normalizesAndAllowsKeepingOwnEmail() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, "ASHA@EXAMPLE.COM", null, null);
        when(userRepository.findByEmailIgnoreCase("asha@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileResponse response = profileService.updateProfile(request);

        assertThat(response.email()).isEqualTo("asha@example.com");
    }

    @Test
    void updateProfile_whenEmailBelongsToAnotherUser_throwsBadRequest() {
        User someoneElse = User.builder().id("user-2").email("taken@example.com").build();
        UpdateProfileRequest request = new UpdateProfileRequest(null, "taken@example.com", null, null);
        when(userRepository.findByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(someoneElse));

        assertThatThrownBy(() -> profileService.updateProfile(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    // ---------------------------------------------------------------
    // uploadAvatar
    // ---------------------------------------------------------------

    @Test
    void uploadAvatar_withValidImage_updatesAndReturnsUrl() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image-bytes".getBytes());
        when(fileService.uploadAvatar(file)).thenReturn("https://cdn.tourbhook.local/avatars/photo.jpg");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UploadAvatarResponse response = profileService.uploadAvatar(file);

        assertThat(response.getAvatarUrl()).isEqualTo("https://cdn.tourbhook.local/avatars/photo.jpg");
        assertThat(user.getAvatarUrl()).isEqualTo("https://cdn.tourbhook.local/avatars/photo.jpg");
    }

    @Test
    void uploadAvatar_withEmptyFile_throwsBadRequest() {
        MultipartFile empty = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> profileService.uploadAvatar(empty))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Image file is required");
    }

    @Test
    void uploadAvatar_withDisallowedContentType_throwsBadRequest() {
        MultipartFile pdf = new MockMultipartFile("file", "resume.pdf", "application/pdf", "not-an-image".getBytes());

        assertThatThrownBy(() -> profileService.uploadAvatar(pdf))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only image files are allowed");
    }

    @Test
    void uploadAvatar_withOversizedFile_throwsBadRequest() {
        byte[] oversized = new byte[6 * 1024 * 1024];
        MultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", oversized);

        assertThatThrownBy(() -> profileService.uploadAvatar(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("File size must be less than 5 MB");
    }

    @Test
    void uploadAvatar_whenStorageFails_wrapsIntoBadRequest() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image-bytes".getBytes());
        when(fileService.uploadAvatar(file)).thenThrow(new IOException("disk full"));

        assertThatThrownBy(() -> profileService.uploadAvatar(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Failed to upload avatar");

        verify(userRepository, never()).save(any(User.class));
    }

    // ---------------------------------------------------------------
    // Payment methods
    // ---------------------------------------------------------------

    @Test
    void addPaymentMethod_asFirstMethod_isMarkedPrimaryAutomatically() {
        AddPaymentMethodRequest request = new AddPaymentMethodRequest(
                "visa", "4111 1111 1111 1111", "Asha Rao", "09/29", null);
        when(paymentMethodRepository.findByUserOrderByPrimaryMethodDescCreatedAtDesc(user))
                .thenReturn(List.of());
        when(paymentMethodRepository.save(any(PaymentMethod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentMethodResponse response = profileService.addPaymentMethod(request);

        assertThat(response.lastFour()).isEqualTo("1111");
        assertThat(response.primaryMethod()).isTrue();
    }

    @Test
    void addPaymentMethod_withTooFewDigits_throwsBadRequest() {
        AddPaymentMethodRequest request = new AddPaymentMethodRequest("visa", "12", "Asha Rao", "09/29", null);

        assertThatThrownBy(() -> profileService.addPaymentMethod(request))
                .isInstanceOf(BadRequestException.class);

        verify(paymentMethodRepository, never()).save(any(PaymentMethod.class));
    }

    @Test
    void setPrimaryPaymentMethod_whenNotFound_throwsResourceNotFound() {
        when(paymentMethodRepository.findByIdAndUser("missing-id", user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.setPrimaryPaymentMethod("missing-id"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletePaymentMethod_whenDeletingPrimary_promotesNextRemainingMethod() {
        PaymentMethod primary = PaymentMethod.builder().id("pm-1").user(user).primaryMethod(true).build();
        PaymentMethod other = PaymentMethod.builder().id("pm-2").user(user).primaryMethod(false).build();

        when(paymentMethodRepository.findByIdAndUser("pm-1", user)).thenReturn(Optional.of(primary));
        when(paymentMethodRepository.findByUserOrderByPrimaryMethodDescCreatedAtDesc(user))
                .thenReturn(List.of(other));

        profileService.deletePaymentMethod("pm-1");

        assertThat(other.getPrimaryMethod()).isTrue();
        verify(paymentMethodRepository).delete(primary);
        verify(paymentMethodRepository).save(other);
    }

    // ---------------------------------------------------------------
    // Subscription / static pages / account deletion
    // ---------------------------------------------------------------

    @Test
    void getSubscription_whenNoneExists_throwsResourceNotFound() {
        when(subscriptionRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getSubscription())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getSubscription_mapsEntityToResponse() {
        Subscription subscription = Subscription.builder()
                .id("sub-1")
                .user(user)
                .plan("PRO")
                .status("ACTIVE")
                .renewsAt(LocalDate.of(2026, 12, 1))
                .features(List.of("offline_maps"))
                .build();
        when(subscriptionRepository.findByUser(user)).thenReturn(Optional.of(subscription));

        SubscriptionResponse response = profileService.getSubscription();

        assertThat(response.plan()).isEqualTo("PRO");
        assertThat(response.features()).containsExactly("offline_maps");
    }

    @Test
    void getPage_whenSlugUnknown_throwsResourceNotFound() {
        when(staticPageRepository.findBySlug("privacy-policy")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getPage("privacy-policy"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPage_returnsMatchingPage() {
        StaticPage page = StaticPage.builder().id("p1").slug("privacy-policy").title("Privacy Policy").content("...").build();
        when(staticPageRepository.findBySlug("privacy-policy")).thenReturn(Optional.of(page));

        StaticPageResponse response = profileService.getPage("privacy-policy");

        assertThat(response.title()).isEqualTo("Privacy Policy");
    }

    @Test
    void deleteAccount_softDeletesTheCurrentUser() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = profileService.deleteAccount();

        assertThat(user.isDeleted()).isTrue();
        assertThat(response).containsEntry("ok", true);
        verify(userRepository, times(1)).save(user);
    }
}
