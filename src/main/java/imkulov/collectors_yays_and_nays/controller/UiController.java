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

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import java.nio.file.Files;
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


    // ---------------------------------------------------------
    // MAIN PAGE
    // ---------------------------------------------------------

    @GetMapping("/")
    public String index() {
        return "index";
    }


    // ---------------------------------------------------------
    // PROCESS SCAN
    // ---------------------------------------------------------

    @PostMapping("/process")
    public String process(
            @RequestParam("file") MultipartFile file,
            Model model
    ) {

        try {

            List<ScannedCard> scannedCards =
                    manaBoxCsvParser.parse(file);

            List<ProcessedCard> processedCards =
                    cardProcessingService.process(scannedCards);

            ScanResult result =
                    scanClassifierService.classify(processedCards);


            // Generate current-scan output files

            moxfieldExportService.export(
                    result.getCollectionWorthy(),
                    Path.of(
                            "data",
                            "collection-worthy.txt"
                    )
            );

            moxfieldExportService.export(
                    result.getMaybe(),
                    Path.of(
                            "data",
                            "maybe.txt"
                    )
            );

            moxfieldExportService.export(
                    result.getPreviouslySeen(),
                    Path.of(
                            "data",
                            "previously-seen.txt"
                    )
            );


            // Only newly encountered cards are added to history.
            // Previously seen cards must not be appended again.

            List<ProcessedCard> newCards =
                    new ArrayList<>();

            newCards.addAll(
                    result.getCollectionWorthy()
            );

            newCards.addAll(
                    result.getMaybe()
            );

            scanHistoryService.saveSeenCards(
                    newCards
            );


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

            model.addAttribute(
                    "duplicateCount",
                    result.getDuplicates().size()
            );


            model.addAttribute(
                    "collectionCards",
                    result.getCollectionWorthy()
            );

            model.addAttribute(
                    "maybeCards",
                    result.getMaybe()
            );

            model.addAttribute(
                    "seenCards",
                    result.getPreviouslySeen()
            );

            model.addAttribute(
                    "duplicateCards",
                    result.getDuplicates()
            );

            model.addAttribute(
                    "successMessage",
                    "Scan processed successfully."
            );


        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "The scan could not be processed: "
                            + e.getMessage()
            );
        }

        return "index";
    }


    // ---------------------------------------------------------
    // DOWNLOAD COLLECTION-WORTHY LIST
    // ---------------------------------------------------------

    @GetMapping("/download/collection")
    public ResponseEntity<Resource> downloadCollection()
            throws IOException {

        return downloadFile(
                Path.of(
                        "data",
                        "collection-worthy.txt"
                ),
                "collection-worthy.txt"
        );
    }


    // ---------------------------------------------------------
    // DOWNLOAD MAYBE LIST
    // ---------------------------------------------------------

    @GetMapping("/download/maybe")
    public ResponseEntity<Resource> downloadMaybe()
            throws IOException {

        return downloadFile(
                Path.of(
                        "data",
                        "maybe.txt"
                ),
                "maybe.txt"
        );
    }


    // ---------------------------------------------------------
    // DOWNLOAD PREVIOUSLY-SEEN LIST
    // ---------------------------------------------------------

    @GetMapping("/download/seen")
    public ResponseEntity<Resource> downloadPreviouslySeen()
            throws IOException {

        return downloadFile(
                Path.of(
                        "data",
                        "previously-seen.txt"
                ),
                "previously-seen.txt"
        );
    }


    // ---------------------------------------------------------
    // DOWNLOAD COMPLETE HISTORY
    // ---------------------------------------------------------

    @GetMapping("/download/history")
    public ResponseEntity<Resource> downloadHistory()
            throws IOException {

        return downloadFile(
                Path.of(
                        "data",
                        "seen-cards.csv"
                ),
                "seen-cards.csv"
        );
    }


    // ---------------------------------------------------------
    // CLEAR HISTORY CONFIRMATION
    // ---------------------------------------------------------

    @GetMapping("/history/clear")
    public String confirmClearHistory() {
        return "confirm-clear-history";
    }


    // ---------------------------------------------------------
    // CLEAR HISTORY
    // ---------------------------------------------------------

    @PostMapping("/history/clear")
    public String clearHistory(
            RedirectAttributes redirectAttributes
    ) throws IOException {

        Path historyPath =
                Path.of(
                        "data",
                        "seen-cards.csv"
                );

        Files.deleteIfExists(
                historyPath
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Scan history cleared successfully."
        );

        return "redirect:/";
    }


    // ---------------------------------------------------------
    // DOWNLOAD
    // ---------------------------------------------------------

    private ResponseEntity<Resource> downloadFile(
            Path path,
            String fileName
    ) throws IOException {

        Resource resource =
                new UrlResource(
                        path.toUri()
                );

        if (!resource.exists()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + fileName
                                + "\""
                )
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                )
                .body(resource);
    }
}