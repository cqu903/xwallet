package com.zerofinance.xwallet.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CollectionTask {
    
    private Long id;
    private Long loanAccountId;
    private Long customerId;
    private String contractNumber;
    private Integer overdueDays;
    private BigDecimal overduePrincipal;
    private BigDecimal overdueInterest;
    private BigDecimal overdueTotal;
    private BigDecimal penaltyRate;
    private LocalDateTime lastCalculatedAt;
    private CollectionStatus status;
    private Long assignedTo;
    private CollectionPriority priority;
    private LocalDate lastContactDate;
    private LocalDate nextContactDate;
    private BigDecimal promiseAmount;
    private LocalDate promiseDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CollectionStatus {
        PENDING,
        IN_PROGRESS,
        CONTACTED,
        PROMISED,
        PAID,
        CLOSED
    }

    public enum CollectionPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT;

        public static CollectionPriority fromOverdueDays(int overdueDays) {
            if (overdueDays >= 90) return URGENT;
            if (overdueDays >= 61) return HIGH;
            if (overdueDays >= 31) return MEDIUM;
            return LOW;
        }
    }
}
