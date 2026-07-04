<template>
  <div class="chat-container">
    <!-- 左侧好友列表 -->
    <div class="friend-list">
      <div class="search-box">
        <el-input v-model="searchText" placeholder="搜索好友" :prefix-icon="Search" clearable></el-input>
      </div>
      <div class="friend-items">
        <div v-for="friend in filteredFriends"
             :key="friend.id"
             class="friend-item"
             :class="{ active: currentFriend?.id === friend.id }"
             @click="selectFriend(friend)">
          <el-avatar :size="40" :src="friend.avatar || defaultAvatar"></el-avatar>
          <div class="friend-info">
            <div class="friend-name">{{ friend.username }}</div>
            <div class="last-message">{{ friend.lastMessage || '暂无消息' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口 -->
    <div class="chat-window" v-if="currentFriend">
      <div class="chat-header">
        <span>{{ currentFriend.username }}</span>
      </div>

      <div class="message-list" ref="messageList">
        <div v-for="msg in messages"
             :key="msg.id"
             class="message-item"
             :class="{ 'message-mine': msg.senderId === currentUser.id }">
          <el-avatar :size="30" :src="msg.senderId === currentUser.id ? currentUser.avatar : currentFriend.avatar"></el-avatar>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
      </div>

      <div class="message-input">
        <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keyup.enter.ctrl="sendMessage"
        ></el-input>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>

    <div class="no-chat" v-else>
      <el-empty description="选择一个好友开始聊天"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 数据定义
const searchText = ref('')
const currentFriend = ref(null)
const messages = ref([])
const newMessage = ref('')
const messageList = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 模拟当前用户数据
const currentUser = ref({
  id: 1,
  username: '我',
  avatar: defaultAvatar
})

// 模拟好友列表数据
const friends = ref([
  { id: 2, username: '张三', avatar: defaultAvatar, lastMessage: '你好啊' },
  { id: 3, username: '李四', avatar: defaultAvatar, lastMessage: '在吗？' },
  { id: 4, username: '王五', avatar: defaultAvatar, lastMessage: '明天见' }
])

// 计算属性：过滤好友列表
const filteredFriends = computed(() => {
  if (!searchText.value) return friends.value
  return friends.value.filter(friend =>
      friend.username.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

// 选择好友
const selectFriend = async (friend) => {
  currentFriend.value = friend
  await loadMessages(friend.id)
  scrollToBottom()
}

// 加载消息历史
const loadMessages = async (friendId) => {
  try {
    const res = await request.get(`/chat/messages/${friendId}`)
    if (res.code === '200') {
      messages.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim()) return

  const message = {
    content: newMessage.value,
    senderId: currentUser.value.id,
    receiverId: currentFriend.value.id,
    createTime: new Date()
  }

  try {
    const res = await request.post('/chat/send', message)
    if (res.code === '200') {
      messages.value.push(res.data)
      newMessage.value = ''
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('发送消息失败')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 组件挂载时初始化
onMounted(() => {
  // 这里可以添加WebSocket连接等初始化操作
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 100px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.friend-list {
  width: 300px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.search-box {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.friend-items {
  flex: 1;
  overflow-y: auto;
}

.friend-item {
  display: flex;
  padding: 15px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.friend-item:hover {
  background-color: #f5f7fa;
}

.friend-item.active {
  background-color: #ecf5ff;
}

.friend-info {
  margin-left: 10px;
  flex: 1;
}

.friend-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.last-message {
  font-size: 12px;
  color: #999;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 15px;
  border-bottom: 1px solid #eee;
  font-weight: bold;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f7fa;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
}

.message-mine {
  flex-direction: row-reverse;
}

.message-content {
  margin: 0 10px;
  max-width: 60%;
}

.message-text {
  padding: 10px;
  border-radius: 4px;
  background: #fff;
  word-break: break-all;
}

.message-mine .message-text {
  background: #95ec69;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.message-input {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
}

.message-input .el-button {
  align-self: flex-end;
}

.no-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
</style> 