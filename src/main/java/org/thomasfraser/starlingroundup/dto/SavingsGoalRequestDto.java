package org.thomasfraser.starlingroundup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavingsGoalRequestDto {
    private String name;
    private String currency;
    private AmountDto target;
    private String base64EncodedPhoto;
}
