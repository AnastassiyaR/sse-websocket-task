<template>
  <div class="chat-app">
    <header class="header">
      <h2>Live Chat</h2>
      <p>Real-time messaging using WebSocket</p>
    </header>

    <main class="chat-main">
      <div class="status" :class="{ online: connected }">
        {{ connected ? 'Connected' : 'Disconnected' }}
      </div>

      <section class="messages" ref="messagesEl">
        <div v-if="messages.length === 0" class="no-messages">
          <p>No messages yet</p>
          <p>Start the conversation!</p>
        </div>

        <article v-for="(message, i) in messages" :key="i"
                 class="message" :class="{ my: message.sender === username }">
          <header class="message-header">
            <strong>{{ message.sender }}</strong>
            <time class="time">{{ formatTime(message.timestamp) }}</time>
          </header>
          <p class="message-text">{{ message.content }}</p>
        </article>
      </section>

      <form v-if="connected" @submit.prevent="send" class="input-area">
        <input
            v-model="newMessage"
            placeholder="Type your message..."
            class="text-input"
        />
        <button type="submit" :disabled="!newMessage.trim()" class="send-btn">
          Send
        </button>
      </form>

      <form v-else @submit.prevent="connect" class="login-area">
        <input
            v-model="username"
            placeholder="Enter your name"
            class="name-input"
        />
        <button type="submit" :disabled="!username.trim()" class="join-btn">
          Join Chat
        </button>
      </form>

    </main>
  </div>
</template>

<script setup>
import { ref, nextTick, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const username = ref('')
const newMessage = ref('')
const messages = ref([])
const stompClient = ref(null)
const connected = ref(false)
const messagesEl = ref(null)

function formatTime(timestamp) {
  try {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    })
  } catch (e) {
    console.error('Failed to format time:', e)
    return ''
  }
}

function connect() {
  if (!username.value.trim()) return

  stompClient.value = new Client({
    webSocketFactory: () => new SockJS('/ws'),

    onConnect: () => {
      connected.value = true

      stompClient.value.subscribe('/topic/messages', (message) => {
        try {
          const data = JSON.parse(message.body)
          messages.value.push({
            sender: data.sender,
            content: data.content,
            timestamp: data.timestamp
          })
          scrollToBottom()
        } catch (e) {
          console.error('Error parsing message:', e)
        }
      })

      messages.value.push({
        sender: 'System',
        content: `${username.value} joined the chat`,
        timestamp: new Date().toISOString()
      })
      scrollToBottom()
    },

    onDisconnect: () => {
      connected.value = false
    },

    onStompError: (frame) => {
      console.error('STOMP error:', frame)
    }
  })

  stompClient.value.activate()
}

function send() {
  if (!newMessage.value.trim() || !stompClient.value?.connected) return

  const message = {
    sender: username.value,
    content: newMessage.value.trim()
  }

  stompClient.value.publish({
    destination: '/app/chat.send',
    body: JSON.stringify(message)
  })

  newMessage.value = ''
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesEl.value) {
      messagesEl.value.scrollTop = messagesEl.value.scrollHeight
    }
  })
}

onUnmounted(() => {
  if (stompClient.value) {
    stompClient.value.deactivate()
  }
})
</script>

<style scoped>
.header {
  text-align: center;
  margin-bottom: 40px;
  color: white;
}

.header h2 {
  font-size: 64px;
  margin-bottom: 10px;
}

.chat-main {
  background: white;
  border-radius: 12px;
  padding: 25px;
  display: flex;
  flex-direction: column;
  height: 800px;
}

.status {
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 10px;
  background: #ffebee;
  color: #b71c1c;
  font-weight: 500;
}

.status.online {
  background: #e8f5e8;
  color: #2e7d32;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.no-messages {
  text-align: center;
  color: #666;
}

.no-messages p {
  margin: 10px;
}

.message {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 8px;
  background: white;
}

.message.my {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.time {
  font-size: 15px;
  color: #666;
}

.message-text {
  color: #444;
}

.input-area, .login-area {
  padding-top: 10px;
  display: flex;
  gap: 10px;
}

.text-input, .name-input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 20px;
}

.text-input:focus, .name-input:focus {
  outline: none;
  border-color: #2196f3;
}

.send-btn, .join-btn {
  padding: 15px 20px;
  background: #1976d2;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
}

.join-btn {
  background: #4CAF50;
  padding: 0.75rem 2rem;
}

.send-btn:disabled, .join-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>