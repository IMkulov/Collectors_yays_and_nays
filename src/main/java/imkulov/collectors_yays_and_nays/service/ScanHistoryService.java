package imkulov.collectors_yays_and_nays.service;
import imkulov.collectors_yays_and_nays.DTOs.ProcessedCard;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScanHistoryService {

    private static final Path HISTORY_PATH =
            Path.of("data", "seen-cards.csv");

    public Set<String> loadSeenOracleIds() throws IOException {

        Set<String> oracleIds = new HashSet<>();

        if (!Files.exists(HISTORY_PATH)) {
            return oracleIds;
        }

        try (
                BufferedReader reader =
                        Files.newBufferedReader(HISTORY_PATH)
        ) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .get();

            for (CSVRecord record : format.parse(reader)) {
                oracleIds.add(record.get("Oracle ID"));
            }
        }

        return oracleIds;
    }

    public void saveSeenCards(List<ProcessedCard> cards)
            throws IOException {

        Files.createDirectories(HISTORY_PATH.getParent());

        boolean fileExists = Files.exists(HISTORY_PATH);

        try (
                BufferedWriter writer = Files.newBufferedWriter(
                        HISTORY_PATH,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );

                CSVPrinter printer = new CSVPrinter(
                        writer,
                        CSVFormat.DEFAULT
                )
        ) {

            if (!fileExists) {
                printer.printRecord(
                        "Oracle ID",
                        "Name"
                );
            }

            for (ProcessedCard card : cards) {

                printer.printRecord(
                        card.getOracleId(),
                        card.getScannedCard().getName()
                );
            }
        }
    }
}