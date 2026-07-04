<template>
  <div class="work-settings-container">
    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 基本信息设置 -->
      <el-tab-pane label="基本信息" name="basic">
        <el-form :model="basicForm" label-width="120px" class="settings-form">
          <el-form-item label="作品名称">
            <el-input v-model="basicForm.title" placeholder="请输入作品名称" />
          </el-form-item>
          
          <el-form-item label="作品封面">
            <el-upload
              class="cover-uploader"
              action="/api/upload"
              :show-file-list="false"
              :on-success="handleCoverSuccess"
            >
              <img v-if="basicForm.cover" :src="basicForm.cover" class="cover-image" />
              <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">建议尺寸：800x1200px，支持jpg、png格式</div>
          </el-form-item>

          <el-form-item label="作品简介">
            <el-input
              v-model="basicForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入作品简介"
            />
          </el-form-item>

          <el-form-item label="作品分类">
            <el-cascader
              v-model="basicForm.categories"
              :options="categoryOptions"
              :props="{ multiple: true }"
              placeholder="请选择作品分类"
            />
          </el-form-item>

          <el-form-item label="作品标签">
            <el-select
              v-model="basicForm.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入标签"
            >
              <el-option
                v-for="tag in tagOptions"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 发布设置 -->
      <el-tab-pane label="发布设置" name="publish">
        <el-form :model="publishForm" label-width="120px" class="settings-form">
          <el-form-item label="发布状态">
            <el-radio-group v-model="publishForm.status">
              <el-radio label="draft">草稿</el-radio>
              <el-radio label="published">已发布</el-radio>
              <el-radio label="scheduled">定时发布</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="定时发布时间" v-if="publishForm.status === 'scheduled'">
            <el-date-picker
              v-model="publishForm.scheduleTime"
              type="datetime"
              placeholder="选择发布时间"
            />
          </el-form-item>

          <el-form-item label="更新频率">
            <el-select v-model="publishForm.updateFrequency">
              <el-option label="每日更新" value="daily" />
              <el-option label="每周更新" value="weekly" />
              <el-option label="每月更新" value="monthly" />
              <el-option label="不定期更新" value="irregular" />
            </el-select>
          </el-form-item>

          <el-form-item label="是否收费">
            <el-switch v-model="publishForm.isPaid" />
          </el-form-item>

          <el-form-item label="章节价格" v-if="publishForm.isPaid">
            <el-input-number 
              v-model="publishForm.chapterPrice" 
              :min="0" 
              :max="100"
              :precision="2"
            />
            <span class="unit">元/章</span>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 权限设置 -->
      <el-tab-pane label="权限设置" name="permission">
        <el-form :model="permissionForm" label-width="120px" class="settings-form">
          <el-form-item label="阅读权限">
            <el-radio-group v-model="permissionForm.readPermission">
              <el-radio label="public">公开</el-radio>
              <el-radio label="private">私密</el-radio>
              <el-radio label="vip">VIP专享</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="评论权限">
            <el-radio-group v-model="permissionForm.commentPermission">
              <el-radio label="all">所有人</el-radio>
              <el-radio label="registered">注册用户</el-radio>
              <el-radio label="vip">VIP用户</el-radio>
              <el-radio label="none">禁止评论</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="打赏权限">
            <el-switch v-model="permissionForm.allowReward" />
          </el-form-item>

          <el-form-item label="版权声明">
            <el-input
              v-model="permissionForm.copyright"
              type="textarea"
              :rows="3"
              placeholder="请输入版权声明"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <!-- 底部操作按钮 -->
    <div class="settings-footer">
      <el-button @click="resetForm">重置</el-button>
      <el-button type="primary" @click="saveSettings">保存设置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

// 基本信息表单
const basicForm = ref({
  title: '',
  cover: '',
  description: '',
  categories: [],
  tags: []
})

// 发布设置表单
const publishForm = ref({
  status: 'draft',
  scheduleTime: '',
  updateFrequency: 'irregular',
  isPaid: false,
  chapterPrice: 0
})

// 权限设置表单
const permissionForm = ref({
  readPermission: 'public',
  commentPermission: 'all',
  allowReward: true,
  copyright: ''
})

// 分类选项
const categoryOptions = [
  {
    value: 'novel',
    label: '小说',
    children: [
      { value: 'fantasy', label: '奇幻' },
      { value: 'scifi', label: '科幻' },
      { value: 'romance', label: '言情' }
    ]
  },
  {
    value: 'poetry',
    label: '诗歌',
    children: [
      { value: 'modern', label: '现代诗' },
      { value: 'classical', label: '古典诗' }
    ]
  }
]

// 标签选项
const tagOptions = [
  '青春', '校园', '都市', '职场', '悬疑', '推理', '历史', '军事'
]

// 处理封面上传
const handleCoverSuccess = (response) => {
  basicForm.value.cover = response.url
}

// 重置表单
const resetForm = () => {
  basicForm.value = {
    title: '',
    cover: '',
    description: '',
    categories: [],
    tags: []
  }
  publishForm.value = {
    status: 'draft',
    scheduleTime: '',
    updateFrequency: 'irregular',
    isPaid: false,
    chapterPrice: 0
  }
  permissionForm.value = {
    readPermission: 'public',
    commentPermission: 'all',
    allowReward: true,
    copyright: ''
  }
}

// 保存设置
const saveSettings = () => {
  // TODO: 实现保存逻辑
  ElMessage.success('设置保存成功')
}
</script>

<style scoped>
.work-settings-container {
  padding: 20px;
}

.settings-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.settings-form {
  max-width: 800px;
  margin: 20px 0;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 300px;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 300px;
  line-height: 300px;
  text-align: center;
}

.cover-image {
  width: 200px;
  height: 300px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.unit {
  margin-left: 8px;
  color: #909399;
}

.settings-footer {
  margin-top: 20px;
  text-align: center;
}
</style> 