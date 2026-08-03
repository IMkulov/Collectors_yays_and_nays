package imkulov.collectors_yays_and_nays.service;

import imkulov.collectors_yays_and_nays.DTOs.ProcessedCard;
import imkulov.collectors_yays_and_nays.DTOs.ScanResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScanClassifierService {

    private final ScanHistoryService scanHistoryService;

    private static final BigDecimal PRICE_THRESHOLD =
            new BigDecimal("0.10");

    public ScanResult classify(List<ProcessedCard> cards)
            throws IOException {

        Set<String> seenOracleIds =
                scanHistoryService.loadSeenOracleIds();

        List<ProcessedCard> collectionWorthy = new ArrayList<>();
        List<ProcessedCard> maybe = new ArrayList<>();
        List<ProcessedCard> previouslySeen = new ArrayList<>();

        for (ProcessedCard card : cards) {

            if (seenOracleIds.contains(card.getOracleId())) {
                previouslySeen.add(card);
                continue;
            }

            seenOracleIds.add(card.getOracleId());

            if (card.getEurPrice() == null ||
                    card.getEurPrice().compareTo(PRICE_THRESHOLD) < 0) {

                maybe.add(card);

            } else {

                collectionWorthy.add(card);
            }
        }

        return new ScanResult(
                collectionWorthy,
                maybe,
                previouslySeen
        );
    }
}