# 个人资金 & 网贷管理系统

> 一个简单易用的个人财务管理工具，帮助你清晰掌握资金状况，避免资金断流

## 📋 项目简介

这是一个基于 Java + Spring Boot + SQLite 的轻量级个人财务管理系统，专为有多笔贷款需要管理的用户设计。

**适用场景**：
- 同时有多个网贷平台的借款（拍拍贷、招联金融、信用飞、分期乐等）
- 每月还款日期不同，担心遗漏还款
- 想知道每月到底要还多少钱
- 想提前知道会不会出现资金缺口

**核心功能**：
- 💰 **贷款管理** - 管理多个网贷/信用卡账单，按平台分组查看
- 📊 **收入支出跟踪** - 记录月度收入和固定支出
- 💼 **余额管理** - 实时更新当前可用余额，追踪变动历史
- 🔮 **现金流预测** - 精确到日的预测，提前发现赤字风险
- 📅 **还款历史** - 记录每笔还款，追踪进度

## 🛠 技术栈

- **后端**: Java 21 + Spring Boot 3.x
- **数据库**: SQLite 3
- **前端**: HTML5 + Bootstrap 5 + JavaScript
- **构建**: Maven

## 📁 项目结构

```
loans/
├── pom.xml                          # Maven 配置文件
├── 需求文档.md                       # 详细功能需求文档
├── README.md                        # 项目说明文档
└── src/
    ├── main/
    │   ├── java/                    # Java 源代码
    │   │   └── com/finance/loans/
    │   │       ├── LoansApplication.java      # 启动类
    │   │       ├── config/                     # 配置类
    │   │       ├── controller/                 # REST API 控制器
    │   │       ├── model/                      # 实体类
    │   │       ├── repository/                 # 数据访问层
    │   │       ├── service/                    # 业务逻辑层
    │   │       └── dto/                        # 数据传输对象
    │   └── resources/
    │       ├── application.properties          # 应用配置
    │       ├── schema.sql                      # 数据库表结构
    │       └── static/                         # 静态资源
    │           ├── css/                        # 样式文件
    │           ├── js/                         # JavaScript 文件
    │           ├── index.html                  # 首页总览
    │           ├── loans.html                  # 贷款管理页
    │           ├── income-expenses.html        # 收入支出页
    │           └── forecast.html               # 月度预测页
    └── test/                                    # 测试代码
```

## 🚀 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本

### 安装步骤

1. **克隆项目** (如果从 Git)
   ```bash
   git clone <repository-url>
   cd loans
   ```

2. **构建项目**
   ```bash
   mvn clean install
   ```

3. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

4. **访问应用**
   
   打开浏览器访问：http://localhost:8080

### 首次使用

1. 首先在「余额管理」中设置当前可用余额
2. 在「收入支出管理」中添加固定收入和支出
3. 在「贷款管理」中添加你的各个贷款信息
4. 查看「月度预测」了解未来资金状况

## 📊 核心功能

### 1. 首页总览
- 一目了然查看当前财务状况
- 显示本月收入、支出、还款汇总
- 快速更新余额

### 2. 贷款管理
- 添加多个贷款（拍拍贷、花呗、信用卡等）
- 记录每笔贷款的总金额、剩余金额、每期还款、还款日
- 自动计算预计还清日期
- 支持手动更新还款进度

### 3. 收入支出管理
- 记录固定收入来源（工资、副业等）
- 记录固定支出（房租、水电、宽带等）
- 自动汇总每月收支

### 4. 月度预测
- 预测未来 3-12 个月现金流
- 自动识别可能出现赤字的月份
- 提前预警资金风险

## 🗃️ 数据库设计

### loans（贷款表）
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INTEGER | 主键 |
| loan_name | VARCHAR | 贷款名称 |
| platform | VARCHAR | 贷款平台 |
| total_amount | DECIMAL | 总借款金额（可选） |
| remaining_amount | DECIMAL | 剩余未还金额 |
| monthly_payment | DECIMAL | 每期还款金额 |
| payment_day | INTEGER | 每月还款日 |
| total_periods | INTEGER | 总期数（可选） |
| paid_periods | INTEGER | 已还期数 |
| status | VARCHAR | 状态 |

