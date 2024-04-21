package org.thomasfraser.starlingroundup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavingsAccountDto {
    private String savingsGoalUid;
    private String name;
    private AmountDto target;
    private AmountDto totalSaved;
    private int savedPercentage;
    private String state;
}
