# Chat Backend - 后端项目功能文档

## 📋 项目概述

这是一个基于 Spring Boot 3.5 构建的AI聊天应用后端，提供流式聊天、消息存储等功能。使用 Ollama 作为AI模型服务，MySQL作为数据库存储。

## 🚀 技术栈

- **Spring Boot 3.5** - Java企业级开发框架
- **Spring AI** - AI集成框架，用于连接Ollama
- **MyBatis-Plus 3.6** - 数据持久化框架
- **MySQL 8.0** - 关系型数据库
- **Reactor** - 响应式编程，支持流式传输
- **Maven** - 项目构建和依赖管理

## 📁 项目结构

```
chat-back/
├── src/main/java/com/example/chat_back/
│   ├── ChatBackApplication.java          # 主启动类
│   ├── controller/
│   │   └── MessageController.java        # REST API控制器
│   ├── entity/
│   │   └── Message.java                  # 消息实体类
│   ├── mapper/
│   │   └── MessageMapper.java            # MyBatis数据访问层
│   └── service/
│       ├── IMessageService.java          # 服务接口
│       └── MessageServiceImpl.java       # 服务实现
├── src/main/resources/
│   ├── application.properties            # 应用配置
│   └── chat_db.sql                      # 数据库建表脚本
└── pom.xml                              # Maven配置
```

## 🔌 API接口

### 1. 获取消息历史
```http
GET /messages
```
**响应示例:**
```json
[
  {
    "id": 1,
    "sender": "user",
    "content": "你好",
    "createdAt": "2024-01-01 12:00:00"
  },
  {
    "id": 2,
    "sender": "assistant",
    "content": "你好！有什么可以帮助你的吗？",
    "createdAt": "2024-01-01 12:00:05"
  }
]
```

### 2. 流式聊天接口
```http
GET /stream?prompt={用户消息}
```
**说明:** 使用Server-Sent Events (SSE) 返回流式AI回复

**响应格式:**
```
event: message
data: Hello

event: message
data: !

event: message
data:  How

event: message
data: [DONE]
```

### 3. 健康检查
```http
GET /health
```
**响应示例:**
```json
{
  "status": "ok",
  "timestamp": 1672531200000
}
```

## 🗄️ 数据库设计

### messages 表
| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键ID | AUTO_INCREMENT |
| sender | VARCHAR(20) | 发送者类型 | NOT NULL ('user'/'assistant') |
| content | TEXT | 消息内容 | NOT NULL |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

## ⚙️ 配置说明

### application.properties 关键配置
```properties
# 服务端口
server.port=8080

# Ollama AI配置
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=deepseek-r1:1.5b

# MySQL数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/chat_db
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}

# MyBatis-Plus配置
mybatis-plus.mapper-locations=classpath:/mapper/*.xml
mybatis-plus.configuration.1og-impl=org.apache.ibatis.1ogging.stdout.stdoutImpl
logging.1evel.com.example.chat=DEBUG
```

## 🛠️ 核心功能

### 1. 消息流式传输
- 使用 `Flux<ServerSentEvent<String>>` 实现实时流式响应
- 支持前端逐字符接收AI回复，提升用户体验
- 自动发送 `[DONE]` 信号标识流结束

### 2. 消息持久化
- 自动保存用户消息和AI回复到数据库
- 使用MyBatis-Plus提供的基础CRUD操作
- 支持时间戳自动生成

### 3. 跨域支持
- 配置 `@CrossOrigin(origins = "*")` 支持前端跨域访问
- 适配开发环境下的前后端分离架构

## 🚀 启动方式

### 方式一：Maven启动
```bash
cd chat-back
mvn spring-boot:run
```

### 方式二：JAR包启动
```bash
cd chat-back
mvn package -DskipTests
java -jar target/chat_back-0.0.1-SNAPSHOT.jar
```

### 方式三：IDE启动
在IDE中直接运行 `ChatBackApplication.java` 主类

## 📋 前置要求

1. **Java 17+** - 项目基于Java 17编译
2. **MySQL 8.0+** - 需要运行MySQL数据库服务
3. **Ollama** - 需要在 `localhost:11434` 运行Ollama服务
4. **Maven 3.6+** - 用于项目构建

## 🔧 环境配置

### 1. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE chat_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE chat_db;

-- 创建消息表
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(20) NOT NULL COMMENT '发送者类型：user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_created_at (created_at),
    INDEX idx_sender (sender)
) COMMENT='聊天消息表';
```

### 2. Ollama模型配置
```bash
# 拉取模型
ollama pull deepseek-r1:1.5b

# 启动Ollama服务
ollama serve
```

## 🧪 测试验证

### 1. 健康检查
```bash
curl http://localhost:8080/health
```

### 2. 获取消息历史
```bash
curl http://localhost:8080/messages
```

### 3. 测试流式聊天
```bash
curl "http://localhost:8080/stream?prompt=hello"
```

## 📈 性能特性

- **轻量级架构**: 仅保留核心功能，去除冗余代码
- **响应式流处理**: 使用Reactor框架支持高并发
- **数据库连接池**: Spring Boot自动配置HikariCP
- **内存优化**: 流式传输避免大量数据积压

## 🔍 故障排除

### 常见问题

1. **端口占用**
   ```bash
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F
   ```

2. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 验证数据库用户名密码
   - 确认数据库已创建

3. **Ollama连接失败**
   - 检查Ollama服务是否在11434端口运行
   - 验证模型是否已下载
   - 测试 `curl http://localhost:11434/api/version`

## 📝 版本信息

- **当前版本**: 1.0.0
- **Spring Boot版本**: 3.5.4
- **Java版本要求**: 17+
- **最后更新**: 2024-08-25