### income（收入表）
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INTEGER | 主键 |
| income_type | VARCHAR | 收入类型 |
| amount | DECIMAL | 收入金额 |
| income_day | INTEGER | 发薪日 |

### expenses_fixed（固定支出表）
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INTEGER | 主键 |
| expense_name | VARCHAR | 支出名称 |
| amount | DECIMAL | 支出金额 |
| expense_day | INTEGER | 支出日期 |

### balance（余额表）
| 字段 | 类型 | 说明 |
|-----|------|------|
| id | INTEGER | 主键 |
| current_balance | DECIMAL | 当前余额 |

## 🔌 API 接口

### 贷款相关
- `GET /api/loans` - 获取所有贷款
- `POST /api/loans` - 添加贷款
- `PUT /api/loans/{id}` - 更新贷款
- `DELETE /api/loans/{id}` - 删除贷款
- `GET /api/loans/summary` - 获取贷款汇总

### 收入相关
- `GET /api/income` - 获取所有收入
- `POST /api/income` - 添加收入
- `PUT /api/income/{id}` - 更新收入
- `DELETE /api/income/{id}` - 删除收入

### 支出相关
- `GET /api/expenses` - 获取所有支出
- `POST /api/expenses` - 添加支出
- `PUT /api/expenses/{id}` - 更新支出
- `DELETE /api/expenses/{id}` - 删除支出

### 其他
- `GET /api/balance` - 获取当前余额
- `POST /api/balance` - 更新余额
- `GET /api/forecast?months=12` - 获取月度预测
- `GET /api/dashboard` - 获取首页数据

## 📈 预测算法

系统采用简单而有效的预测算法：

```
初始余额 = 当前余额

for 每个月 in [1..12]:
    月收入 = sum(所有收入来源)
    月支出 = sum(所有固定支出)
    月还款 = sum(所有贷款每期还款)
    月结余 = 月收入 - 月支出 - 月还款
    月末余额 = 初始余额 + 月结余
    
    if 月末余额 < 0:
        标记为赤字预警
    
    初始余额 = 月末余额
```

## 🎨 界面预览

- **响应式设计**: 支持电脑、平板、手机访问
- **现代简洁**: 使用 Bootstrap 5 组件
- **直观清晰**: 重要数据突出显示
- **操作便捷**: 一键快速操作

## 🔐 数据安全

- 所有数据存储在本地 SQLite 数据库
- 无需联网，隐私安全
- 数据文件位置：`data/loans.db`
- 建议定期备份数据文件

## 📝 开发计划

### ✅ 已完成（85%）
- [x] 需求文档编写（已根据真实数据优化）
- [x] 技术选型确定
- [x] 数据库设计（5张表）
- [x] 项目结构规划
- [x] 真实使用场景设计
- [x] **后端完整实现**（Model + Repository + Service + Controller）
- [x] **前端核心页面**（首页 + 贷款管理 + 预测页面）
- [x] **现金流预测功能**（精确到日的预测算法）
- [x] **自动还款功能**（自动扣减余额）
- [x] **响应式UI设计**

### 🚧 进行中
- [ ] 收入支出管理页面（可选）
- [ ] 对话式录入功能（可选）

### 📅 可选扩展
- [ ] 图表可视化
- [ ] 数据导入导出
- [ ] 多用户支持
- [ ] 移动端APP

### 🎯 未来扩展
- [ ] 图表可视化（Chart.js）
- [ ] 数据导出（Excel/CSV）
- [ ] 还款日历视图
- [ ] 移动端 APP
- [ ] 多用户支持

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

## 👨‍💻 作者

- 李志强

## 📮 联系方式

如有问题或建议，欢迎联系！

---

**最后更新**: 2025-10-28

