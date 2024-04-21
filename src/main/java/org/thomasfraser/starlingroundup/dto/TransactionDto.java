package org.thomasfraser.starlingroundup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDto {
    private String direction;
    private AmountDto amount;
    private String status;
}
