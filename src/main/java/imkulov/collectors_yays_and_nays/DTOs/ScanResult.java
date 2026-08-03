package imkulov.collectors_yays_and_nays.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ScanResult {

    private List<ProcessedCard> collectionWorthy;
    private List<ProcessedCard> maybe;
    private List<ProcessedCard> previouslySeen;
}