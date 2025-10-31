package com.finance.loans.service;

import com.finance.loans.mapper.LoanMapper;
import com.finance.loans.mapper.PaymentHistoryMapper;
import com.finance.loans.model.Loan;
import com.finance.loans.model.PaymentHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 贷款业务逻辑服务
 */
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanMapper loanMapper;
    private final PaymentHistoryMapper paymentHistoryMapper;
    private final BalanceService balanceService;

    /**
     * 获取所有贷款
     */
    public List<Loan> getAllLoans() {
        return loanMapper.findAll();
    }

    /**
     * 获取活跃的贷款
     */
    public List<Loan> getActiveLoans() {
        return loanMapper.findByStatus("active");
    }

    /**
     * 按平台分组获取贷款
     */
    public Map<String, List<Loan>> getLoansByPlatform() {
        return loanMapper.findAll().stream()
                .filter(loan -> loan.getPlatform() != null)
                .collect(Collectors.groupingBy(Loan::getPlatform));
    }

    /**
     * 根据ID获取贷款
     */
    public Loan getLoanById(Long id) {
        Loan loan = loanMapper.findById(id);
        if (loan == null) {
            throw new RuntimeException("贷款不存在：" + id);
        }
        return loan;
    }

    /**
     * 添加贷款
     */
    @Transactional
    public Loan addLoan(Loan loan) {
        if (loan.getStatus() == null) {
            loan.setStatus("active");
        }
        
        // 后端自动计算剩余金额
        if (loan.getTotalPeriods() != null && loan.getPaidPeriods() != null && loan.getMonthlyPayment() != null) {
            int remainingPeriods = loan.getTotalPeriods() - loan.getPaidPeriods();
            BigDecimal remainingAmount = loan.getMonthlyPayment().multiply(new BigDecimal(remainingPeriods));
            loan.setRemainingAmount(remainingAmount);
        }
        
        loanMapper.insert(loan);
        return loan;
    }

    /**
     * 更新贷款
     */
    @Transactional
    public Loan updateLoan(Long id, Loan loanDetails) {
        Loan loan = getLoanById(id);
        loan.setLoanName(loanDetails.getLoanName());
        loan.setPlatform(loanDetails.getPlatform());
        loan.setTotalAmount(loanDetails.getTotalAmount());
        loan.setMonthlyPayment(loanDetails.getMonthlyPayment());
        loan.setPaymentDay(loanDetails.getPaymentDay());
        loan.setTotalPeriods(loanDetails.getTotalPeriods());
        loan.setPaidPeriods(loanDetails.getPaidPeriods());
        loan.setStartDate(loanDetails.getStartDate());
        loan.setStatus(loanDetails.getStatus());
        loan.setNote(loanDetails.getNote());
        
        // 后端自动计算剩余金额
        if (loan.getTotalPeriods() != null && loan.getPaidPeriods() != null && loan.getMonthlyPayment() != null) {
            int remainingPeriods = loan.getTotalPeriods() - loan.getPaidPeriods();
            BigDecimal remainingAmount = loan.getMonthlyPayment().multiply(new BigDecimal(remainingPeriods));
            loan.setRemainingAmount(remainingAmount);
        } else {
            loan.setRemainingAmount(loanDetails.getRemainingAmount());
        }
        
        loanMapper.update(loan);
        return loan;
    }

    /**
     * 删除贷款
     */
    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = loanMapper.findById(id);
        if (loan == null) {
            throw new RuntimeException("贷款不存在：" + id);
        }
        loanMapper.deleteById(id);
    }

    /**
     * 记录还款
     */
    @Transactional
    public PaymentHistory recordPayment(Long loanId, BigDecimal amount, 
                                       LocalDate paymentDate, boolean autoDeduct, String note) {
        Loan loan = getLoanById(loanId);

        // 创建还款记录
        PaymentHistory payment = new PaymentHistory();
        payment.setLoanId(loanId);
        payment.setPaymentAmount(amount);
        payment.setPaymentDate(paymentDate);
        payment.setAutoDeductBalance(autoDeduct);
        payment.setNote(note);
        paymentHistoryMapper.insert(payment);

        // 更新贷款信息
        BigDecimal newRemaining = loan.getRemainingAmount().subtract(amount);
        loan.setRemainingAmount(newRemaining);
        
        if (loan.getPaidPeriods() != null) {
            loan.setPaidPeriods(loan.getPaidPeriods() + 1);
        }
        
        // 如果还清了，更新状态
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus("completed");
            loan.setRemainingAmount(BigDecimal.ZERO);
        }
        
        loanMapper.update(loan);

        // 如果自动扣减余额
        if (autoDeduct) {
            balanceService.deductBalance(amount, "payment", payment.getId(), 
                                        "还款：" + loan.getLoanName());
        }

        return payment;
    }

    /**
     * 获取总负债
     */
    public BigDecimal getTotalDebt() {
        List<Loan> activeLoans = loanMapper.findByStatus("active");
        return activeLoans.stream()
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取每月还款总额
     */
    public BigDecimal getMonthlyPaymentTotal() {
        List<Loan> activeLoans = loanMapper.findByStatus("active");
        return activeLoans.stream()
                .map(Loan::getMonthlyPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取某笔贷款的还款历史
     */
    public List<PaymentHistory> getPaymentHistory(Long loanId) {
        return paymentHistoryMapper.findByLoanId(loanId);
    }

    /**
     * 获取贷款汇总信息
     */
    public Map<String, Object> getLoanSummary() {
        List<Loan> allLoans = loanMapper.findAll();
        List<Loan> activeLoans = loanMapper.findByStatus("active");
        List<Loan> completedLoans = loanMapper.findByStatus("completed");

        List<String> platforms = allLoans.stream()
                .map(Loan::getPlatform)
                .filter(p -> p != null && !p.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        return Map.of(
            "totalLoans", allLoans.size(),
            "activeLoans", activeLoans.size(),
            "completedLoans", completedLoans.size(),
            "totalDebt", getTotalDebt(),
            "monthlyPayment", getMonthlyPaymentTotal(),
            "platforms", platforms
        );
    }

    /**
     * 获取平台汇总
     */
    public List<Map<String, Object>> getPlatformSummary() {
        List<Loan> activeLoans = loanMapper.findByStatus("active");
        
        Map<String, Map<String, Object>> platformMap = new java.util.HashMap<>();
        
        // 按平台分组统计
        for (Loan loan : activeLoans) {
            String platform = loan.getPlatform() != null ? loan.getPlatform() : "未分类";
            
            platformMap.putIfAbsent(platform, new java.util.HashMap<>());
            Map<String, Object> data = platformMap.get(platform);
            
            int count = (int) data.getOrDefault("count", 0) + 1;
            BigDecimal totalDebt = ((BigDecimal) data.getOrDefault("totalDebt", BigDecimal.ZERO))
                    .add(loan.getRemainingAmount());
            BigDecimal monthlyPayment = ((BigDecimal) data.getOrDefault("monthlyPayment", BigDecimal.ZERO))
                    .add(loan.getMonthlyPayment());
            
            data.put("platform", platform);
            data.put("count", count);
            data.put("totalDebt", totalDebt);
            data.put("monthlyPayment", monthlyPayment);
        }
        
        // 转换为列表并排序（按负债从高到低）
        return platformMap.values().stream()
                .sorted((a, b) -> ((BigDecimal) b.get("totalDebt")).compareTo((BigDecimal) a.get("totalDebt")))
                .collect(Collectors.toList());
    }

    /**
     * 获取贷款详情（含计算信息）
     */
    public Map<String, Object> getLoanDetail(Long id) {
        Loan loan = getLoanById(id);
        Map<String, Object> detail = new java.util.HashMap<>();
        
        // 基本信息
        detail.put("loan", loan);
        
        // 计算剩余期数
        int remainingPeriods = 0;
        if (loan.getTotalPeriods() != null && loan.getPaidPeriods() != null) {
            remainingPeriods = loan.getTotalPeriods() - loan.getPaidPeriods();
        }
        detail.put("remainingPeriods", remainingPeriods);
        
        // 计算已还总金额
        BigDecimal paidTotal = BigDecimal.ZERO;
        if (loan.getPaidPeriods() != null) {
            paidTotal = loan.getMonthlyPayment().multiply(new BigDecimal(loan.getPaidPeriods()));
        }
        detail.put("paidTotal", paidTotal);
        
        // 计算利息
        if (loan.getTotalAmount() != null && loan.getTotalPeriods() != null) {
            BigDecimal totalRepayment = loan.getMonthlyPayment().multiply(new BigDecimal(loan.getTotalPeriods()));
            BigDecimal totalInterest = totalRepayment.subtract(loan.getTotalAmount());
            detail.put("totalRepayment", totalRepayment);
            detail.put("totalInterest", totalInterest);
        }
        
        // 计算预计还清日期
        if (remainingPeriods > 0) {
            java.time.LocalDate payoffDate = java.time.LocalDate.now().plusMonths(remainingPeriods);
            detail.put("payoffDate", payoffDate);
        }
        
        return detail;
    }
}


