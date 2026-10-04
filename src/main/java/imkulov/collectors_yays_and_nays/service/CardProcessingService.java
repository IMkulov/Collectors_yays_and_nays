package imkulov.collectors_yays_and_nays.service;

import imkulov.collectors_yays_and_nays.DTOs.ProcessedCard;
import imkulov.collectors_yays_and_nays.DTOs.ScryfallCardData;
import imkulov.collectors_yays_and_nays.model.ScannedCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardProcessingService {

    private final ScryfallService scryfallService;

    public List<ProcessedCard> process(List<ScannedCard> cards) {

        List<ProcessedCard> processedCards = new ArrayList<>();

        for (ScannedCard card : cards) {

            ScryfallCardData scryfallData =
                    scryfallService.getCardData(card.getScryfallId());

            processedCards.add(new ProcessedCard(
                            card,
                            scryfallData.getOracleId(),
                            scryfallData.getEurPrice(),
                            scryfallData.getImageUrls()
                    )
            );
        }

        return processedCards;
    }
}