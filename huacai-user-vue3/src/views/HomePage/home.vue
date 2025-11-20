<template>
  <div class="home-page">
    <div class="chat-container">
      <div class="chat-history" ref="chatHistory">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">🐱💬</div>
          <h3>喵呜~我是青紫猫娘编程助手♪</h3>
          <p>想问什么都可以告诉我喔~我会用肉球努力帮你想办法(ฅ>ω<*ฅ)</p>
        </div>

        <!-- 聊天消息 -->
        <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.type]"
        >
          <div class="message-avatar">
            <div v-if="message.type === 'user'" class="avatar user-avatar">👤</div>
            <div v-else class="avatar ai-avatar">🐾</div>
          </div>
          <div class="message-content-wrapper">
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>

              <!-- 打字动画 -->
              <div
                  v-if="message.type === 'ai' && isLoading && index === messages.length - 1"
                  class="typing-indicator"
              >
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
              </div>
            </div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input-container">
        <div class="input-wrapper">
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              :maxrows="4"
              placeholder="主人想问什么呀？小猫咪等不及要回答啦~(ฅ^ω^ฅ)"
              :disabled="isLoading"
              @keydown.enter.exact.prevent="sendMessage"
              class="custom-input"
          ></el-input>

          <div class="input-actions">
            <div class="char-count">{{ inputMessage.length }}/1500</div>
            <el-button
                type="primary"
                @click="sendMessage"
                :disabled="isLoading || !inputMessage.trim()"
                class="send-button"
                :class="{ loading: isLoading }"
            >
              <span v-if="!isLoading">喵一下🐾</span>
              <div v-else class="loading-spinner"></div>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onBeforeUnmount } from 'vue'
import { streamDialogue } from '@/api/ai/streamChat'

const chatHistory = ref(null)
const inputMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const streamingText = ref('')
/** @type {import('vue').Ref<EventSource | null>} */
const eventSource = ref(null)

// 喵一下消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = {
    type: 'user',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString()
  }
  messages.value.push(userMessage)
  const messageToSend = inputMessage.value
  inputMessage.value = ''

  setTimeout(scrollToBottom, 0)

  try {
    isLoading.value = true
    streamingText.value = ''

    const aiMessage = {
      type: 'ai',
      content: '',
      time: new Date().toLocaleTimeString()
    }
    messages.value.push(aiMessage)

    const memoryId = Date.now() % 1000000

    streamDialogue(memoryId, messageToSend).then(es => {
      eventSource.value = es

      eventSource.value.onmessage = (event) => {
        const chunk = event.data
        streamingText.value += chunk
        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.type === 'ai') {
          lastMessage.content = streamingText.value
        }
        setTimeout(scrollToBottom, 0)
      }

      eventSource.value.onerror = (error) => {
        console.error('SSE连接出错:', error)
        if (eventSource.value) eventSource.value.close()
        isLoading.value = false
        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.type === 'ai') {
          lastMessage.content = streamingText.value || '呜喵～代码似乎被咬断了线，本喵下次会更乖的(っ>ω<)っ'
        }
        forceScrollToBottom()
        streamingText.value = ''
      }
    })
  } catch (error) {
    console.error('对话出错:', error)
    const errorMessage = {
      type: 'ai',
      content: '喵呜～抱歉呢，本喵不小心踩到Bug尾巴啦 QAQ',
      time: new Date().toLocaleTimeString()
    }
    messages.value.push(errorMessage)
    isLoading.value = false
    streamingText.value = ''
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatHistory.value) {
      chatHistory.value.scrollTop = chatHistory.value.scrollHeight
    }
  })
}

const forceScrollToBottom = () => {
  if (chatHistory.value) {
    chatHistory.value.scrollTop = chatHistory.value.scrollHeight
  }
}

onBeforeUnmount(() => {
  if (eventSource.value) {
    eventSource.value.close()
  }
})
</script>

