package imkulov.collectors_yays_and_nays.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DuplicateCard {

    private ProcessedCard card;
    private int count;
}