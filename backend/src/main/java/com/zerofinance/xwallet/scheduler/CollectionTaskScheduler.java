package com.zerofinance.xwallet.scheduler;

import com.zerofinance.xwallet.mapper.CollectionTaskMapper;
import com.zerofinance.xwallet.model.entity.CollectionTask;
import com.zerofinance.xwallet.model.entity.LoanAccount;
import com.zerofinance.xwallet.model.entity.RepaymentSchedule;
import com.zerofinance.xwallet.mapper.LoanAccountMapper;
import com.zerofinance.xwallet.mapper.RepaymentScheduleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class CollectionTaskScheduler {

    @Autowired
    private CollectionTaskMapper collectionTaskMapper;

    @Autowired
    private RepaymentScheduleMapper repaymentScheduleMapper;

    @Autowired
    private LoanAccountMapper loanAccountMapper;

    @Scheduled(cron = "0 10 0 * * ?")
    public void dailyUpdateCollectionTasks() {
        log.info("Starting daily collection task update at 00:10");
        
        try {
            updateActiveTasks();
            autoGenerateCollectionTasks();
            log.info("Daily collection task update completed successfully");
        } catch (Exception e) {
            log.error("Error during daily collection task update", e);
        }
    }

    @Transactional
    public void updateActiveTasks() {
        log.info("Updating active collection tasks");
        
        List<CollectionTask> activeTasks = collectionTaskMapper.findActiveTasks();
        log.info("Found {} active collection tasks", activeTasks.size());
        
        int updatedCount = 0;
        int errorCount = 0;
        
        for (CollectionTask task : activeTasks) {
            try {
                updateTaskOverdueAmounts(task);
                updatedCount++;
            } catch (Exception e) {
                log.error("Error updating task {}: {}", task.getId(), e.getMessage());
                errorCount++;
            }
        }
        
        log.info("Updated {} tasks, {} errors", updatedCount, errorCount);
    }

    private void updateTaskOverdueAmounts(CollectionTask task) {
        LocalDate today = LocalDate.now();
        
        List<RepaymentSchedule> overdueSchedules = repaymentScheduleMapper
            .findOverdueSchedulesByLoanAccount(task.getLoanAccountId(), today);
        
        if (overdueSchedules.isEmpty()) {
            log.debug("No overdue schedules for task {}", task.getId());
            return;
        }
        
        BigDecimal overduePrincipal = BigDecimal.ZERO;
        BigDecimal baseInterest = BigDecimal.ZERO;
        LocalDate earliestDueDate = null;
        
        for (RepaymentSchedule schedule : overdueSchedules) {
            BigDecimal unpaidPrincipal = schedule.getPrincipalAmount()
                .subtract(schedule.getPaidPrincipal() != null ? schedule.getPaidPrincipal() : BigDecimal.ZERO);
            BigDecimal unpaidInterest = schedule.getInterestAmount()
                .subtract(schedule.getPaidInterest() != null ? schedule.getPaidInterest() : BigDecimal.ZERO);
            
            overduePrincipal = overduePrincipal.add(unpaidPrincipal);
            baseInterest = baseInterest.add(unpaidInterest);
            
            if (earliestDueDate == null || schedule.getDueDate().isBefore(earliestDueDate)) {
                earliestDueDate = schedule.getDueDate();
            }
        }
        
        int overdueDays = (int) ChronoUnit.DAYS.between(earliestDueDate, today);
        
        BigDecimal penaltyRate = task.getPenaltyRate() != null ? task.getPenaltyRate() : new BigDecimal("0.0005");
        BigDecimal penalty = overduePrincipal
            .multiply(penaltyRate)
            .multiply(new BigDecimal(overdueDays))
            .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal totalOverdueInterest = baseInterest.add(penalty);
        BigDecimal overdueTotal = overduePrincipal.add(totalOverdueInterest);
        
        task.setOverdueDays(overdueDays);
        task.setOverduePrincipal(overduePrincipal.setScale(2, RoundingMode.HALF_UP));
        task.setOverdueInterest(totalOverdueInterest.setScale(2, RoundingMode.HALF_UP));
        task.setOverdueTotal(overdueTotal.setScale(2, RoundingMode.HALF_UP));
        task.setPriority(CollectionTask.CollectionPriority.fromOverdueDays(overdueDays));
        task.setLastCalculatedAt(LocalDateTime.now());
        
        collectionTaskMapper.update(task);
        
        log.debug("Updated task {}: overdueDays={}, overdueTotal={}", 
            task.getId(), overdueDays, overdueTotal);
    }

    @Transactional
    public void autoGenerateCollectionTasks() {
        log.info("Auto-generating collection tasks for overdue accounts");
        
        LocalDate today = LocalDate.now();
        List<RepaymentSchedule> allOverdueSchedules = repaymentScheduleMapper.findOverdueSchedules(today);
        
        Set<Long> processedAccounts = new HashSet<>();
        int generatedCount = 0;
        int skippedCount = 0;
        
        for (RepaymentSchedule schedule : allOverdueSchedules) {
            Long accountId = schedule.getLoanAccountId();
            
            if (processedAccounts.contains(accountId)) {
                continue;
            }
            processedAccounts.add(accountId);
            
            List<CollectionTask> existingTasks = collectionTaskMapper.findByLoanAccountId(accountId);
            boolean hasActiveTask = existingTasks.stream()
                .anyMatch(task -> task.getStatus() == CollectionTask.CollectionStatus.PENDING ||
                                  task.getStatus() == CollectionTask.CollectionStatus.IN_PROGRESS ||
                                  task.getStatus() == CollectionTask.CollectionStatus.CONTACTED ||
                                  task.getStatus() == CollectionTask.CollectionStatus.PROMISED);
            
            if (hasActiveTask) {
                skippedCount++;
                continue;
            }
            
            LoanAccount account = loanAccountMapper.findById(accountId);
            if (account == null) {
                log.warn("Loan account not found for ID: {}", accountId);
                continue;
            }
            
            CollectionTask newTask = createCollectionTask(account, schedule, today);
            collectionTaskMapper.insert(newTask);
            generatedCount++;
            
            log.info("Generated collection task {} for account {}", newTask.getId(), accountId);
        }
        
        log.info("Generated {} new collection tasks, skipped {} accounts with active tasks", 
            generatedCount, skippedCount);
    }

    private CollectionTask createCollectionTask(LoanAccount account, RepaymentSchedule schedule, LocalDate today) {
        CollectionTask task = new CollectionTask();
        task.setLoanAccountId(account.getId());
        task.setCustomerId(account.getCustomerId());
        task.setContractNumber(schedule.getContractNumber());
        
        int overdueDays = (int) ChronoUnit.DAYS.between(schedule.getDueDate(), today);
        task.setOverdueDays(overdueDays);
        task.setPriority(CollectionTask.CollectionPriority.fromOverdueDays(overdueDays));
        
        task.setPenaltyRate(account.getPenaltyRate() != null ? account.getPenaltyRate() : new BigDecimal("0.0005"));
        task.setOverduePrincipal(BigDecimal.ZERO);
        task.setOverdueInterest(BigDecimal.ZERO);
        task.setOverdueTotal(BigDecimal.ZERO);
        task.setStatus(CollectionTask.CollectionStatus.PENDING);
        task.setLastCalculatedAt(LocalDateTime.now());
        
        return task;
    }
}
