package imkulov.collectors_yays_and_nays.service;

import imkulov.collectors_yays_and_nays.DTOs.ScryfallCardData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScryfallService {

    private final ScryfallRateLimiter rateLimiter;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.scryfall.com")
            .defaultHeader("User-Agent", "CollectorsYaysAndNays/1.0")
            .defaultHeader("Accept", "application/json")
            .build();

    public ScryfallCardData getCardData(String scryfallId) {

        rateLimiter.waitForPermission();

        Map response = restClient.get()
                .uri("/cards/{id}", scryfallId)
                .retrieve()
                .body(Map.class);

        ScryfallCardData data = new ScryfallCardData();

        data.setOracleId(
                (String) response.get("oracle_id")
        );

        Map prices = (Map) response.get("prices");

        String eur = (String) prices.get("eur");

        if (eur != null) {
            data.setEurPrice(new BigDecimal(eur));
        }

        return data;
    }
}