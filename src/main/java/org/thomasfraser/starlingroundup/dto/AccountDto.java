package org.thomasfraser.starlingroundup.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto {
    private String accountUid;
    private String accountType;
    private String defaultCategory;
    private String currency;
    private String createdAt;
    private String name;

}
