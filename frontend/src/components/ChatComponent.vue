<script setup lang="ts">
import { ref, reactive, nextTick } from 'vue'
import chatService from '../services/chatService'

interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const chatContainer = ref<HTMLElement | null>(null)

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  // 添加用户消息到聊天记录
  const userMessage: Message = {
    id: Date.now(),
    role: 'user',
    content: inputMessage.value,
    timestamp: new Date()
  }
  
  messages.value.push(userMessage)
  
  // 清空输入框
  const userMessageContent = inputMessage.value
  inputMessage.value = ''
  
  // 设置加载状态
  isLoading.value = true
  
  // 滚动到最新消息
  scrollToBottom()
  
  try {
    // 创建助手消息占位符
    const assistantMessage: Message = {
      id: Date.now() + 1,
      role: 'assistant',
      content: '',
      timestamp: new Date()
    }
    messages.value.push(assistantMessage)
    
    // 使用 chatService 发送消息
    const response = await chatService.chat(userMessageContent) as Response
    
    if (!response.body) {
      throw new Error('Response body is null')
    }
    
    // 处理 SSE 流式响应
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    
    // 更新助手消息内容
    let assistantMessageIndex = messages.value.length - 1
    
    let buffer = ''
    // 读取流数据
    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        break
      }
      
      // 解码数据
      buffer += decoder.decode(value, { stream: true })
      
      // 处理 SSE 格式的数据，但保留原始格式包括换行符
      // 查找完整的 SSE 消息块（以 \n\n 结尾）
      while (buffer.includes('\n\n')) {
        const endIndex = buffer.indexOf('\n\n') + 2
        const messageBlock = buffer.substring(0, endIndex)
        buffer = buffer.substring(endIndex)
        
        // 处理消息块中的每一行
        const lines = messageBlock.split('\n')
        for (const line of lines) {
          // 检查是否是 SSE 数据行
          if (line.startsWith('data:')) {
            // 提取实际数据内容（移除 'data:' 前缀）
            const data = line.slice(5)
            // 更新助手消息内容，保留原始数据中的换行符
            if (assistantMessageIndex < messages.value.length) {
              // 修复：确保换行符被正确处理
              messages.value[assistantMessageIndex].content += data + '\n'
              scrollToBottom()
            }
          }
          // 忽略其他 SSE 字段如 'event:', 'id:', 'retry:' 等
        }
      }
    }
    
    // 处理剩余的缓冲区数据
    if (buffer) {
      const lines = buffer.split('\n')
      for (const line of lines) {
        // 检查是否是 SSE 数据行
        if (line.startsWith('data:')) {
          // 提取实际数据内容（移除 'data:' 前缀）
          const data = line.slice(5)
          // 更新助手消息内容，保留原始数据中的换行符
          if (assistantMessageIndex < messages.value.length) {
            // 修复：确保换行符被正确处理
            messages.value[assistantMessageIndex].content += data + '\n'
            scrollToBottom()
          }
        }
        // 忽略其他 SSE 字段如 'event:', 'id:', 'retry:' 等
        // 也忽略空行
      }
    }
    
    reader.releaseLock()
  } catch (error) {
    console.error('Error sending message:', error)
    // 显示错误消息
    if (messages.value.length > 0) {
      const lastIndex = messages.value.length - 1
      messages.value[lastIndex].content = '抱歉，发送消息时出现错误，请稍后重试。'
    }
  } finally {
    isLoading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// 处理回车键发送消息
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2>AI 助手</h2>
    </div>
    
    <div ref="chatContainer" class="chat-messages">
      <div 
        v-for="message in messages" 
        :key="message.id"
        :class="['message', message.role]"
      >
        <div class="message-content">
          <div class="avatar">
            {{ message.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="text">
            <div class="message-content-text" v-if="message.content">{{ message.content }}</div>
          </div>
        </div>
      </div>
      
      <div v-if="isLoading" class="message assistant">
        <div class="message-content">
          <div class="avatar">🤖</div>
          <div class="text">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <textarea
        v-model="inputMessage"
        :disabled="isLoading"
        placeholder="输入消息..."
        @keydown="handleKeydown"
      ></textarea>
      <button 
        @click="sendMessage" 
        :disabled="!inputMessage.trim() || isLoading"
        class="send-button"
      >
        发送
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 66.67%; /* 设置为屏幕宽度的三分之二 */
  width: 66.67%; /* 设置为屏幕宽度的三分之二 */
  margin: 0 auto;
  background-color: #f5f5f5;
}

.chat-header {
  padding: 20px;
  background-color: #4a6fa5;
  color: white;
  text-align: center;
}

.chat-header h2 {
  margin: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  display: flex;
  max-width: 100%;
  width: 100%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
  width: auto; /* 改为自动宽度 */
}

.message.assistant {
  align-self: flex-start;
  width: auto; /* 改为自动宽度 */
}

.message-content {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  width: auto; /* 改为自动宽度 */
}

.message.user .message-content {
  flex-direction: row-reverse;
}

.avatar {
  font-size: 24px;
  flex-shrink: 0;
}

.text {
  background-color: white;
  padding: 12px 16px;
  border-radius: 18px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  white-space: pre-wrap;
  word-break: break-word;
  max-width: 80%; /* 限制最大宽度 */
}

.message-content-text {
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
  padding: 0;
}

.message.user .text {
  background-color: #4a6fa5;
  color: white;
  text-align: left; /* 改为左对齐 */
}

.chat-input {
  padding: 20px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.chat-input textarea {
  flex: 1;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 18px;
  resize: none;
  height: 60px;
  font-family: inherit;
}

.chat-input textarea:focus {
  outline: none;
  border-color: #4a6fa5;
}

.send-button {
  padding: 12px 24px;
  background-color: #4a6fa5;
  color: white;
  border: none;
  border-radius: 18px;
  cursor: pointer;
  font-weight: bold;
}

.send-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.send-button:hover:not(:disabled) {
  background-color: #3a5a80;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background-color: #999;
  border-radius: 50%;
  display: inline-block;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-5px);
  }
}
</style>