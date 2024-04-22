package org.thomasfraser.starlingroundup.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.thomasfraser.starlingroundup.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Client class for interacting with the Starling API.
 */
@Service
public class StarlingClient {

    private static final Logger LOGGER = LogManager.getLogger(StarlingClient.class);

    @Value("${starling.api.baseUrl}")
    private String baseUrl;

    @Autowired
    private HttpHeaders headers;

    @Autowired
    private RestTemplate restTemplate;

    private static HttpEntity<SavingsGoalRequestDto> getSavingsGoalRequestDtoHttpEntity(HttpHeaders headers, String savingsGoalsName) {
        SavingsGoalRequestDto savingsGoalRequestDto = new SavingsGoalRequestDto();
        savingsGoalRequestDto.setName(savingsGoalsName);
        savingsGoalRequestDto.setCurrency("GBP");

        // Hardcoding the target amount for now
        AmountDto targetDto = new AmountDto();
        targetDto.setCurrency("GBP");
        targetDto.setMinorUnits(100000);

        savingsGoalRequestDto.setTarget(targetDto);
        savingsGoalRequestDto.setBase64EncodedPhoto("string");

        return new HttpEntity<>(savingsGoalRequestDto, headers);
    }

    public List<AccountDto> fetchClientAccounts() throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<AccountsResponseDto> response = restTemplate.exchange(
                baseUrl + "/accounts",
                HttpMethod.GET,
                entity,
                AccountsResponseDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().getAccounts() != null) {
            LOGGER.info("Successfully fetched {} accounts", response.getBody().getAccounts().size());
            return response.getBody().getAccounts();
        } else {
            throw new Exception("Failed to fetch accounts: " + response.getStatusCode());
        }
    }

    public List<TransactionDto> fetchTransactions(String accountUuid, String minTimestamp, String maxTimestamp) throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String urlTemplate = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/feed/account/" + accountUuid + "/settled-transactions-between")
                .queryParam("minTransactionTimestamp", minTimestamp)
                .queryParam("maxTransactionTimestamp", maxTimestamp)
                .toUriString();

        ResponseEntity<TransactionsResponseDto> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                entity,
                TransactionsResponseDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            LOGGER.info("Successfully fetched transactions: {}", response.getBody().getFeedItems().size());
            return response.getBody().getFeedItems();
        } else {
            throw new Exception("Failed to fetch transactions: " + response.getStatusCode());
        }
    }

    public void createSavingsGoal(String accountUuid, String savingsGoalsName) throws Exception {
        HttpEntity<SavingsGoalRequestDto> entity = getSavingsGoalRequestDtoHttpEntity(headers, savingsGoalsName);

        String urlTemplate = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/account/" + accountUuid + "/savings-goals")
                .toUriString();

        ResponseEntity<Void> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.PUT,
                entity,
                Void.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("Successfully created savings goal");
        } else {
            throw new Exception("Failed to create savings goal: " + response.getStatusCode());
        }
    }

    public List<SavingsAccountDto> getSavingsGoals(String accountUuid) throws Exception {
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String urlTemplate = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/account/" + accountUuid + "/savings-goals")
                .toUriString();

        ResponseEntity<SavingsAccountsResponseDto> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                entity,
                SavingsAccountsResponseDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            LOGGER.info("Successfully fetched savings goals: {}", response.getBody().getSavingsGoalList().size());
            return response.getBody().getSavingsGoalList();
        } else {
            throw new Exception("Failed to fetch savings goals: " + response.getStatusCode());
        }
    }

    public boolean addMoneyToSavingsGoal(String accountUuid, String savingsGoalUid, long roundUpTotal) {
        AmountDto amountDto = new AmountDto();
        amountDto.setCurrency("GBP");
        amountDto.setMinorUnits(roundUpTotal);

        TransferAmountDtoWrapper transferAmountDtoWrapper = new TransferAmountDtoWrapper();
        transferAmountDtoWrapper.setAmount(amountDto);

        HttpEntity<TransferAmountDtoWrapper> entity = new HttpEntity<>(transferAmountDtoWrapper, headers);

        // Generating a random transferUid, as it is required by the API
        String transferUid = UUID.randomUUID().toString();

        String urlTemplate = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/account/" + accountUuid + "/savings-goals/" + savingsGoalUid + "/add-money/" + transferUid)
                .toUriString();

        ResponseEntity<Void> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.PUT,
                entity,
                Void.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            LOGGER.info("Successfully added money to savings goal");
            return true;
        } else {
            LOGGER.error("Failed to add money to savings goal: {}", response.getStatusCode());
            return false;
        }
    }

}
