package org.thomasfraser.starlingroundup.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thomasfraser.starlingroundup.client.StarlingClient;
import org.thomasfraser.starlingroundup.dto.AccountDto;
import org.thomasfraser.starlingroundup.dto.SavingsAccountDto;
import org.thomasfraser.starlingroundup.dto.TransactionDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service class for handling round up operations.
 */
@Service
public class RoundUpService {

    private static final Logger LOGGER = LogManager.getLogger(RoundUpService.class);
    private final StarlingClient starlingClient;
    private final String SAVINGS_GOALS_NAME = "RoundUp2025";

    @Autowired
    public RoundUpService(StarlingClient starlingClient) {
        this.starlingClient = starlingClient;
    }

    public BigDecimal calculateAndTransferRoundUp() throws Exception {
        List<AccountDto> accounts = starlingClient.fetchClientAccounts();
        AccountDto account = getPrimaryAccount(accounts);
        List<TransactionDto> transactions = fetchValidTransactions(account);
        int roundUpTotal = calculateRoundUp(transactions);

        SavingsAccountDto savingsAccount = ensureSavingsAccountExists(account);
        if (savingsAccount == null) {
            starlingClient.createSavingsGoal(account.getAccountUid(), SAVINGS_GOALS_NAME);
            savingsAccount = ensureSavingsAccountExists(account);
        }

        boolean success = transferRoundUpToSavings(account, savingsAccount, roundUpTotal);
        if (!success) {
            throw new Exception("Failed to transfer round up amount.");
        }

        return convertToBigDecimal(roundUpTotal);
    }

    private AccountDto getPrimaryAccount(List<AccountDto> accounts) throws Exception {
        // Assuming we use primary account for round up
        return accounts.stream()
                .filter(account -> "PRIMARY".equalsIgnoreCase(account.getAccountType()))
                .findFirst()
                .orElseThrow(() -> new Exception("No primary account found."));
    }

    private List<TransactionDto> fetchValidTransactions(AccountDto account) {
        String accountUuid = account.getAccountUid();

        // Assumption: We are fetching transactions from yesterday (last full day) to last week
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate lastWeek = yesterday.minusDays(7);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String minTimestamp = lastWeek.atStartOfDay().format(formatter);
        String maxTimestamp = yesterday.atTime(23, 59, 59).format(formatter);

        return starlingClient.fetchTransactions(accountUuid, minTimestamp, maxTimestamp)
                .stream()
                .filter(this::isValidTransaction)
                .toList();
    }

    private SavingsAccountDto ensureSavingsAccountExists(AccountDto account) throws Exception {
        String accountUuid = account.getAccountUid();
        return starlingClient.getSavingsGoals(accountUuid).stream()
                .filter(savingsAccount -> savingsAccount.getName().equals(SAVINGS_GOALS_NAME))
                .filter(savingsAccount -> savingsAccount.getState().equals("ACTIVE"))
                .findFirst()
                .orElse(null);
    }

    private boolean transferRoundUpToSavings(AccountDto account, SavingsAccountDto savingsAccount, int roundUpTotal) {
        return starlingClient.addMoneyToSavingsGoal(account.getAccountUid(), savingsAccount.getSavingsGoalUid(), roundUpTotal);
    }

    private BigDecimal convertToBigDecimal(int roundUpTotal) {
        BigDecimal formattedRoundUp = new BigDecimal(roundUpTotal).divide(new BigDecimal(100));

        LOGGER.debug("Converting {}, to {}", roundUpTotal, formattedRoundUp);
        return formattedRoundUp;
    }

    private int calculateRoundUp(List<TransactionDto> validTransactions) {
        int totalRoundUp = 0;
        for (TransactionDto transaction : validTransactions) {
            int minorUnits = transaction.getAmount().getMinorUnits();
            int remainder = minorUnits % 100;

            totalRoundUp += 100 - remainder;
        }

        return totalRoundUp;
    }

    private boolean isValidTransaction(TransactionDto transaction) {
        return transaction.getDirection().equalsIgnoreCase("OUT")
                && transaction.getAmount().getMinorUnits() > 0
                && transaction.getAmount().getCurrency().equalsIgnoreCase("GBP");
    }
}
