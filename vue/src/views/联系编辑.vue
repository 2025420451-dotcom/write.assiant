<template>
  <div class="editor-container">
    <div class="header">
      <h1>编辑联系人</h1>
      <p class="subtitle">已合作的编辑团队</p>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="编辑类型">
          <el-select v-model="filterForm.type" placeholder="请选择类型" clearable>
            <el-option label="总编" value="chief" />
            <el-option label="责任编辑" value="editor" />
            <el-option label="签约编辑" value="contract" />
            <el-option label="特约编辑" value="special" />
          </el-select>
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-select v-model="filterForm.expertise" placeholder="请选择领域" clearable>
            <el-option label="文学" value="literature" />
            <el-option label="科幻" value="scifi" />
            <el-option label="悬疑" value="mystery" />
            <el-option label="历史" value="history" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="24" justify="center" v-loading="loading">
      <el-col
          v-for="(editor, index) in editors"
          :key="index"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
      >
        <el-card class="editor-card" shadow="hover">
          <!-- 头像 -->
          <div class="avatar-wrapper">
            <el-image
                :src="editor.avatar"
                class="avatar"
                :preview-src-list="[editor.avatar]"
            />
            <div class="status-badge" :class="editor.status">
              {{ editor.statusText }}
            </div>
          </div>

          <!-- 基本信息 -->
          <div class="editor-info">
            <h3>{{ editor.name }}</h3>
            <div class="tags">
              <el-tag
                  v-for="(tag, tagIndex) in editor.tags"
                  :key="tagIndex"
                  class="tag"
                  :type="tagTypes[tagIndex % 3]"
                  effect="plain"
                  round
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="expertise">
              <span class="label">擅长领域：</span>
              <span class="value">{{ editor.expertise.join('、') }}</span>
            </div>
          </div>

          <!-- 联系信息 -->
          <div class="contact-info">
            <div class="contact-item">
              <el-icon><Iphone /></el-icon>
              <span>{{ editor.phone }}</span>
            </div>
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>{{ editor.email }}</span>
            </div>
            <div class="contact-item">
              <el-icon><Timer /></el-icon>
              <span>响应时间：{{ editor.responseTime }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="card-actions">
            <el-button type="primary" @click="handleContact(editor)">
              <el-icon><ChatDotRound /></el-icon>
              联系
            </el-button>
            <el-button @click="handleViewDetails(editor)">
              <el-icon><InfoFilled /></el-icon>
              详情
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 联系对话框 -->
    <el-dialog
      v-model="contactDialogVisible"
      title="联系编辑"
      width="500px"
      destroy-on-close
    >
      <div class="contact-dialog-content">
        <div class="editor-profile">
          <el-image :src="currentEditor?.avatar" class="dialog-avatar" />
          <div class="profile-info">
            <h4>{{ currentEditor?.name }}</h4>
            <p>{{ currentEditor?.title }}</p>
          </div>
        </div>
        <el-form :model="contactForm" label-width="80px">
          <el-form-item label="主题">
            <el-input v-model="contactForm.subject" placeholder="请输入联系主题" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input
              v-model="contactForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入联系内容"
            />
          </el-form-item>
          <el-form-item label="附件">
            <el-upload
              action="/api/upload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
            >
              <el-button type="primary">上传文件</el-button>
              <template #tip>
                <div class="upload-tip">
                  支持jpg、png、pdf格式，单个文件不超过10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="contactDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitContact" :loading="submitting">
            发送
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="编辑详情"
      width="600px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ currentEditor?.name }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ currentEditor?.title }}</el-descriptions-item>
        <el-descriptions-item label="擅长领域">{{ currentEditor?.expertise.join('、') }}</el-descriptions-item>
        <el-descriptions-item label="响应时间">{{ currentEditor?.responseTime }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentEditor?.phone }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱">{{ currentEditor?.email }}</el-descriptions-item>
        <el-descriptions-item label="工作经历" :span="2">
          <div v-for="(exp, index) in currentEditor?.experience" :key="index" class="experience-item">
            <div class="exp-title">{{ exp.title }}</div>
            <div class="exp-company">{{ exp.company }}</div>
            <div class="exp-time">{{ exp.time }}</div>
            <div class="exp-desc">{{ exp.description }}</div>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="个人简介" :span="2">
          {{ currentEditor?.introduction }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { 
  ElRow, 
  ElCol, 
  ElCard, 
  ElImage, 
  ElTag, 
  ElIcon,
  ElMessage
} from 'element-plus'
import { 
  Iphone, 
  Message, 
  Timer,
  Search,
  Refresh,
  ChatDotRound,
  InfoFilled
} from '@element-plus/icons-vue'

const tagTypes = ['', 'success', 'info']

// 筛选表单
const filterForm = reactive({
  type: '',
  expertise: ''
})

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 对话框状态
const contactDialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const currentEditor = ref(null)

// 联系表单
const contactForm = reactive({
  subject: '',
  content: '',
  attachments: []
})

// 编辑列表数据
const editors = ref([
  {
    name: '陈文轩',
    title: '总编',
    avatar: 'https://picsum.photos/200/200?random=1',
    tags: ['总编', '文学编辑'],
    expertise: ['文学', '历史', '传记'],
    phone: '138-1234-5678',
    email: 'chenwx@publish.com',
    responseTime: '2小时内',
    status: 'online',
    statusText: '在线',
    experience: [
      {
        title: '总编辑',
        company: '文学出版社',
        time: '2020-至今',
        description: '负责出版社整体内容策划和编辑团队管理'
      },
      {
        title: '高级编辑',
        company: '文化传媒',
        time: '2015-2020',
        description: '负责文学类图书的策划和编辑工作'
      }
    ],
    introduction: '从事编辑工作15年，擅长文学类作品的策划和编辑，曾获得多个出版奖项。'
  },
  {
    name: '李晓萌',
    title: '责任编辑',
    avatar: 'https://picsum.photos/200/200?random=2',
    tags: ['责任编辑', '新人指导'],
    expertise: ['科幻', '奇幻', '悬疑'],
    phone: '139-8765-4321',
    email: 'lixm@book.com',
    responseTime: '4小时内',
    status: 'busy',
    statusText: '忙碌',
    experience: [
      {
        title: '责任编辑',
        company: '科幻出版社',
        time: '2018-至今',
        description: '负责科幻类图书的策划和编辑工作'
      }
    ],
    introduction: '专注于科幻和奇幻类作品的编辑工作，对新人作者有丰富的指导经验。'
  },
  {
    name: '王建国',
    title: '签约编辑',
    avatar: 'https://picsum.photos/200/200?random=3',
    tags: ['签约编辑', '十年经验'],
    expertise: ['历史', '军事', '传记'],
    phone: '136-5555-8888',
    email: 'wangjg@novel.org',
    responseTime: '6小时内',
    status: 'offline',
    statusText: '离线',
    experience: [
      {
        title: '签约编辑',
        company: '历史出版社',
        time: '2015-至今',
        description: '负责历史类图书的策划和编辑工作'
      }
    ],
    introduction: '专注于历史类作品的编辑工作，对历史题材有深入研究。'
  },
  {
    name: '张思雨',
    title: '特约编辑',
    avatar: 'https://picsum.photos/200/200?random=4',
    tags: ['特约编辑', '快速响应'],
    expertise: ['言情', '都市', '青春'],
    phone: '137-9999-0000',
    email: 'zhangsy@writer.net',
    responseTime: '1小时内',
    status: 'online',
    statusText: '在线',
    experience: [
      {
        title: '特约编辑',
        company: '青春文学出版社',
        time: '2019-至今',
        description: '负责青春文学类图书的策划和编辑工作'
      }
    ],
    introduction: '专注于青春文学类作品的编辑工作，对年轻读者群体有深入了解。'
  }
])

// 搜索
const handleSearch = () => {
  loading.value = true
  // TODO: 实现搜索逻辑
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 重置筛选
const resetFilter = () => {
  filterForm.type = ''
  filterForm.expertise = ''
  handleSearch()
}

// 联系编辑
const handleContact = (editor) => {
  currentEditor.value = editor
  contactDialogVisible.value = true
}

// 查看详情
const handleViewDetails = (editor) => {
  currentEditor.value = editor
  detailsDialogVisible.value = true
}

// 上传成功
const handleUploadSuccess = (response) => {
  contactForm.attachments.push(response.url)
  ElMessage.success('上传成功')
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 提交联系
const submitContact = () => {
  submitting.value = true
  // TODO: 实现提交逻辑
  setTimeout(() => {
    submitting.value = false
    contactDialogVisible.value = false
    ElMessage.success('发送成功')
  }, 1000)
}
</script>

<style scoped>
.editor-container {
  padding: 2rem;
  background: #f8f9fa;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #7f8c8d;
  font-size: 0.95rem;
}

.filter-card {
  margin-bottom: 2rem;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.editor-card {
  margin-bottom: 1.5rem;
  border-radius: 12px;
  transition: transform 0.3s ease;
  overflow: visible;
  padding-top: 60px;
  position: relative;
}

.editor-card:hover {
  transform: translateY(-5px);
}

.avatar-wrapper {
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  margin: 0;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  background: #fff;
}

.status-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
}

.status-badge.online {
  background-color: #67c23a;
}

.status-badge.busy {
  background-color: #e6a23c;
}

.status-badge.offline {
  background-color: #909399;
}

.editor-info {
  margin-top: 10px;
  text-align: center;
  margin-bottom: 1.5rem;
}

.editor-info h3 {
  font-size: 1.3rem;
  color: #34495e;
  margin-bottom: 0.8rem;
}

.tags {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-bottom: 1rem;
}

.tag {
  transition: all 0.2s ease;
}

.tag:hover {
  transform: scale(1.05);
}

.expertise {
  font-size: 0.9rem;
  color: #666;
  margin-top: 0.5rem;
}

.expertise .label {
  color: #909399;
}

.contact-info {
  padding: 0 1rem;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 1rem 0;
  padding: 12px;
  background: #f1f5f9;
  border-radius: 8px;
  transition: background 0.3s ease;
}

.contact-item:hover {
  background: #e9ecef;
}

.el-icon {
  color: #6c757d;
  font-size: 1.2rem;
}

.card-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 1rem;
  padding: 0 1rem 1rem;
}

.contact-dialog-content {
  padding: 20px;
}

.editor-profile {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.dialog-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.profile-info h4 {
  margin: 0;
  font-size: 1.2rem;
  color: #2c3e50;
}

.profile-info p {
  margin: 4px 0 0;
  color: #666;
  font-size: 0.9rem;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.experience-item {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #ebeef5;
}

.experience-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.exp-title {
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.exp-company {
  color: #409eff;
  margin-bottom: 4px;
}

.exp-time {
  color: #909399;
  font-size: 0.9rem;
  margin-bottom: 4px;
}

.exp-desc {
  color: #666;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .editor-container {
    padding: 1rem;
  }

  .avatar {
    width: 70px;
    height: 70px;
  }
}
</style>