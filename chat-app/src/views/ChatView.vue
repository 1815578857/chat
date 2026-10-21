<template>
  <div class="chat-container">
    <!-- 头部 -->
    <el-header class="chat-header">
      <div class="header-content">
        <el-icon class="header-icon"><ChatDotRound /></el-icon>
        <h1>AI 智能聊天助手</h1>

      </div>
    </el-header>

    <!-- 聊天内容区域 -->
    <el-main class="chat-main">
      <div class="messages-container" ref="messagesContainer">
        <!-- 欢迎消息 -->
                 <div v-if="messages.length === 0" class="welcome-message">
           <el-card shadow="hover">
             <div class="welcome-content">
               <img :src="botAvatar" alt="AI头像" class="welcome-avatar" />
               <h3>欢迎使用 AI 聊天助手</h3>
               <p>我是基于 DeepSeek-R1 的智能助手，有什么可以帮您的吗？</p>
             </div>
           </el-card>
         </div>

        <!-- 消息列表 -->
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message-item"
          :class="{ 'user-message': message.sender === 'user', 'assistant-message':message.sender === 'assistant' }"
        >
          <div class="message-avatar">
            <img 
              :src="message.sender === 'user' ? userAvatar : botAvatar" 
              :alt="message.sender === 'user' ? '用户头像' : 'AI头像'"
              class="avatar-image"
            />
          </div>
          <div class="message-content">
            <div class="message-text">
              <!-- 如果是机器人消息，解析 think 标签并分段渲染 -->
              <span v-if="message.sender === 'assistant'">
                <span v-for="(part, idx) in parseMessageContent(message.content)" :key="idx" :class="part.isThink && part.text.trim() ? 'think-content' : ''">
                  <span v-if="part.isThink && part.text.trim()">
                    <!-- 思考内容的特殊样式 -->
                    <div class="thinking-section">
                      <div class="thinking-header" @click="toggleThinking">
                        <span class="thinking-icon">🤔</span>
                        <strong>AI 思考过程</strong>
                        <span class="thinking-toggle">▼</span>
                      </div>
                      <div class="thinking-content">
                        <span v-html="renderMarkdown(part.text)"></span>
                      </div>
                    </div>
                  </span>
                  <span v-else>
                    <!-- 普通内容的 Markdown 渲染 -->
                    <span v-html="renderMarkdown(part.text)"></span>
                  </span>
                  <br v-if="part.isThink && part.text.trim()"><!-- 在思考内容后添加换行 -->
                </span>
              </span>
              <!-- 如果不是机器人消息，直接显示内容 -->
              <span v-else>{{ message.content }}</span>
            </div>
            <div class="message-time">
              {{ formatTime(message.createdAt || message.timestamp) }}
            </div>
          </div>
        </div>


      </div>
    </el-main>

    <!-- 输入区域 -->
    <el-footer class="chat-footer">
      <div class="input-container">
        <el-input
          v-model="inputMessage"
          placeholder="请输入您的消息..."
          type="textarea"
          :rows="2"
          resize="none"
          @keydown.enter.prevent="handleKeyPress"
          :disabled="isLoading"
          class="message-input"
        />
        <el-button
          type="primary"
          @click="sendMessage"
          :loading="isLoading"
          :disabled="!inputMessage.trim()"
          class="send-button"
        >
          <el-icon><Promotion /></el-icon>
          发送
        </el-button>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'

import axios from 'axios'
import {
  ChatDotRound,
  Promotion
} from '@element-plus/icons-vue'
import userAvatar from '../assets/用户头像.png'
import botAvatar from '../assets/机器人头像.png'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

// Backend address, configurable via chat-app/.env (VITE_API_BASE_URL)
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 响应式数据
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)

// 组件挂载时的操作
onMounted(() => {
  loadHistory()
})

// 组件加载时获取历史聊天记录
const loadHistory = async () => {
  try {
    const response = await axios.get(`${API_BASE}/messages`);
    messages.value = response.data.sort((a, b) => new Date(a.created_at) - new Date(b.created_at));
    // 加载历史消息后滚动到底部显示最新消息
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error("获取历史消息失败:", error);
  }
};

