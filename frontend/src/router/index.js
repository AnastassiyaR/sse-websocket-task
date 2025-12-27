import { createRouter, createWebHistory } from 'vue-router'
import ChatApp from '../views/ChatApp.vue'
import SsePage from '../views/SsePage.vue'

const routes = [
    {
        path: '/',
        redirect: '/websocket'
    },
    {
        path: '/websocket',
        name: 'WebSocket',
        component: ChatApp
    },
    {
        path: '/sse',
        name: 'SSE',
        component: SsePage
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router