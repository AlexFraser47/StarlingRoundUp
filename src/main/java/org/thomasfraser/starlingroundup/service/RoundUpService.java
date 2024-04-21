package org.thomasfraser.starlingroundup.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thomasfraser.starlingroundup.client.StarlingClient;
import org.thomasfraser.starlingroundup.dto.AccountDto;
import org.thomasfraser.starlingroundup.dto.SavingsAccountDto;
import org.thomasfraser.starlingroundup.dto.SavingsAccountsResponseDto;
import org.thomasfraser.starlingroundup.dto.TransactionDto;
import org.thomasfraser.starlingroundup.util.JsonUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class RoundUpService {

    private static final Logger LOGGER = LogManager.getLogger(RoundUpService.class);
    private final StarlingClient starlingClient;
    private final String SAVINGS_GOALS_NAME = "RoundUp2024";

    @Autowired
    public RoundUpService(StarlingClient starlingClient) {
        this.starlingClient = starlingClient;
    }

    public BigDecimal calculateAndTransferRoundUp() throws Exception {
        List<AccountDto> accounts = starlingClient.fetchClientAccounts();

        // Assuming we are only interested in the primary account
        Optional<AccountDto> optionalAccount = accounts.stream()
                .filter(account -> "PRIMARY".equalsIgnoreCase(account.getAccountType()))
                .findFirst();

        if (optionalAccount.isPresent()) {
            AccountDto account = optionalAccount.get();
            String accountUuid = account.getAccountUid();
            String categoryId = account.getDefaultCategory();

            List<TransactionDto> transactions = starlingClient.fetchTransactions(accountUuid, categoryId, "2024-04-12T12:34:56.000Z", "2024-04-19T12:34:56.000Z");
            List<TransactionDto> validTransactions = transactions.stream()
                    .filter(this::isValidTransaction)
                    .toList();

            int roundUpTotal = calculateRoundUp(validTransactions);

            starlingClient.createSavingsGoal(accountUuid, SAVINGS_GOALS_NAME);

            // check for savings account
            List<SavingsAccountDto> existingSavingsAccount = starlingClient.getSavingsGoals(accountUuid);
            Optional<SavingsAccountDto> savingsAccountDto = existingSavingsAccount.stream()
                    .filter(savingsAccount -> savingsAccount.getName().equals(SAVINGS_GOALS_NAME))
                    .filter(savingsAccount -> savingsAccount.getState().equals("ACTIVE")).findFirst();

            if (savingsAccountDto.isPresent()) {
                String savingsGoalUid = savingsAccountDto.get().getSavingsGoalUid();
                boolean isSuccessfulRoundUp = starlingClient.addMoneyToSavingsGoal(accountUuid, savingsGoalUid, roundUpTotal);
                if (isSuccessfulRoundUp) {
                    return convertToBigDecimal(roundUpTotal);
                } else {
                    throw new Exception("Failed to transfer round up amount.");
                }
            } else {
                throw new Exception("No savings account found.");
            }

        } else {
            throw new Exception("No account found.");
        }
    }

    private BigDecimal convertToBigDecimal(int roundUpTotal) {
        BigDecimal formattedRoundUp = new BigDecimal(roundUpTotal).divide(new BigDecimal(100));

        LOGGER.debug("Converting {}, to {}", roundUpTotal, formattedRoundUp);
        return formattedRoundUp;
    }

    private int calculateRoundUp(List<TransactionDto> validTransactions) {
        int totalRoundUp = 0;
        for (TransactionDto transaction : validTransactions) {
            // calculate the round up amount
            int minorUnits = transaction.getAmount().getMinorUnits();
            int remainder = minorUnits % 100;

            totalRoundUp += 100 - remainder;
        }

        return totalRoundUp;
    }

    public String getAccountInfo() {
         return JsonUtil.convertListToJson(starlingClient.fetchClientAccounts());
    }

    private boolean isValidTransaction(TransactionDto transaction) {
        return transaction.getDirection().equalsIgnoreCase("OUT")
                && transaction.getAmount().getMinorUnits() > 0
                && transaction.getAmount().getCurrency().equalsIgnoreCase("GBP")
                && "SETTLED".equals(transaction.getStatus());
    }
}
