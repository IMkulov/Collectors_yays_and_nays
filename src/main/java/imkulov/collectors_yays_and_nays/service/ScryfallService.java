package imkulov.collectors_yays_and_nays.service;

import imkulov.collectors_yays_and_nays.DTOs.ScryfallCardData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
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
        List<String> imageUrls = new ArrayList<>();

        Map imageUris = (Map) response.get("image_uris");
        if (imageUris != null) {

            String imageUrl = (String) imageUris.get("small");
            if (imageUrl != null) {
                imageUrls.add(imageUrl);
            }
        }


        else {

            List<Map> cardFaces = (List<Map>) response.get("card_faces");
            if (cardFaces != null) {

                for (Map face : cardFaces) {
                    Map faceImageUris = (Map) face.get("image_uris");
                    if (faceImageUris != null) {
                        String imageUrl = (String) faceImageUris.get("small");
                        if (imageUrl != null) {
                            imageUrls.add(imageUrl);
                        }
                    }
                }
            }
        }
        data.setImageUrls(imageUrls);
        return data;
    }
}