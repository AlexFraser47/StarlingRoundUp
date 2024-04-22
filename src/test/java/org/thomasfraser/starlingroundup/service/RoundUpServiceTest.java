package org.thomasfraser.starlingroundup.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.thomasfraser.starlingroundup.client.StarlingClient;
import org.thomasfraser.starlingroundup.dto.AccountDto;
import org.thomasfraser.starlingroundup.dto.AmountDto;
import org.thomasfraser.starlingroundup.dto.SavingsAccountDto;
import org.thomasfraser.starlingroundup.dto.TransactionDto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class RoundUpServiceTest {

    @Mock
    private StarlingClient starlingClient;

    @InjectMocks
    private RoundUpService roundUpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validSimpleRoundUpTest() throws Exception {
        when(starlingClient.fetchClientAccounts()).thenReturn(createValidAccount());
        when(starlingClient.fetchTransactions(any(), any(), any())).thenReturn(createValidTransaction());
        when(starlingClient.getSavingsGoals(any())).thenReturn(createValidSavingsAccount());
        when(starlingClient.addMoneyToSavingsGoal(any(), any(), anyLong())).thenReturn(true);

        BigDecimal expected = new BigDecimal(201).divide(new BigDecimal(100));

        assertEquals(expected, roundUpService.calculateAndTransferRoundUp());
        verify(starlingClient, times(1)).getSavingsGoals(any());
    }

    @Test
    void RoundUpOnlyHundredsTest() throws Exception {
        when(starlingClient.fetchClientAccounts()).thenReturn(createValidAccount());
        when(starlingClient.fetchTransactions(any(), any(), any())).thenReturn(createValidTransactionHundreds());

        assertEquals(BigDecimal.ZERO, roundUpService.calculateAndTransferRoundUp());
    }

    @Test
    void noAccountFoundRoundUpTest() throws Exception {
        when(starlingClient.fetchClientAccounts()).thenReturn(List.of(new AccountDto()));

        String expectedMessage = "No primary account found.";

        Exception exception = assertThrows(Exception.class, () -> roundUpService.calculateAndTransferRoundUp());

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void noValidTransactionsFoundReturnsZeroTest() throws Exception {
        when(starlingClient.fetchClientAccounts()).thenReturn(createValidAccount());
        when(starlingClient.fetchTransactions(any(), any(), any())).thenReturn(createInvalidTransactions());

        assertEquals(BigDecimal.ZERO, roundUpService.calculateAndTransferRoundUp());
    }

    @Test
    void createSavingsAccountIfNoneExistTest() throws Exception {
        when(starlingClient.fetchClientAccounts()).thenReturn(createValidAccount());
        when(starlingClient.fetchTransactions(any(), any(), any())).thenReturn(createValidTransaction());
        when(starlingClient.getSavingsGoals(any())).thenReturn(List.of()).thenReturn(createValidSavingsAccount());
        when(starlingClient.addMoneyToSavingsGoal(any(), any(), anyLong())).thenReturn(true);

        BigDecimal expected = new BigDecimal(201).divide(new BigDecimal(100));
        assertEquals(expected, roundUpService.calculateAndTransferRoundUp());

        verify(starlingClient, times(1)).createSavingsGoal(any(), any());
        verify(starlingClient, times(2)).getSavingsGoals(any());
    }

    @Test
    void createSavingsAccountIfInvalidAccountExistsTest() throws Exception {
        when(starlingClient.fetchClientAccounts()).thenReturn(createValidAccount());
        when(starlingClient.fetchTransactions(any(), any(), any())).thenReturn(createValidTransaction());
        when(starlingClient.getSavingsGoals(any())).thenReturn(createInvalidSavingsAccount()).thenReturn(createValidSavingsAccount());
        when(starlingClient.addMoneyToSavingsGoal(any(), any(), anyLong())).thenReturn(true);

        BigDecimal expected = new BigDecimal(201).divide(new BigDecimal(100));
        assertEquals(expected, roundUpService.calculateAndTransferRoundUp());

        verify(starlingClient, times(1)).createSavingsGoal(any(), any());
        verify(starlingClient, times(2)).getSavingsGoals(any());
    }

    @Test
    void transferRoundUpFailsTest() throws Exception {
        when(starlingClient.fetchClientAccounts()).thenReturn(createValidAccount());
        when(starlingClient.fetchTransactions(any(), any(), any())).thenReturn(createValidTransaction());
        when(starlingClient.getSavingsGoals(any())).thenReturn(createValidSavingsAccount());
        when(starlingClient.addMoneyToSavingsGoal(any(), any(), anyLong())).thenReturn(false);

        String expectedMessage = "Failed to transfer round up amount.";

        Exception exception = assertThrows(Exception.class, () -> roundUpService.calculateAndTransferRoundUp());

        assertTrue(exception.getMessage().contains(expectedMessage));
    }


    private List<SavingsAccountDto> createValidSavingsAccount() {
        SavingsAccountDto savingsAccountDto = new SavingsAccountDto();
        savingsAccountDto.setSavingsGoalUid("1234");
        savingsAccountDto.setName("RoundUp2025");
        savingsAccountDto.setState("ACTIVE");

        return List.of(savingsAccountDto);
    }

    private List<AccountDto> createValidAccount() {
        AccountDto accountDto = new AccountDto();
        accountDto.setAccountUid("1234");
        accountDto.setAccountType("PRIMARY");
        return List.of(accountDto);
    }

    private List<TransactionDto> createValidTransaction() {
        AmountDto amountDto1 = new AmountDto();
        amountDto1.setCurrency("GBP");
        amountDto1.setMinorUnits(1001);

        AmountDto amountDto2 = new AmountDto();
        amountDto2.setCurrency("GBP");
        amountDto2.setMinorUnits(0);

        AmountDto amountDto3 = new AmountDto();
        amountDto3.setCurrency("GBP");
        amountDto3.setMinorUnits(999);

        AmountDto amountDto4 = new AmountDto();
        amountDto4.setCurrency("GBP");
        amountDto4.setMinorUnits(50);

        AmountDto amountDto5 = new AmountDto();
        amountDto5.setCurrency("GBP");
        amountDto5.setMinorUnits(12345674249L);

        TransactionDto transactionDto1 = new TransactionDto();
        transactionDto1.setAmount(amountDto1);
        transactionDto1.setDirection("OUT");

        TransactionDto transactionDto2 = new TransactionDto();
        transactionDto2.setAmount(amountDto2);
        transactionDto2.setDirection("OUT");

        TransactionDto transactionDto3 = new TransactionDto();
        transactionDto3.setAmount(amountDto3);
        transactionDto3.setDirection("OUT");

        TransactionDto transactionDto4 = new TransactionDto();
        transactionDto4.setAmount(amountDto4);
        transactionDto4.setDirection("OUT");

        TransactionDto transactionDto5 = new TransactionDto();
        transactionDto5.setAmount(amountDto5);
        transactionDto5.setDirection("OUT");

        return List.of(transactionDto1, transactionDto2, transactionDto3, transactionDto4, transactionDto5);
    }

    private List<TransactionDto> createValidTransactionHundreds() {
        AmountDto amountDto1 = new AmountDto();
        amountDto1.setCurrency(null);
        amountDto1.setMinorUnits(5600);

        AmountDto amountDto2 = new AmountDto();
        amountDto2.setCurrency("GBP");
        amountDto2.setMinorUnits(45600);

        AmountDto amountDto3 = new AmountDto();
        amountDto3.setCurrency("GBP");
        amountDto3.setMinorUnits(900);

        AmountDto amountDto4 = new AmountDto();
        amountDto4.setCurrency("GBP");
        amountDto4.setMinorUnits(0);

        AmountDto amountDto5 = new AmountDto();
        amountDto5.setCurrency("GBP");
        amountDto5.setMinorUnits(12345674200L);

        TransactionDto transactionDto1 = new TransactionDto();
        transactionDto1.setAmount(amountDto1);
        transactionDto1.setDirection("OUT");

        TransactionDto transactionDto2 = new TransactionDto();
        transactionDto2.setAmount(amountDto2);
        transactionDto2.setDirection("OUT");

        TransactionDto transactionDto3 = new TransactionDto();
        transactionDto3.setAmount(amountDto3);
        transactionDto3.setDirection("OUT");

        TransactionDto transactionDto4 = new TransactionDto();
        transactionDto4.setAmount(amountDto4);
        transactionDto4.setDirection("OUT");

        TransactionDto transactionDto5 = new TransactionDto();
        transactionDto5.setAmount(amountDto5);
        transactionDto5.setDirection("OUT");

        return List.of(transactionDto1, transactionDto2, transactionDto3, transactionDto4, transactionDto5);
    }

    private List<TransactionDto> createInvalidTransactions() {
        AmountDto amountDto1 = new AmountDto();
        amountDto1.setCurrency("GBP");
        amountDto1.setMinorUnits(1001);

        AmountDto amountDto2 = new AmountDto();
        amountDto2.setCurrency("USD");
        amountDto2.setMinorUnits(1001);

        TransactionDto transactionDto1 = new TransactionDto();
        transactionDto1.setAmount(amountDto1);
        transactionDto1.setDirection("IN");

        TransactionDto transactionDto2 = new TransactionDto();
        transactionDto2.setAmount(amountDto2);
        transactionDto2.setDirection("OUT");

        return List.of(transactionDto1, transactionDto2);
    }

    private List<SavingsAccountDto> createInvalidSavingsAccount() {
        SavingsAccountDto savingsAccountDto = new SavingsAccountDto();
        savingsAccountDto.setSavingsGoalUid("4321");
        savingsAccountDto.setName("RoundUp2025");
        savingsAccountDto.setState("INACTIVE");

        SavingsAccountDto savingsAccountDto2 = new SavingsAccountDto();
        savingsAccountDto2.setSavingsGoalUid("2354");
        savingsAccountDto2.setName("DifferentName");
        savingsAccountDto2.setState("ACTIVE");

        return List.of(savingsAccountDto, savingsAccountDto2);
    }

}