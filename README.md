# 🤖 AI 智能聊天系统

一个基于 **Vue 3 + Spring Boot + Ollama** 构建的现代化AI聊天应用，支持实时流式对话、消息历史持久化、AI思考过程可视化等功能。

## 📋 项目概述

本项目是一个完整的前后端分离的AI聊天系统，前端采用Vue 3构建用户界面，后端使用Spring Boot提供API服务，集成Ollama进行AI对话，使用MySQL存储聊天记录。

### 🎯 核心特性

- 🚀 **实时流式对话** - 基于SSE的流式AI回复，逐字符显示
- 💭 **思考过程可视化** - 解析并展示AI的思考过程
- 📱 **响应式设计** - 完美适配桌面和移动设备
- 💾 **消息持久化** - 自动保存和加载聊天历史
- 🎨 **现代化UI** - 基于Element Plus的美观界面
- ⚡ **高性能** - 前端响应式更新，后端异步处理

## 🏗️ 系统架构

```
┌─────────────────┐     HTTP/SSE     ┌─────────────────┐     HTTP API    ┌─────────────────┐
│   Vue 3 前端    │ ◄──────────────► │ Spring Boot后端 │ ◄─────────────► │   Ollama AI     │
│ localhost:5173  │                  │ localhost:8080  │                 │ localhost:11434 │
│                 │                  │                 │     SQL         │                 │
│  - ChatView     │                  │ - Controller    │ ◄─────────────► │   MySQL DB      │
│  - EventSource  │                  │ - Service       │                 │ localhost:3306  │
│  - Element Plus │                  │ - MyBatis-Plus  │                 │                 │
└─────────────────┘                  └─────────────────┘                 └─────────────────┘
```

## 🚀 技术栈

### 前端技术
- **Vue 3.4+** - 渐进式JavaScript框架
- **Element Plus 2.4+** - Vue 3 UI组件库
- **Vue Router 4.2+** - 官方路由管理器
- **Axios 1.6+** - HTTP客户端
- **Marked 16.2+** - Markdown解析
- **Vite 5.0+** - 现代前端构建工具

### 后端技术
- **Spring Boot 3.5.4** - Java微服务框架
- **Spring AI 1.0.1** - AI集成框架
- **MyBatis-Plus 3.5.11** - ORM框架
- **MySQL 8.0+** - 关系型数据库
- **Maven** - 项目管理工具

### AI模型
- **Ollama** - 本地LLM运行环境
- **DeepSeek-R1:1.5b** - 中文对话模型

## 📁 项目结构

```
chat/
├── chat-app/                          # Vue3 前端项目
│   ├── src/
│   │   ├── api/
│   │   │   └── chat.js                # API接口封装
│   │   ├── assets/
│   │   │   ├── 用户头像.png           # 用户头像
│   │   │   └── 机器人头像.png         # AI头像
│   │   ├── router/
│   │   │   └── index.js               # 路由配置
│   │   ├── views/
│   │   │   └── ChatView.vue           # 主聊天界面
│   │   ├── App.vue                    # 根组件
│   │   └── main.js                    # 应用入口
│   ├── package.json                   # 项目依赖
│   ├── vite.config.js                 # Vite配置
│   └── README.md                      # 前端详细文档
│
├── chat-back/                         # Spring Boot 后端项目
│   ├── src/main/java/com/example/chat_back/
│   │   ├── controller/
│   │   │   └── MessageController.java # 消息控制器
│   │   ├── entity/
│   │   │   └── Message.java           # 消息实体
│   │   ├── mapper/
│   │   │   └── MessageMapper.java     # MyBatis映射器
│   │   ├── service/
│   │   │   ├── IMessageService.java   # 服务接口
│   │   │   └── MessageServiceImpl.java# 服务实现
│   │   └── ChatBackApplication.java   # 启动类
│   ├── src/main/resources/
│   │   ├── application.properties     # 应用配置
│   │   └── chat_db.sql               # 数据库脚本
│   └── pom.xml                       # Maven配置
│
├── 前端后端连接与数据传递流程.md        # 架构详细说明
└── README.md                         # 项目总体说明(本文件)
```

## 🛠️ 环境搭建

### 1. 前置要求
- **Node.js 16+** 
- **Java 17+**
- **MySQL 8.0+**
- **Maven 3.6+**
- **Ollama** (用于AI模型)

### 2. 安装Ollama和模型
```bash
# 安装Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# 启动Ollama服务
ollama serve

# 下载DeepSeek模型
ollama pull deepseek-r1:1.5b
```

### 3. 数据库配置
```sql
-- 创建数据库
CREATE DATABASE chat_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER 'chat_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON chat_db.* TO 'chat_user'@'localhost';
FLUSH PRIVILEGES;

-- 导入表结构
USE chat_db;
SOURCE chat-back/src/main/resources/chat_db.sql;
```

### 4. 后端启动
```bash
cd chat-back

# 修改配置文件
vim src/main/resources/application.properties
# 更新数据库连接信息

# 编译启动
mvn clean package
mvn spring-boot:run

# 或者直接运行
java -jar target/chat_back-0.0.1-SNAPSHOT.jar
```

### 5. 前端启动
```bash
cd chat-app

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 🔌 API接口说明

### 1. 获取历史消息
```http
GET http://localhost:8080/messages
```
**响应**:
```json
[
  {
    "id": 1,
    "sender": "user",
    "content": "你好",
    "createdAt": "2024-08-25T15:30:00"
  },
  {
    "id": 2,
    "sender": "assistant",
    "content": "你好！有什么可以帮助你的吗？",
    "createdAt": "2024-08-25T15:30:05"
  }
]
```

### 2. 流式聊天接口
```http
GET http://localhost:8080/stream?prompt=你好
Content-Type: text/event-stream
```
**响应**:
```
event: message
data: 你

