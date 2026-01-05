<template>
  <div>
    <header class="app-header">
      <nav class="nav-container">
        <h1>Real-Time Demo</h1>
        <div class="nav-links">
          <router-link to="/websocket" class="nav-link">WebSocket Chat</router-link>
          <router-link to="/sse" class="nav-link">SSE News Feed</router-link>
        </div>
      </nav>
    </header>
    <main class="main-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
</script>

<style>

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #667eea;
}

.app-header {
  background: rgba(215, 213, 213, 0.95);
}

.nav-container {
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h1 {
  color: #5f4949;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-link {
  text-decoration: none;
  color: #6e1717;
  padding: 20px 20px;
  border-radius: 8px;
}

.nav-link:hover {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 30px;
}
</style>