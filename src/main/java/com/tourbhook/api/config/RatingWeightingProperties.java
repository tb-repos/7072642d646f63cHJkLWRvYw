package com.tourbhook.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "itinerary.rating-weighting")
public class RatingWeightingProperties {

    private BigDecimal threshold = new BigDecimal("4.5");

    private double boostWeight = 2.0;
}