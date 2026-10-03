package imkulov.collectors_yays_and_nays.DTOs;

import imkulov.collectors_yays_and_nays.model.ScannedCard;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProcessedCard {

    private ScannedCard scannedCard;
    private String oracleId;
    private BigDecimal eurPrice;

    private String imageUrl;
}