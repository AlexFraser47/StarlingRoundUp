package org.thomasfraser.starlingroundup.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.thomasfraser.starlingroundup.dto.AccountDto;

import java.util.List;

public class JsonUtil {
    public static String convertListToJson(List<AccountDto> accounts) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(accounts);
        } catch (JsonProcessingException e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }
}
