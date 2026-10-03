package imkulov.collectors_yays_and_nays.DTOs;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScryfallCardData {

    private String oracleId;
    private BigDecimal eurPrice;

    private String imageUrl;
}