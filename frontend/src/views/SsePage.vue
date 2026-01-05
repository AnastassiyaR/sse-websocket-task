<template>
  <div class="sse-page">
    <header class="page-header">
      <h2>Live News Feed</h2>
      <p>Real-time updates using Server-Sent Events (SSE)</p>
    </header>

    <div class="news-container">
      <aside class="stats-card">
        <h3>Live Stats</h3>
        <dl>
          <div class="stat-item">
            <dt class="stat-label">Events Received</dt>
            <dd class="stat-value">{{ events.length }}</dd>
          </div>
          <div class="stat-item">
            <dt class="stat-label">Connection Status</dt>
            <dd class="stat-value" :class="{ connected: started }">
              {{ started ? 'Live' : 'Offline' }}
            </dd>
          </div>
          <div class="stat-item">
            <dt class="stat-label">Update Interval</dt>
            <dd class="stat-value">10s</dd>
          </div>
        </dl>
      </aside>

      <main class="news-main">
        <nav class="controls-section">
          <button @click="start" :disabled="started" class="control-button primary">
            Start Live Feed
          </button>
          <button @click="stop" :disabled="!started" class="control-button secondary">
            Stop Feed
          </button>
          <button @click="clearEvents" class="control-button outline">
            Clear
          </button>
        </nav>

        <section class="news-feed" ref="newsFeed">
          <div v-if="events.length === 0" class="empty-state">
            <h3>No news updates yet</h3>
            <p>Click "Start Live Feed" to begin receiving real-time updates</p>
          </div>

          <article v-for="(event, index) in events" :key="index" class="news-item">
            <time class="news-time">{{ event.time }}</time>
            <div class="news-content">
              <span class="news-badge" :class="getEventType()">
                {{ getEventType().toUpperCase() }}
              </span>
              <p class="news-text">{{ event.data }}</p>
            </div>
          </article>
        </section>

        <footer v-if="started" class="update-indicator">
          Receiving live updates...
        </footer>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onUnmounted } from 'vue'

const events = ref([])
const started = ref(false)
const newsFeed = ref(null)
let eventSource = null

function getEventType() {
  return 'info'
}

function start() {
  if (started.value || eventSource) return

  eventSource = new EventSource('/api/sse')

  eventSource.onopen = () => {
    started.value = true
    addEvent('✅ Connected to live news feed')
  }

  eventSource.addEventListener('time-update', (event) => {
    addEvent(event.data)
  })

  eventSource.onerror = (err) => {
    console.error('SSE error:', err)
    addEvent('⚠️ Connection error - browser will auto-reconnect...')
    if (eventSource.readyState === EventSource.CLOSED) {
      eventSource = null
      started.value = false
    }
  }
}

function stop() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  started.value = false
  addEvent('🛑 Live feed stopped')
}

function clearEvents() {
  events.value = []
}

function addEvent(data) {
  events.value.push({
    time: new Date().toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }),
    data
  })
  scrollToBottom()
}

function scrollToBottom() {
  nextTick(() => {
    if (newsFeed.value) {
      newsFeed.value.scrollTop = newsFeed.value.scrollHeight
    }
  })
}

onUnmounted(() => {
  stop()
})
</script>

<style scoped>
.sse-page { max-width: 1000px; margin: 0 auto; }
.page-header { text-align: center; margin-bottom: 40px; color: white; }
.page-header h2 { font-size: 64px; margin-bottom: 10px; }
.page-header p { font-size: 1.1rem; opacity: 0.9; }

.news-container { display: grid; gap: 50px; }
.stats-card { background: white; border-radius: 12px; padding: 40px; }
.stats-card h3 { margin-bottom: 10px; color: #333; }

.stat-item { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 2px solid #7d7676; }
.stat-item:last-child { border-bottom: none; }

.stat-label { color: #000; font-size: 0.9rem; }
.stat-value { font-weight: 600; color: #333; }
.stat-value.connected { color: #4CAF50; }

.news-main { background: #e8e8e8; border-radius: 12px; padding: 20px; }
.controls-section { display: flex; justify-content: left; margin-bottom: 20px; gap: 30px; padding-bottom: 20px; border-bottom: 1px solid #666; }
.control-button { display: flex; align-items: center; padding: 15px 20px; border: none; border-radius: 8px; cursor: pointer; font-weight: 600; transition: all 0.3s ease; }
.control-button.primary { background: #2196f3; color: white; }
.control-button.primary:hover:not(:disabled) { background: #1976d2; }
.control-button.secondary { background: #ff5722; color: white; }
.control-button.secondary:hover:not(:disabled) { background: #e64a19; }
.control-button.outline { background-color: white; color: #616161; border: 2px solid #d4d4d4; }
.control-button.outline:hover { border-color: #999; color: #333; }
.control-button:disabled { opacity: 0.6; cursor: not-allowed; }

.news-feed { flex: 1; overflow-y: auto; max-height: 500px; }
.empty-state { text-align: center; padding: 70px; color: #666; }
.news-item { display: flex; gap: 20px; margin-bottom: 10px; padding: 15px; border-radius: 8px; border-left: 4px solid #2196f3; background-color: white; animation: slideIn 0.3s ease; }
@keyframes slideIn { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.news-time { font-size: 13px; color: #666; font-weight: 400; min-width: 80px; }
.news-badge { display: inline-block; padding: 0.25rem 0.75rem; border-radius: 20px; font-size: 0.7rem; font-weight: 600; margin-bottom: 0.5rem; background: #e3f2fd; color: #1976d2; }
.news-text { color: #333; line-height: 1.4; margin: 0; }
.update-indicator { margin-top: 20px; padding: 15px 20px; background: #e8f5e8; border-radius: 8px; color: #2e7d32; font-size: 15px; text-align: center; animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.7; } }
</style>
