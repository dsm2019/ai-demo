interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
}

class ChatService {
  /**
   * 发送消息到AI助手
   * Send message to AI assistant
   * @param message 用户消息 User message
   * @returns 服务器发送事件流 Server Sent Events stream
   */
  async chat(message: string): Promise<ReadableStream<Uint8Array> | Response> {
    try {
        // 使用 fetch API 直接处理流式响应，避免 axios 的限制
        const response = await fetch(`/api/ai/chat?message=${encodeURIComponent(message)}`)
        if (!response.body) {
        throw new Error('Response body is null')
        }
        return response
    } catch (error) {
        console.error('Chat service error:', error)
        throw error
    }
  }

  /**
   * 格式化消息对象
   * Format message object
   * @param content 消息内容 Message content
   * @param role 消息角色 Message role
   * @returns 格式化后的消息对象 Formatted message object
   */
  formatMessage(content: string, role: 'user' | 'assistant'): ChatMessage {
    return {
      role,
      content,
      timestamp: new Date()
    }
  }
}

export default new ChatService()