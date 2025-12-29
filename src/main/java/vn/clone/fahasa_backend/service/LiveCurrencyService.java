package vn.clone.fahasa_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import vn.clone.fahasa_backend.domain.response.ExchangeRateResponse;

@Service
public class LiveCurrencyService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${exchangerate-api.key}")
    private String apiKey;

    public BigDecimal convertVndToUsdLive(BigDecimal vndAmount) {
        BigDecimal rate = getVndToUsdRate();
        return vndAmount.multiply(rate)
                        .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal convertVndToUsdLive(BigDecimal vndAmount, BigDecimal rate) {
        return vndAmount.multiply(rate)
                        .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getVndToUsdRate() {
        String baseUrl = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/VND";

        try {
            // Fetch live rates
            ExchangeRateResponse response = restTemplate.getForObject(baseUrl, ExchangeRateResponse.class);

            if (response != null && "success".equals(response.result())) {
                double usdRate = response.conversionRates()
                                         .get("USD");

                return BigDecimal.valueOf(usdRate);
            }
        } catch (Exception e) {
            // Log error and fallback to a hardcoded rate or cached value
            System.err.println("Failed to fetch live rate: " + e.getMessage());
        }

        // Fallback rate if API fails (approximate 2025 rate)
        return new BigDecimal("0.000038");
    }
}
