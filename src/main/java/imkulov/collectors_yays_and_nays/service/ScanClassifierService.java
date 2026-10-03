package imkulov.collectors_yays_and_nays.service;

import imkulov.collectors_yays_and_nays.DTOs.DuplicateCard;
import imkulov.collectors_yays_and_nays.DTOs.ProcessedCard;
import imkulov.collectors_yays_and_nays.DTOs.ScanResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScanClassifierService {

    private final ScanHistoryService scanHistoryService;

    private static final BigDecimal PRICE_THRESHOLD =
            new BigDecimal("0.10");

    public ScanResult classify(List<ProcessedCard> cards)
            throws IOException {

        Set<String> historicalOracleIds =
                scanHistoryService.loadSeenOracleIds();

        List<ProcessedCard> collectionWorthy =
                new ArrayList<>();

        List<ProcessedCard> maybe =
                new ArrayList<>();

        List<ProcessedCard> previouslySeen =
                new ArrayList<>();

        List<DuplicateCard> duplicates =
                new ArrayList<>();


        /*
         * Group cards from THIS scan by Oracle ID.
         *
         * LinkedHashMap is used so the cards retain
         * approximately the same order as the original scan.
         */
        Map<String, List<ProcessedCard>> cardsByOracleId =
                new LinkedHashMap<>();

        for (ProcessedCard card : cards) {

            cardsByOracleId
                    .computeIfAbsent(
                            card.getOracleId(),
                            key -> new ArrayList<>()
                    )
                    .add(card);
        }


        /*
         * Each Oracle ID is classified only once.
         */
        for (Map.Entry<String, List<ProcessedCard>> entry
                : cardsByOracleId.entrySet()) {

            String oracleId =
                    entry.getKey();

            List<ProcessedCard> occurrences =
                    entry.getValue();

            /*
             * Use the first scanned occurrence as the
             * representative card for the normal lists.
             */
            ProcessedCard representative =
                    occurrences.getFirst();


            /*
             * A duplicate is supplemental information.
             *
             * The card can therefore be:
             *
             * Collection Worthy + Duplicate
             * Maybe + Duplicate
             * Previously Seen + Duplicate
             */
            if (occurrences.size() > 1) {

                duplicates.add(
                        new DuplicateCard(
                                representative,
                                occurrences.size()
                        )
                );
            }


            /*
             * Historical duplicate:
             * this card existed BEFORE the current scan.
             */
            if (historicalOracleIds.contains(oracleId)) {

                previouslySeen.add(
                        representative
                );

                continue;
            }


            /*
             * New card:
             * classify according to price.
             */
            if (representative.getEurPrice() == null ||
                    representative
                            .getEurPrice()
                            .compareTo(PRICE_THRESHOLD) < 0) {

                maybe.add(
                        representative
                );

            } else {

                collectionWorthy.add(
                        representative
                );
            }
        }


        return new ScanResult(
                collectionWorthy,
                maybe,
                previouslySeen,
                duplicates
        );
    }
}