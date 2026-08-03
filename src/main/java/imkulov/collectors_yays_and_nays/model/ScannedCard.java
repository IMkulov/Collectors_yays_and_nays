package imkulov.collectors_yays_and_nays.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScannedCard {

    private String name;

    private String setCode;
    private String setName;
    private String collectorNumber;

    private String foil;
    private String rarity;

    private int quantity;

    private String manaBoxId;
    private String scryfallId;

    private String condition;
    private String language;
}