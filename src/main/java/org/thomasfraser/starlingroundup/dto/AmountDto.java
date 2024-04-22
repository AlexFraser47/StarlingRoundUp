package org.thomasfraser.starlingroundup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmountDto {
    private String currency;
    private long minorUnits;
}
