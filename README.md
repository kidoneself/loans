# 贷款管理系统 v3.0 🚀

## 📋 项目简介

全新的贷款管理系统，采用现代化技术栈，提供直观、高效的贷款管理体验。

### ✨ 核心功能

- **贷款管理** - 新增、编辑、删除、提前还清
- **还款计划** - 自动生成、状态跟踪、还款记录
- **还款日历** - 直观的月历视图，查看每日还款安排
- **趋势分析** - 可视化负债变化、月还款额、贷款数量趋势
- **数据统计** - 实时统计剩余负债、月还款、活跃贷款数等

---

## 🛠️ 技术栈

### 后端
- **Spring Boot 3.2.0** - 核心框架
- **MyBatis 3.0.3** - 持久层框架
- **MySQL 8.0** - 数据库
- **Lombok** - 代码简化

### 前端
- **HTML5 + Bootstrap 5** - 界面框架
- **Chart.js 4.4.0** - 图表库
- **Bootstrap Icons** - 图标库

---

## 📦 快速开始

### 1. 数据库初始化

```bash
# 初始化数据库
mysql -u root -p --default-character-set=utf8mb4 < database-init-v3.0.sql

# 导入贷款数据（如果有）
mysql -u root -p --default-character-set=utf8mb4 loansv2 < import-data.sql
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/loansv2?useUnicode=true&characterEncoding=utf8mb4
    username: loans    # 修改为你的数据库用户名
    password: loans123 # 修改为你的数据库密码
```

### 3. 启动应用

```bash
# 方式1：使用 Maven
mvn clean package
mvn spring-boot:run

# 方式2：使用 IDE
直接运行 LoansApplication.java
```

### 4. 访问系统

浏览器打开：**http://localhost:8080**

---

## 📚 API 接口

### 贷款管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/loans` | 获取所有贷款 |
| GET | `/api/loans/{id}` | 获取贷款详情 |
| POST | `/api/loans` | 新增贷款 |
| PUT | `/api/loans/{id}` | 更新贷款 |
| DELETE | `/api/loans/{id}` | 删除贷款 |
| GET | `/api/loans/summary` | 获取统计摘要 |
| POST | `/api/loans/{id}/early-settlement` | 提前还清 |

### 还款计划

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/schedules/loan/{loanId}` | 获取贷款的还款计划 |
| GET | `/api/schedules/current-month` | 获取本月还款计划 |
| POST | `/api/schedules/{id}/pay` | 记录还款 |

### 负债快照

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/snapshots/latest` | 获取最新快照 |
| GET | `/api/snapshots/recent/{days}` | 获取最近N天快照 |
| POST | `/api/snapshots/create` | 手动创建快照 |

---

## 🗄️ 数据库设计

### 核心表结构

#### 1. loan（贷款表）
- `id` - 主键
- `name` - 贷款名称
- `platform` - 贷款平台
- `principal` - 借款本金
- `monthly_amount` - 月还款额
- `total_periods` - 总期数
- `payment_day` - 每月还款日
- `start_date` - 首期还款日期
- `status` - 状态（active/completed）

#### 2. repayment_schedule（还款计划表）
- `id` - 主键
- `loan_id` - 贷款ID
- `period` - 期数
- `due_date` - 应还日期
- `amount` - 应还金额
- `status` - 状态（pending/paid/overdue）
- `paid_date` - 实还日期
- `paid_amount` - 实还金额

#### 3. payment_record（还款记录表）
- `id` - 主键
- `loan_id` - 贷款ID
- `schedule_id` - 计划ID
- `amount` - 还款金额
- `payment_date` - 还款日期
- `payment_type` - 还款类型

#### 4. debt_snapshot（负债快照表）
- `id` - 主键
- `snapshot_date` - 快照日期
- `total_debt` - 总负债
- `total_principal` - 总本金
- `paid_amount` - 已还金额
- `remaining_amount` - 剩余金额
- `active_loans` - 活跃贷款数
- `monthly_payment` - 月还款额

---

## 🎨 页面说明

### 首页（index.html）
- 统计卡片展示关键指标
- 贷款列表卡片，带进度条和操作按钮
- 新增贷款表单
- 提前还清和删除确认

### 贷款详情（loan-detail.html）
- 贷款基本信息展示
- 还款进度可视化
- 还款计划列表（按状态筛选）
- 记录还款功能

### 还款日历（calendar.html）
- 月份切换
- 日历网格展示
- 每日还款计划标记（已还/待还/逾期）
- 点击日期查看详情

### 负债趋势（trend.html）
- 时间范围选择（7天/30天/90天）
- 负债变化趋势图
- 月还款额趋势图
- 贷款数量柱状图

---

## ⚙️ 特色功能

### 1. 自动还款计划生成
新增贷款时自动生成所有期数的还款计划，包括：
- 自动计算每期还款日期
- 根据当前日期自动标记历史期数为"已还"

### 2. 智能逾期检测
定时任务自动检测并更新逾期状态（触发器实现）

### 3. 负债快照
- 每日自动创建快照（定时任务）
- 支持手动创建快照
- 用于趋势分析

### 4. 提前还清
一键标记所有剩余期数为已还，自动更新贷款状态

---

## 🔧 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/loansv2
    username: loans
    password: loans123
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```

### 端口配置

```yaml
server:
  port: 8080  # 修改为你需要的端口
```

---

## 📝 使用建议

1. **首次使用**
   - 先初始化数据库
   - 导入现有贷款数据（如果有）
   - 手动创建初始快照

2. **日常维护**
   - 系统每天自动创建快照
   - 定期检查逾期状态
   - 及时记录还款

3. **数据备份**
   ```bash
   mysqldump -u root -p loansv2 > backup.sql
   ```

---

## 🐛 问题排查

### 数据库连接失败
- 检查 MySQL 是否启动
- 确认用户名密码正确
- 确认数据库 `loansv2` 已创建

### 中文乱码
- 确保数据库字符集为 `utf8mb4`
- 确保执行 SQL 时使用 `--default-character-set=utf8mb4`

### 快照数据为空
- 手动创建快照：POST `/api/snapshots/create`
- 等待定时任务自动创建（每天凌晨）

---

## 📄 许可证

MIT License

---

## 👨‍💻 开发者

@lizhiqiang

---

**享受高效的贷款管理体验！** 🎉
