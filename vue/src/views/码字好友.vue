<template>
  <div class="chat-container">
    <el-container style="height: 100vh">
      <!-- 用户列表 -->
      <el-aside width="280px" class="user-aside">
        <el-card>
          <template #header>
            <div class="header-title">
              <el-icon><User /></el-icon>
              <span>在线用户 ({{ onlineUsers.length }})</span>
            </div>
          </template>

          <el-scrollbar>
            <div
                v-for="user in onlineUsers"
                :key="user"
                class="user-item"
                :class="{
                'active': selectedUser === user,
                'offline': !isOnline(user)
              }"
                @click="selectUser(user)"
            >
              <el-icon class="user-icon"><Avatar /></el-icon>
              <span class="username">{{ user }}</span>
              <el-badge
                  :value="unreadMessages[user]"
                  :hidden="!unreadMessages[user]"
                  class="badge"
              />
            </div>
            <div v-if="onlineUsers.length === 0" class="empty-tip">
              当前没有在线用户
            </div>
          </el-scrollbar>
        </el-card>
      </el-aside>

      <!-- 聊天区 -->
      <el-main>
        <el-card class="chat-card">
          <el-scrollbar ref="messageScroll" height="62vh">
            <div class="message-list">
              <div
                  v-for="(msg, index) in messages"
                  :key="index"
                  class="message"
                  :class="{ 'self': msg.from === currentUser }"
              >
                <el-avatar
                    v-if="msg.from !== currentUser"
                    :size="36"
                    src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"
                />
                <div class="message-content">
                  <div class="meta">
                    <span class="sender">{{ msg.from }}</span>
                    <span class="time">{{ formatTime(msg.timestamp) }}</span>
                  </div>
                  <div class="text">{{ msg.content }}</div>
                </div>
              </div>
            </div>
          </el-scrollbar>

          <div class="input-area">
            <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="3"
                placeholder="输入消息..."
                @keyup.enter.native="sendMessage"
                :disabled="!isConnected"
            />
            <el-button
                type="primary"
                class="send-btn"
                @click="sendMessage"
                :disabled="!isConnected || !selectedUser"
            >
              {{ isConnected ? '发送' : '连接中...' }}
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { User, Avatar, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 状态管理
const currentUser = ref('')
const onlineUsers = ref([])
const messages = ref([])
const selectedUser = ref(null)
const inputMessage = ref('')
const messageScroll = ref(null)
const isConnected = ref(false)
const unreadMessages = ref({})
let socket = null
let reconnectTimer = null

// WebSocket 连接
const initWebSocket = () => {
  // 从localStorage获取当前登录用户
  const userInfo = JSON.parse(localStorage.getItem('code_information'))
  if (!userInfo) {
    ElMessage.error('请先登录')
    return
  }
  currentUser.value = userInfo.username
  const wsUrl = `ws://localhost:9999/ws/chat?username=${encodeURIComponent(userInfo.username)}`

  socket = new WebSocket(wsUrl)

  socket.onopen = () => {
    isConnected.value = true
    ElMessage.success('聊天室连接成功')
    console.log('WebSocket 连接成功')
  }

  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      switch(data.type) {
        case 'userlist':
          onlineUsers.value = data.data.onlineUsers
          break
        case 'message':
          handleIncomingMessage(data)
          break
        case 'history':
          messages.value = data.data
          break
        case 'error':
          ElMessage.error(data.message || '发生错误')
          console.error('错误:', data.message)
          break
      }
    } catch (error) {
      console.error('消息处理错误:', error)
    }
  }

  socket.onerror = (error) => {
    console.error('连接错误:', error)
    isConnected.value = false
    ElMessage.error('聊天室连接错误')
  }

  socket.onclose = () => {
    isConnected.value = false
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }
    ElMessage.warning('聊天室连接断开，5秒后重连...')
    console.log('连接断开，5秒后重连...')
    reconnectTimer = setTimeout(initWebSocket, 5000)
  }
}

// 清理WebSocket连接
const cleanupWebSocket = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (socket) {
    socket.close()
    socket = null
  }
  isConnected.value = false
}

// 组件卸载前清理
onBeforeUnmount(() => {
  cleanupWebSocket()
})

// 处理接收消息
const handleIncomingMessage = (data) => {
  if (data.to === currentUser.value) {
    if (data.from !== selectedUser.value) {
      unreadMessages.value[data.from] = (unreadMessages.value[data.from] || 0) + 1
    }
    messages.value.push(data)
    scrollToBottom()
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || !selectedUser.value) return

  const message = {
    type: 'chat',
    messageType: 'private',
    to: selectedUser.value,
    content: inputMessage.value.trim()
  }

  socket.send(JSON.stringify(message))
  inputMessage.value = ''
}

// 选择用户
const selectUser = (user) => {
  selectedUser.value = user
  unreadMessages.value[user] = 0
  loadHistory(user)
}

// 加载历史记录
const loadHistory = () => {
  socket.send(JSON.stringify({
    type: 'history',
    target: selectedUser.value
  }))
}

// 时间格式化
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 自动滚动
const scrollToBottom = () => {
  nextTick(() => {
    if (messageScroll.value) {
      messageScroll.value.setScrollTop(messageScroll.value.wrap$.scrollHeight)
    }
  })
}

// 检查用户在线状态
const isOnline = (user) => onlineUsers.value.includes(user)

// 初始化
onMounted(() => {
  initWebSocket()
})
</script>

<style scoped>
.chat-container {
  height: 100vh;
  background: #f8f9fa;
}

.user-aside {
  background: white;
  border-right: 1px solid #e9ecef;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #495057;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.user-item:hover {
  background: #f1f3f5;
}

.user-item.active {
  background: #e7f5ff;
}

.user-item.offline {
  opacity: 0.6;
}

.username {
  margin-left: 12px;
  flex: 1;
}

.badge {
  margin-left: 8px;
}

.empty-tip {
  padding: 16px;
  color: #868e96;
  text-align: center;
}

.chat-card {
  height: calc(100vh - 40px);
  border: none;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.message-list {
  padding: 20px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.self {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 75%;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message.self .message-content {
  background: #409eff;
  color: white;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.sender {
  font-weight: 500;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #868e96;
}

.message.self .time {
  color: rgba(255,255,255,0.8);
}

.text {
  line-height: 1.6;
  word-break: break-word;
}

.input-area {
  padding: 16px;
  border-top: 1px solid #e9ecef;
}

.send-btn {
  width: 120px;
  margin-top: 12px;
}
</style>