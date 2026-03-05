package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CollectionRecord {
    
    private Long id;
    private Long collectionTaskId;
    private Long operatorId;
    private ContactMethod contactMethod;
    private ContactResult contactResult;
    private LocalDateTime contactTime;
    private String notes;
    private String nextAction;
    private LocalDate nextContactDate;
    private LocalDateTime createdAt;

    public enum ContactMethod {
        PHONE,
        SMS,
        EMAIL,
        VISIT,
        OTHER
    }

    public enum ContactResult {
        NO_ANSWER,
        PROMISED,
        REFUSED,
        UNREACHABLE,
        WRONG_NUMBER,
        OTHER
    }
}
