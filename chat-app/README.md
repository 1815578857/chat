# Chat Frontend - 前端项目功能文档

## 📋 项目概述

这是一个基于 Vue 3 构建的现代化AI聊天应用前端，提供实时流式聊天、消息历史、Markdown渲染等功能。采用组合式API设计，界面美观且响应式适配。

## 🚀 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Element Plus** - Vue 3 UI组件库
- **Axios** - HTTP客户端库
- **Vite** - 现代化前端构建工具
- **Vue Router** - 官方路由管理器
- **Marked** - Markdown解析库

## 📁 项目结构

```
chat-app/
├── src/
│   ├── api/
│   │   └── chat.js                      # API接口配置
│   ├── assets/
│   │   ├── 用户头像.png                 # 用户头像图片
│   │   └── 机器人头像.png               # AI头像图片
│   ├── router/
│   │   └── index.js                     # 路由配置
│   ├── views/
│   │   └── ChatView.vue                 # 主聊天界面
│   ├── App.vue                          # 根组件
│   └── main.js                          # 应用入口
├── index.html                           # HTML模板
├── package.json                         # 项目配置
├── vite.config.js                       # Vite配置
└── README.md                            # 项目说明
```

## 🎨 核心功能

### 1. 实时流式聊天
- **EventSource集成**: 使用原生EventSource API接收后端SSE流
- **逐字符显示**: AI回复逐个token显示，模拟真实打字效果
- **智能空格处理**: 自动在英文单词间添加空格，解决模型tokenization问题
- **流结束检测**: 监听`[DONE]`信号自动关闭连接

### 2. 消息历史管理
- **自动加载**: 页面加载时自动获取历史对话记录
- **时间排序**: 按创建时间升序显示，最新消息在底部
- **动态更新**: 发送新消息时实时更新列表

### 3. <think>标签解析
- **思考过程展示**: 解析AI响应中的`<think>`标签
- **可折叠显示**: 思考内容默认折叠，用户可点击展开
- **Markdown渲染**: 思考内容和回复内容都支持Markdown格式

### 4. 用户界面特性
- **响应式设计**: 适配桌面和移动设备
- **美观头像**: 用户和AI使用不同头像区分
- **自动滚动**: 新消息自动滚动到底部
- **加载状态**: 发送消息时显示加载状态

## 🔌 API集成

### 后端接口调用
```javascript
// 获取历史消息
const response = await axios.get("http://localhost:8080/messages")

// 流式聊天
const eventSource = new EventSource(`http://localhost:8080/stream?prompt=${encodeURIComponent(message)}`)
```

### 流式数据处理
```javascript
eventSource.onmessage = (event) => {
  if (event.data === '[DONE]') {
    eventSource.close()
    return
  }
  
  let token = event.data
  
  // 智能添加空格
  if (botMsg.content && 
      /^[a-zA-Z]/.test(token) && 
      !/[\s\n>]$/.test(botMsg.content) &&
      !/^[<]/.test(token)) {
    token = ' ' + token
  }
  
  botMsg.content += token
}
```

## 🎯 组件功能详解

### ChatView.vue 主要方法

#### 1. loadHistory()
```javascript
const loadHistory = async () => {
  try {
    const response = await axios.get("http://localhost:8080/messages");
    messages.value = response.data.sort((a, b) => new Date(a.created_at) - new Date(b.created_at));
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error("获取历史消息失败:", error);
  }
};
```

#### 2. sendMessage()
```javascript
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  // 添加用户消息
  const userMsg = { sender: "user", content: inputMessage.value }
  messages.value.push(userMsg)

  // 创建AI消息容器
  const botMsg = { sender: "bot", content: "" }
  messages.value.push(botMsg)
  
  // 建立SSE连接
  const eventSource = new EventSource(`http://localhost:8080/stream?prompt=${encodeURIComponent(userText)}`)
  
  // 处理流式响应...
}
```

#### 3. parseMessageContent()
```javascript
const parseMessageContent = (content) => {
  if (!content.includes('<think>')) {
    return { normalContent: content, thinkSections: [] }
  }
  
  // 解析think标签和普通内容...
}
```

#### 4. 滚动控制
```javascript
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}
```

## 🎨 样式设计

### 渐变背景
```css
.chat-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}
```

### 消息气泡
```css
.user-message {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-left: auto;
}

