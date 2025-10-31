package com.finance.loans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 个人资金 & 网贷管理系统 - 主启动类
 * 
 * @author lizhiqiang
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class LoansApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoansApplication.class, args);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ 个人资金 & 网贷管理系统启动成功！");
        System.out.println("🌐 访问地址: http://localhost:8080");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}

