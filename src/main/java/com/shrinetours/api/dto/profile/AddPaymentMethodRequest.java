package com.shrinetours.api.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddPaymentMethodRequest(
        @NotBlank(message = "Card type is required")
        String type,
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^[0-9]{12,19}$", message = "Card number must contain 12 to 19 digits")
        String card_number,
        @NotBlank(message = "Card holder name is required")
        String holder_name,
        @NotBlank(message = "Expiry is required")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Expiry must be in MM/YY format")
        String expiry,
        @JsonProperty("is_primary")
        Boolean is_primary
) {
}
