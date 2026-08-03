package imkulov.collectors_yays_and_nays.service;
import imkulov.collectors_yays_and_nays.DTOs.ProcessedCard;
import imkulov.collectors_yays_and_nays.model.ScannedCard;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class MoxfieldExportService {

    public void export(
            List<ProcessedCard> cards,
            Path outputPath
    ) throws IOException {

        Files.createDirectories(outputPath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            for (ProcessedCard card : cards) {

                ScannedCard scanned = card.getScannedCard();

                StringBuilder line = new StringBuilder();

                line.append(scanned.getQuantity())
                        .append(" ")
                        .append(scanned.getName())
                        .append(" (")
                        .append(scanned.getSetCode().toUpperCase())
                        .append(") ")
                        .append(scanned.getCollectorNumber());

                if ("foil".equalsIgnoreCase(scanned.getFoil())) {
                    line.append(" *F*");
                }

                writer.write(line.toString());
                writer.newLine();
            }
        }
    }
}