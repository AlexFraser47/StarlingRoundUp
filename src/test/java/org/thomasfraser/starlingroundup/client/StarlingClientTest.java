package org.thomasfraser.starlingroundup.client;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.thomasfraser.starlingroundup.config.AppConfig;
import org.thomasfraser.starlingroundup.dto.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class StarlingClientTest {

    @InjectMocks
    private StarlingClient starlingClient;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AppConfig appConfig = new AppConfig();
        ReflectionTestUtils.setField(appConfig, "apiToken", "<api token>");
        ReflectionTestUtils.setField(starlingClient, "baseUrl", "https://api-sandbox.starling.com/api/v2");
    }

    @Test
    void fetchClientAccountsThrowsExceptionWhenResponseIsNotOkTest() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThrows(Exception.class, () -> starlingClient.fetchClientAccounts());
    }

    @Test
    void fetchClientAccountsReturnsAccountsWhenResponseIsOkTest() throws Exception {
        AccountsResponseDto accountsResponseDto = new AccountsResponseDto();
        accountsResponseDto.setAccounts(List.of(new AccountDto()));

        ResponseEntity<AccountsResponseDto> responseEntity = new ResponseEntity<>(accountsResponseDto, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(AccountsResponseDto.class)))
                .thenReturn(responseEntity);

        List<AccountDto> result = starlingClient.fetchClientAccounts();

        assertFalse(result.isEmpty());
    }

    @Test
    void fetchTransactionsThrowsExceptionWhenResponseIsNotOkTest() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThrows(Exception.class, ()-> starlingClient.fetchTransactions("uuid", "minTimestamp", "maxTimestamp"));
    }

    @Test
    void fetchTransactionsReturnsTransactionListWhenResponseIsOkTest() throws Exception {
        TransactionsResponseDto transactionsResponseDto = new TransactionsResponseDto();
        transactionsResponseDto.setFeedItems(List.of(new TransactionDto()));

        ResponseEntity<TransactionsResponseDto> responseEntity = new ResponseEntity<>(transactionsResponseDto, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(TransactionsResponseDto.class)))
                .thenReturn(responseEntity);

        List<TransactionDto> result = starlingClient.fetchTransactions("uuid", "minTimestamp", "maxTimestamp");

        assertFalse(result.isEmpty());
    }

    @Test
    void createSavingsGoalThrowsExceptionWhenResponseIsNotOkTest() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThrows(Exception.class, () -> starlingClient.createSavingsGoal("uuid", "savingsGoalsName"));
    }

    @Test
    void createSavingsGoalReturnsSuccessWhenResponseIsOkTest() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertDoesNotThrow(() -> starlingClient.createSavingsGoal("uuid", "savingsGoalsName"));
    }

    @Test
    void getSavingsGoalsThrowsExceptionWhenResponseIsNotOkTest() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertThrows(Exception.class, () -> starlingClient.getSavingsGoals("uuid"));
    }

    @Test
    void getSavingsGoalsReturnsSavingsGoalsWhenResponseIsOkTest() throws Exception {
        SavingsAccountsResponseDto savingsAccountsResponseDto = new SavingsAccountsResponseDto();
        savingsAccountsResponseDto.setSavingsGoalList(List.of(new SavingsAccountDto()));

        ResponseEntity<SavingsAccountsResponseDto> responseEntity = new ResponseEntity<>(savingsAccountsResponseDto, HttpStatus.OK);

        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), eq(SavingsAccountsResponseDto.class)))
                .thenReturn(responseEntity);

        List<SavingsAccountDto> result = starlingClient.getSavingsGoals("uuid");

        assertFalse(result.isEmpty());
    }

    @Test
    void addMoneyToSavingsGoalReturnsFalseWhenResponseIsNotOkTest() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        assertFalse(starlingClient.addMoneyToSavingsGoal("accountUuid", "savingsGoalUid", 100));
    }

    @Test
    void addMoneyToSavingsGoalReturnsTrueWhenResponseIsOkTest() {
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertTrue(starlingClient.addMoneyToSavingsGoal("accountUuid", "savingsGoalUid", 100));
    }
}