// 发送消息并开始流式接收 AI 回复
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  // 设置加载状态
  isLoading.value = true

  // 先存储用户消息
  const userMsg = { sender: "user", content: inputMessage.value }
  messages.value.push(userMsg)

  const userText = inputMessage.value
  inputMessage.value = '' // 清空输入框

  // 添加一个空的 AI 消息
  const botMsg = { sender: "assistant", content: "" }
  messages.value.push(botMsg)
  
  // 发送消息后立即滚动到底部
  nextTick(() => scrollToBottom())

  // 使用 EventSource 流式接收数据
  const eventSource = new EventSource(`${API_BASE}/stream?prompt=${encodeURIComponent(userText)}`)

  // 监听流式返回数据，拼接 AI 回复
  eventSource.onmessage = (event) => {
    // 特殊处理结束信号
    if (event.data === '[DONE]') {
      eventSource.close()
      // 取消加载状态
      isLoading.value = false
      // 流式结束后，重新获取历史数据以确保正确渲染
      setTimeout(() => {
        loadHistory()
      }, 500) // 延迟500ms确保后端数据已保存
      return
    }
    
    let token = event.data
    
    // 智能添加空格：如果当前token是英文字母开头，且前一个内容不是空格、换行或标签结尾，则添加空格
    if (botMsg.content && 
        /^[a-zA-Z]/.test(token) && 
        !/[\s\n>]$/.test(botMsg.content) &&
        !/^[<]/.test(token)) {
      token = ' ' + token
    }
    
    botMsg.content += token // 拼接内容

    // 触发视图更新
    messages.value = [...messages.value]
    
    // 实时滚动到底部显示最新内容
    nextTick(() => scrollToBottom())
  }

  // 监听错误，关闭连接
  eventSource.onerror = () => {
    eventSource.close()
    // 出错时也要取消加载状态
    isLoading.value = false
  }
}

// 处理回车键
const handleKeyPress = (event) => {
  if (event.ctrlKey || event.shiftKey) {
    return // 允许换行
  }
  event.preventDefault()
  sendMessage()
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 渲染 Markdown 内容（经 DOMPurify 清洗，防止模型输出中的 HTML 造成 XSS）
const renderMarkdown = (content) => {
  return DOMPurify.sanitize(marked.parse(content || ''))
}

// 解析消息内容，分离 <think> 标签
const parseMessageContent = (content) => {
  const parts = []
  let remaining = content
  
  while (remaining.includes("<think>") && remaining.includes("</think>")) {
    const thinkStart = remaining.indexOf("<think>")
    const thinkEnd = remaining.indexOf("</think>") + "</think>".length
    
    // 添加思考前的内容（<think>之前的部分）
    if (thinkStart > 0) {
      parts.push({ text: remaining.slice(0, thinkStart), isThink: false })
    }
    
    // 提取 <think> 内容
    const thinkContent = remaining.slice(thinkStart + "<think>".length, thinkEnd - "</think>".length)
    parts.push({ text: thinkContent, isThink: true })
    
    // 剩余部分
    remaining = remaining.slice(thinkEnd)
  }
  
  // 添加剩余的普通内容
  if (remaining) {
    parts.push({ text: remaining, isThink: false })
  }
  
  return parts
}

// 切换思考内容的展开/折叠
const toggleThinking = (event) => {
  const thinkingSection = event.target.closest('.thinking-section')
  if (thinkingSection) {
    thinkingSection.classList.toggle('collapsed')
  }
}



// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  if (isNaN(date.getTime())) return ''
  
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}
</script>

<style scoped>
.chat-container {
  height: 92vh;
  max-width: 1000px;
  width: 90%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.chat-header {
  background: linear-gradient(135deg, #6c5ce7 0%, #a29bfe 100%);
  color: white;
  border-bottom: none;
  padding: 0;
  height: 70px !important;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 24px;
}

.header-content h1,
.header-icon {
  color: white;
}

.header-content h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.header-icon {
  font-size: 26px;
  margin-right: 12px;
}



.chat-main {
  flex: 1;
  padding: 0;
  overflow: hidden;
}

.messages-container {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
  background: #fafbfc;
}

.welcome-message {
  text-align: center;
  margin: 20px 0;
}

.welcome-content {
  text-align: center;
  padding: 20px;
}

.welcome-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border: 3px solid #409eff;
  background: #fff;
}

.welcome-content h3 {
  margin: 16px 0 8px 0;
  color: #333;
  font-size: 20px;
}

.welcome-content p {
  margin: 0;
  color: #666;
  font-size: 15px;
}

.message-item {
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
}

.user-message {
  flex-direction: row-reverse;
  margin-left: auto;
  margin-right: 0;
  max-width: 80%;
}

.assistant-message {
  flex-direction: row;
  margin-left: 0;
  margin-right: auto;
  max-width: 80%;
}

.message-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e0e0e0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

/* 用户头像样式 - 绿色主题 */
.user-message .avatar-image {
  border-color: #95ec69;
  background: #f0f9ff;
}

/* 机器人头像样式 - 蓝色主题 */
.assistant-message .avatar-image {
  border-color: #409eff;
  background: #fff;
}

/* 头像悬停效果 */
.avatar-image:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  word-wrap: break-word;
  min-width: 0;
}

.message-text {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 15px;
  word-break: break-word;
  display: inline-block;
  max-width: 100%;
  position: relative;
  width: fit-content;
  box-sizing: border-box;
  line-height: 1.6;
}

.user-message .message-text {
  background: #95ec69;
  color: #333;
}

.assistant-message .message-text {
  background: #f5f5f5;
  color: #333;
}

.message-time {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  margin-top: 6px;
  text-align: left;
}

.user-message .message-time {
  text-align: right;
  flex-direction: column;
  word-wrap: break-word;
  min-width: 0;
}