.bot-message {
  background: white;
  color: #333;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
```

### 响应式适配
```css
@media (max-width: 768px) {
  .chat-container {
    margin: 10px;
    border-radius: 10px;
  }
}
```

## 🚀 启动和构建

### 开发环境启动
```bash
cd chat-app
npm install
npm run dev
```
**访问地址**: http://localhost:5173

### 生产环境构建
```bash
npm run build
npm run preview
```

### 项目依赖
```json
{
  "dependencies": {
    "vue": "^3.3.0",
    "element-plus": "^2.4.0",
    "axios": "^1.5.0",
    "vue-router": "^4.2.0",
    "marked": "^9.0.0"
  },
  "devDependencies": {
    "vite": "^4.4.0",
    "@vitejs/plugin-vue": "^4.3.0"
  }
}
```

## 🔧 配置说明

### Vite配置 (vite.config.js)
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // 使用Vite默认端口5173，无需额外配置
})
```

### 路由配置 (router/index.js)
```javascript
import { createRouter, createWebHistory } from 'vue-router'
import ChatView from '../views/ChatView.vue'

const routes = [
  {
    path: '/',
    name: 'Chat',
    component: ChatView
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
```

## 🎯 用户体验特性

### 1. 智能交互
- **回车发送**: 支持Enter键快速发送消息
- **Ctrl+Enter换行**: 支持多行文本输入
- **自动聚焦**: 发送后自动聚焦输入框

### 2. 视觉反馈
- **发送状态**: 发送时禁用按钮显示加载状态
- **思考展示**: AI思考过程可视化展示
- **平滑滚动**: 消息更新时平滑滚动到底部

### 3. 错误处理
- **网络异常**: 自动重试和错误提示
- **连接超时**: SSE连接超时自动关闭
- **数据验证**: 输入内容验证和提示

## 📱 响应式设计

### 桌面端 (>768px)
- 宽度：800px居中显示
- 高度：600px固定高度
- 侧边距：自动居中

### 移动端 (≤768px)
- 宽度：calc(100% - 20px)
- 高度：calc(100vh - 20px)
- 边距：10px全边距

## 🔍 性能优化

### 1. 渲染优化
- **v-for键值**: 使用消息ID作为唯一键
- **计算属性**: 缓存复杂计算结果
- **按需更新**: 只在必要时触发重新渲染

### 2. 内存管理
- **EventSource清理**: 组件销毁时关闭连接
- **事件监听清理**: 及时移除事件监听器
- **防抖优化**: 防止频繁操作

### 3. 加载优化
- **异步组件**: 路由级别的代码分割
- **资源预加载**: 关键资源优先加载
- **图片优化**: 头像图片压缩优化

## 🧪 功能测试

### 1. 基础功能测试
- ✅ 页面加载和历史消息显示
- ✅ 发送消息和接收AI回复
- ✅ 流式响应和实时更新
- ✅ 思考内容解析和展示

### 2. 交互测试
- ✅ 键盘快捷键操作
- ✅ 移动端触摸操作
- ✅ 滚动和布局适配

### 3. 异常情况测试
- ✅ 网络断开重连
- ✅ 后端服务异常
- ✅ 超长消息处理

## 📝 版本信息

- **当前版本**: 1.0.0
- **Vue版本**: 3.3+
- **Node.js要求**: 16+
- **浏览器支持**: Chrome 90+, Firefox 88+, Safari 14+
- **最后更新**: 2024-08-25

## 🔗 相关链接

- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/)
- [Vite 构建工具](https://vitejs.dev/)
- [Axios HTTP客户端](https://axios-http.com/)