event: message  
data: 好

event: message
data: ！

event: message
data: [DONE]
```

### 3. 健康检查
```http
GET http://localhost:8080/health
```

## 🎨 核心功能详解

### 1. 实时流式对话
- 使用原生EventSource API接收SSE流
- 逐token显示AI回复，模拟真实打字效果
- 智能空格处理，解决模型tokenization问题
- 自动检测流结束信号并关闭连接

### 2. AI思考过程可视化
- 解析AI响应中的`<think>`标签
- 思考内容默认折叠，用户可点击展开
- 支持思考内容的Markdown渲染
- 提供思维过程的透明度

### 3. 消息历史管理
- 页面加载时自动获取历史对话
- 按时间升序显示，最新消息在底部
- 实时更新消息列表
- 数据库持久化存储

### 4. 响应式用户界面
- 基于Element Plus的现代化设计
- 完美适配桌面和移动设备
- 美观的头像和消息气泡
- 自动滚动到最新消息

## 🔧 配置说明

### 后端配置 (application.properties)
```properties
# 应用配置
spring.application.name=ollama-demo
server.port=8080

# Ollama AI配置
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=deepseek-r1:1.5b

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/chat_db?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis-Plus配置
mybatis-plus.mapper-locations=classpath:/mapper/*.xml
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdoutImpl

# 开发工具配置
spring.devtools.restart.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
```

### 前端配置 (package.json)
```json
{
  "name": "chat-app",
  "version": "1.0.0",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "element-plus": "^2.4.0",
    "axios": "^1.6.0",
    "vue-router": "^4.2.0",
    "marked": "^16.2.0"
  }
}
```

## 🚀 部署说明

### 1. 开发环境
```bash
# 启动Ollama
ollama serve

# 启动MySQL
sudo systemctl start mysql

# 启动后端 (端口8080)
cd chat-back && mvn spring-boot:run

# 启动前端 (端口5173)
cd chat-app && npm run dev
```

### 2. 生产环境
```bash
# 后端打包
cd chat-back
mvn clean package
java -jar target/chat_back-0.0.1-SNAPSHOT.jar

# 前端打包
cd chat-app
npm run build
# 将dist目录部署到Web服务器
```

### 3. Docker部署 (可选)
```dockerfile
# 后端Dockerfile示例
FROM openjdk:17-jdk-slim
COPY target/chat_back-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🧪 测试指南

### 1. 功能测试
- ✅ 页面加载和历史消息显示
- ✅ 发送消息和接收AI回复
- ✅ 流式响应和实时更新
- ✅ 思考内容解析和展示
- ✅ 响应式布局适配

### 2. API测试
```bash
# 测试历史消息接口
curl http://localhost:8080/messages

# 测试流式聊天
curl "http://localhost:8080/stream?prompt=你好"

# 测试健康检查
curl http://localhost:8080/health
```

### 3. 前端测试
```bash
cd chat-app
npm run dev
# 在浏览器中访问 http://localhost:5173
```

## 🐛 常见问题

### 1. Ollama连接失败
```bash
# 检查Ollama服务状态
ollama list
ollama serve

# 确认模型已下载
ollama pull deepseek-r1:1.5b
```

### 2. 数据库连接错误
- 检查MySQL服务是否启动
- 确认用户名密码正确
- 验证数据库是否存在

### 3. 跨域问题
- 后端已配置`@CrossOrigin(origins = "*")`
- 如仍有问题，检查防火墙设置

### 4. 前端构建失败
```bash
# 清除缓存重新安装
rm -rf node_modules package-lock.json
npm install
```

## 📊 性能优化

### 前端优化
- 使用Vue 3的Composition API
- 响应式数据更新策略
- 虚拟滚动处理长消息列表
- 代码分割和懒加载

### 后端优化
- Spring Boot异步处理
- 数据库连接池优化
- MyBatis-Plus查询优化
- 内存管理和垃圾回收调优

## 🔒 安全考虑

- 输入验证和XSS防护
- SQL注入防护 (MyBatis-Plus)
- 跨域请求控制
- 敏感信息加密存储

## 📈 功能扩展

### 计划中的功能
- 🔐 用户认证和权限管理
- 💬 多会话管理
- 🎨 主题和界面定制
- 📁 文件上传和处理
- 🌐 多语言支持
- 📊 对话数据分析

### 技术扩展
- Redis缓存集成
- WebSocket实时通信
- 微服务架构改造
- 容器化部署
- 监控和日志系统

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目作者：[Your Name]
- 邮箱：[your.email@example.com]
- 项目地址：[https://github.com/your-username/chat-system]

## 📚 相关文档

- [前端详细文档](chat-app/README.md)
- [后端API文档](http://localhost:8080/swagger-ui.html)
- [架构设计文档](前端后端连接与数据传递流程.md)
- [Vue 3 官方文档](https://vuejs.org/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Ollama 官方文档](https://ollama.ai/)

## 🎉 致谢

感谢以下开源项目的支持：
- Vue.js 团队
- Spring Boot 团队
- Element Plus 团队
- Ollama 团队
- MyBatis-Plus 团队

---

**⭐ 如果这个项目对您有帮助，请不要忘记给个Star！**

