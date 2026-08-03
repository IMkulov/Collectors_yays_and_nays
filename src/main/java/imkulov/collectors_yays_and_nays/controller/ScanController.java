package imkulov.collectors_yays_and_nays.controller;

import imkulov.collectors_yays_and_nays.DTOs.ProcessedCard;
import imkulov.collectors_yays_and_nays.DTOs.ScanResult;
import imkulov.collectors_yays_and_nays.DTOs.ScryfallCardData;
import imkulov.collectors_yays_and_nays.model.ScannedCard;
import imkulov.collectors_yays_and_nays.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScanController {

    private final CardProcessingService cardProcessingService;
    private final ScanClassifierService scanClassifierService;
    private final ScanHistoryService scanHistoryService;
    private final MoxfieldExportService moxfieldExportService;

    private final ManaBoxCsvParser manaBoxCsvParser;
    private final ScryfallService scryfallService;

    @PostMapping("/classify")
    public ScanResult classifyScan(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        List<ScannedCard> scannedCards =
                manaBoxCsvParser.parse(file);

        List<ProcessedCard> processedCards =
                cardProcessingService.process(scannedCards);

        ScanResult result =
                scanClassifierService.classify(processedCards);

        moxfieldExportService.export(
                result.getCollectionWorthy(),
                Path.of("data", "collection-worthy.txt")
        );

        moxfieldExportService.export(
                result.getMaybe(),
                Path.of("data", "maybe.txt")
        );

        List<ProcessedCard> newCards = new ArrayList<>();

        newCards.addAll(result.getCollectionWorthy());
        newCards.addAll(result.getMaybe());

        scanHistoryService.saveSeenCards(newCards);

        return result;
    }

    @PostMapping("/scan")
    public ResponseEntity<List<ScannedCard>> scan(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        List<ScannedCard> cards = manaBoxCsvParser.parse(file);

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/scryfall/{id}")
    public ScryfallCardData testScryfall(
            @PathVariable String id
    ) {
        return scryfallService.getCardData(id);
    }
}