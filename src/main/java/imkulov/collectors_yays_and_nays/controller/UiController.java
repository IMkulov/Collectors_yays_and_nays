package imkulov.collectors_yays_and_nays.controller;


import imkulov.collectors_yays_and_nays.DTOs.ProcessedCard;
import imkulov.collectors_yays_and_nays.DTOs.ScanResult;
import imkulov.collectors_yays_and_nays.model.ScannedCard;
import imkulov.collectors_yays_and_nays.service.CardProcessingService;
import imkulov.collectors_yays_and_nays.service.ManaBoxCsvParser;
import imkulov.collectors_yays_and_nays.service.MoxfieldExportService;
import imkulov.collectors_yays_and_nays.service.ScanClassifierService;
import imkulov.collectors_yays_and_nays.service.ScanHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UiController {

    private final ManaBoxCsvParser manaBoxCsvParser;
    private final CardProcessingService cardProcessingService;
    private final ScanClassifierService scanClassifierService;
    private final MoxfieldExportService moxfieldExportService;
    private final ScanHistoryService scanHistoryService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/history/clear")
    public String clearHistory() throws IOException {

        Path historyPath = Path.of("data", "seen-cards.csv");

        Files.deleteIfExists(historyPath);

        return "redirect:/";
    }

    @GetMapping("/history/clear")
    public String confirmClearHistory() {
        return "confirm-clear-history";
    }

    @PostMapping("/process")
    public String process(@RequestParam("file") MultipartFile file, Model model) throws IOException {

        List<ScannedCard> scannedCards = manaBoxCsvParser.parse(file);

        List<ProcessedCard> processedCards = cardProcessingService.process(scannedCards);

        ScanResult result = scanClassifierService.classify(processedCards);

        moxfieldExportService.export(
                result.getCollectionWorthy(),
                Path.of("data", "collection-worthy.txt")
        );

        moxfieldExportService.export(
                result.getMaybe(),
                Path.of("data", "maybe.txt")
        );

        moxfieldExportService.export(
                result.getPreviouslySeen(),
                Path.of("data", "previously-seen.txt")
        );

        List<ProcessedCard> newCards = new ArrayList<>();
        newCards.addAll(result.getCollectionWorthy());
        newCards.addAll(result.getMaybe());

        scanHistoryService.saveSeenCards(newCards);

        model.addAttribute(
                "collectionCount",
                result.getCollectionWorthy().size()
        );

        model.addAttribute(
                "maybeCount",
                result.getMaybe().size()
        );

        model.addAttribute(
                "seenCount",
                result.getPreviouslySeen().size()
        );

        return "index";
    }
}