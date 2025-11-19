package com.finance.loans.task;

import com.finance.loans.service.RepaymentScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 */
@Component
public class ScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    @Autowired
    private RepaymentScheduleService scheduleService;
    
    /**
     * 每天晚上21:00自动标记当天应还款项为已还
     */
    @Scheduled(cron = "0 0 21 * * ?")
    public void autoMarkTodayAsPaid() {
        logger.info("开始执行定时任务：标记当天应还款项为已还");
        try {
            int count = scheduleService.markTodayAsPaid();
            logger.info("定时任务执行成功，标记了 {} 笔还款", count);
        } catch (Exception e) {
            logger.error("定时任务执行失败", e);
        }
    }
}
