package vn.clone.fahasa_backend.domain.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExchangeRateResponse(
        String result,

        @JsonProperty("base_code")
        String baseCode,

        @JsonProperty("conversion_rates")
        Map<String, Double> conversionRates
) {
}
