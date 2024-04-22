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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
        long roundUpTotal = calculateRoundUp(transactions);

        if (roundUpTotal == 0) {
            LOGGER.info("No round up amount to transfer.");
            return BigDecimal.ZERO;
        }

        SavingsAccountDto savingsAccount = ensureSavingsAccountExists(account);
        if (savingsAccount == null) {
            starlingClient.createSavingsGoal(account.getAccountUid(), account.getCurrency(), SAVINGS_GOALS_NAME);
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

    private List<TransactionDto> fetchValidTransactions(AccountDto account) throws Exception {
        // Being safe here and checking for nulls
        String accountUuid = Optional.ofNullable(account.getAccountUid())
                .orElseThrow(() -> new Exception("Account UUID cannot be null"));

        String accountCurrency = Optional.ofNullable(account.getCurrency())
                .orElseThrow(() -> new Exception("Account currency cannot be null"));

        // Assumption: We are fetching transactions from curren time to 7 days ago.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String minTimestamp = sevenDaysAgo.format(formatter);
        String maxTimestamp = now.format(formatter);

        return starlingClient.fetchTransactions(accountUuid, minTimestamp, maxTimestamp)
                .stream()
                .filter(transaction -> isValidTransaction(transaction, accountCurrency))
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

    private boolean transferRoundUpToSavings(AccountDto account, SavingsAccountDto savingsAccount, long roundUpTotal) {
        return starlingClient.addMoneyToSavingsGoal(account.getAccountUid(), account.getCurrency(), savingsAccount.getSavingsGoalUid(), roundUpTotal);
    }

    private BigDecimal convertToBigDecimal(long roundUpTotal) {
        BigDecimal formattedRoundUp = new BigDecimal(roundUpTotal).divide(new BigDecimal(100));

        LOGGER.debug("Converting {}, to {}", roundUpTotal, formattedRoundUp);
        return formattedRoundUp;
    }

    private long calculateRoundUp(List<TransactionDto> validTransactions) {
        long totalRoundUp = 0;
        for (TransactionDto transaction : validTransactions) {
            long minorUnits = transaction.getAmount().getMinorUnits();
            if (minorUnits % 100 == 0) {
                continue;
            }
            long remainder = minorUnits % 100;
            totalRoundUp += 100 - remainder;
        }

        return totalRoundUp;
    }

    private boolean isValidTransaction(TransactionDto transaction, String accountCurrency) {
        return transaction.getDirection().equalsIgnoreCase("OUT")
                && transaction.getAmount().getMinorUnits() > 0
                && transaction.getAmount().getCurrency().equalsIgnoreCase(accountCurrency);
    }
}
