package org.thomasfraser.starlingroundup.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransactionsResponseDto {
    private List<TransactionDto> feedItems;

}