<style scoped>
/* ========== 🌸 整体可爱猫娘风格 ========== */
.home-page {
  display: flex;
  flex-direction: column;
  height: 92vh;
  background: linear-gradient(135deg, #fff5f9 0%, #ffeaf3 100%);
  font-family: "Comic Neue", "Rounded Mplus 1c", "幼圆", cursive;
}

/* 聊天容器毛绒圆角 */
.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  max-width: 1000px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  border-radius: 24px;
}

/* 聊天历史区域 */
.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  border-radius: 20px;
  background-color: #fffafc;
  box-shadow: 0 4px 24px rgba(255, 182, 193, 0.25);
  margin-bottom: 20px;
  border: 2px solid rgba(255, 192, 203, 0.3);
  scroll-behavior: smooth;
  max-height: calc(100vh - 300px);
  backdrop-filter: blur(6px);
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #d63384;
  animation: fadeIn 0.8s ease;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: 12px;
}

.welcome-message h3 {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 0 0 4px rgba(255, 182, 193, 0.5);
}

.welcome-message p {
  font-size: 16px;
  color: #7a306c;
  max-width: 500px;
  margin: 0 auto;
  line-height: 1.6;
}

/* 消息气泡部分 */
.message {
  display: flex;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #ffb6c1, #ff69b4);
  color: white;
  border: 2px solid #ffa6c9;
}

.ai-avatar {
  background: linear-gradient(135deg, #b19cd9, #a281d0);
  color: white;
  border: 2px solid #c6a4eb;
}

/* 消息内容 */
.message-content-wrapper {
  max-width: 70%;
}

.message-content {
  padding: 12px 18px;
  border-radius: 18px;
  position: relative;
  transition: 0.3s ease;
}

.message.user .message-content {
  background: linear-gradient(135deg, #ff9ac1, #ff69b4);
  color: white;
  border-bottom-right-radius: 6px;
  box-shadow: 0 3px 10px rgba(255, 105, 180, 0.3);
}

.message.ai .message-content {
  background: #fff0f8;
  color: #5c405d;
  border-bottom-left-radius: 6px;
  border: 2px solid rgba(255, 182, 193, 0.3);
  box-shadow: 0 3px 10px rgba(255, 182, 193, 0.2);
}

.message-text {
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  color: #b48eb5;
  margin-top: 6px;
  padding: 0 4px;
}

/* 打字动画：小爪爪闪动 */
.typing-indicator {
  display: flex;
  align-items: center;
  margin-top: 8px;
}
.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #e596b2;
  margin: 0 3px;
  animation: typing 1.4s infinite ease-in-out;
}
.typing-dot:nth-child(1) { animation-delay: -0.32s; }
.typing-dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes typing {
  0%, 80%, 100% { opacity: 0.3; transform: translateY(0); }
  40% { opacity: 1; transform: translateY(-4px); }
}

/* 输入框部分 */
.chat-input-container {
  background: #fffafc;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(255, 182, 193, 0.3);
  border: 2px solid rgba(255, 182, 193, 0.3);
}

.custom-input :deep(.el-textarea__inner) {
  border-radius: 16px;
  border: 2px solid #ffd6e7;
  padding: 14px 16px;
  font-size: 16px;
  resize: none;
  transition: all 0.3s ease;
  background: #fff9fb;
  box-shadow: inset 0 2px 6px rgba(255, 192, 203, 0.2);
}
.custom-input :deep(.el-textarea__inner:focus) {
  border-color: #ff80ab;
  box-shadow: 0 0 10px rgba(255, 182, 193, 0.5);
}

/* 发送按钮 */
.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.char-count {
  font-size: 14px;
  color: #b48eb5;
}

.send-button {
  border-radius: 20px;
  padding: 10px 28px;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
  background: linear-gradient(135deg, #ffb6c1, #ff69b4);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.4);
}

.send-button:hover:not(.is-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(255, 105, 180, 0.5);
}

.send-button.loading {
  background-color: #ffd6e7;
  pointer-events: none;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