.message-text {
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 14px;
  word-break: break-word;
  display: inline-block;
  max-width: 100%;
  position: relative;
  width: fit-content;
  padding: 4px 8px;
  box-sizing: border-box;
  line-height: 1.4;
}

.message-text > *:first-child {
  margin-top: 0 !important;
}

.message-text > *:last-child {
  margin-bottom: 0 !important;
}

.message-text p,
.message-text li {
  line-height: 1.4;
}

.message-text p {
  margin: 0.2em 0;
}

.message-text h1,
.message-text h2,
.message-text h3,
.message-text h4,
.message-text h5,
.message-text h6 {
  margin: 0.4em 0 0.2em 0;
  font-weight: 600;
  line-height: 1.3;
}

.message-text h1 { font-size: 1.4em; }
.message-text h2 { font-size: 1.3em; }
.message-text h3 { font-size: 1.2em; }
.message-text h4 { font-size: 1.1em; }

.message-text ul,
.message-text ol {
  margin: 0.3em 0;
  padding-left: 1.5em;
}

.message-text li {
  margin: 0.1em 0;
}

.message-text strong {
  font-weight: 600;
}

.message-text code {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
  font-family: monospace;
}

.user-message .message-text {
  background: #95ec69;
}

.assistant-message .message-text {
  background: #f5f5f5;
}

.user-message .message-text,
.assistant-message .message-text {
  color: #333;
}

.message-time {
  font-size: 11px;
  color: rgba(0, 0, 0, 0.5);
  margin-top: 4px;
  text-align: left;
}

.user-message .message-time {
  text-align: right;
}



.chat-footer {
  background: white;
  border-top: 1px solid #e8e8e8;
  padding: 20px 24px;
  height: auto !important;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  font-size: 15px;
  line-height: 1.5;
  padding: 12px;
}

.send-button {
  height: 44px;
  min-width: 90px;
  font-size: 15px;
}

/* 思考过程样式 */
.thinking-section {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 1px solid #dee2e6;
  border-radius: 12px;
  margin: 8px 0;
  overflow: hidden;
  transition: all 0.3s ease;
}

.thinking-section.collapsed .thinking-content {
  display: none;
}

.thinking-section.collapsed .thinking-toggle {
  transform: rotate(-90deg);
}

.thinking-header {
  background: linear-gradient(135deg,rgb(212, 214, 215) 0%,rgb(188, 190, 192) 100%);
  color: white;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
}

.thinking-icon {
  font-size: 16px;
}

.thinking-toggle {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.3s ease;
  cursor: pointer;
}

.thinking-content {
  padding: 12px;
  background: #f8f9fa;
  font-size: 14px;
  line-height: 1.5;
  color: #495057;
  border-top: 1px solid #dee2e6;
}

/* 思考区域内的Markdown样式优化 */
:deep(.thinking-content h1, .thinking-content h2, .thinking-content h3, .thinking-content h4, .thinking-content h5, .thinking-content h6) {
  margin-top: 1em;
  margin-bottom: 0.3em;
  font-weight: 600;
  line-height: 1.3;
}

:deep(.thinking-content h1) { font-size: 1.8em; color: #2c3e50; }
:deep(.thinking-content h2) { font-size: 1.6em; color: #34495e; }
:deep(.thinking-content h3) { font-size: 1.4em; color: #34495e; }
:deep(.thinking-content h4) { font-size: 1.2em; color: #34495e; }

:deep(.thinking-content p) {
  margin: 0.3em 0;
  line-height: 1.4;
}

/* 其他全局样式只作用于思考区域 */
:deep(.thinking-content ul, .thinking-content ol) {
  margin: 0.6em 0;
  padding-left: 2em;
}

:deep(.thinking-content li) {
  margin-bottom: 0.3em;
}

:deep(.thinking-content blockquote) {
  border-left: 4px solid #409eff;
  margin: 0.6em 0;
  padding: 0.4em 0.8em;
  background: #f8f9fa;
  border-radius: 4px;
}

:deep(.thinking-content code) {
  background: #f8f9fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
  color: #e83e8c;
}

:deep(.thinking-content pre) {
  background: #f8f9fa;
  padding: 0.8em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 0.6em 0;
}

/* 美化消息区域滚动条 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    height: 100vh;
    width: 100%;
    position: fixed;
    top: 0;
    left: 0;
    transform: none;
    border-radius: 0;
  }
  
  .header-content {
    padding: 0 16px;
  }
  
  .header-content h1 {
    font-size: 18px;
  }
  
  .messages-container {
    padding: 16px;
  }
  
  .user-message,
  .assistant-message {
    max-width: 90%;
  }
  
  .chat-footer {
    padding: 16px;
  }
  
  .input-container {
    gap: 8px;
  }
}

/* 消息动画效果 */
.message-item {
  animation: fadeInUp 0.3s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 改善消息气泡样式 */
.message-text {
  position: relative;
  font-size: 16px;
  line-height: 1.6;
}

/* 移除箭头指示器 */

/* 加载状态改善 */
.send-button:loading {
  background: #a0cfff;
}

/* 输入框聚焦效果 */
.message-input:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}
</style>


