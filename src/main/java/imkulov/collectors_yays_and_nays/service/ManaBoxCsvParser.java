package imkulov.collectors_yays_and_nays.service;

import imkulov.collectors_yays_and_nays.model.ScannedCard;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ManaBoxCsvParser {

    public List<ScannedCard> parse(MultipartFile file) throws IOException {

        List<ScannedCard> cards = new ArrayList<>();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            Iterable<CSVRecord> records = format.parse(reader);

            for (CSVRecord record : records) {

                ScannedCard card = ScannedCard.builder()
                        .name(record.get("Name"))
                        .setCode(record.get("Set code"))
                        .setName(record.get("Set name"))
                        .collectorNumber(record.get("Collector number"))
                        .foil(record.get("Foil"))
                        .rarity(record.get("Rarity"))
                        .quantity(Integer.parseInt(record.get("Quantity")))
                        .manaBoxId(record.get("ManaBox ID"))
                        .scryfallId(record.get("Scryfall ID"))
                        .condition(record.get("Condition"))
                        .language(record.get("Language"))
                        .build();

                cards.add(card);
            }
        }

        return cards;
    }
}