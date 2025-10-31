package com.finance.loans.controller;

import com.finance.loans.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 日历控制器
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 获取某月的还款统计
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getMonthSummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(calendarService.getMonthSummary(year, month));
    }

    /**
     * 获取某天的还款列表
     */
    @GetMapping("/day-payments")
    public ResponseEntity<List<Map<String, Object>>> getDayPayments(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        return ResponseEntity.ok(calendarService.getDayPayments(year, month, day));
    }
}

