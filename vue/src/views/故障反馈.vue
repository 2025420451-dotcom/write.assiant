<template>
  <div class="feedback-container">
    <el-card class="feedback-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Warning /></el-icon>
          <span>故障反馈</span>
        </div>
      </template>

      <el-form
          ref="feedbackForm"
          :model="form"
          :rules="rules"
          label-width="120px"
          label-position="top"
      >
        <!-- 反馈类型 -->
        <el-form-item label="反馈类型" prop="type">
          <el-select
              v-model="form.type"
              placeholder="请选择反馈类型"
              style="width: 100%"
          >
            <el-option
                v-for="item in feedbackTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value"
            />
          </el-select>
        </el-form-item>

        <!-- 问题描述 -->
        <el-form-item label="问题描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="请详细描述您遇到的问题..."
              maxlength="500"
              show-word-limit
          />
        </el-form-item>

        <!-- 截图上传 -->
        <el-form-item label="相关截图">
          <el-upload
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="3"
              :file-list="fileList"
              :on-change="handleUpload"
              :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传3张图片，单张不超过2MB</div>
        </el-form-item>

        <!-- 联系方式 -->
        <el-form-item label="联系方式" prop="contact">
          <el-input
              v-model="form.contact"
              placeholder="请输入邮箱/手机号（选填）"
              clearable
          />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              @click="submitForm"
              :loading="submitLoading"
          >
            提交反馈
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 表单数据
const form = reactive({
  type: '',
  description: '',
  contact: ''
})

// 独立的上传文件列表
const fileList = ref([])

// 表单验证规则
const rules = reactive({
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入问题描述', trigger: 'blur' },
    { min: 20, message: '至少输入20个字符的描述', trigger: 'blur' }
  ],
  contact: [
    { pattern: /^1[3-9]\d{9}$|^\w+@[a-z0-9]+\.[a-z]{2,4}$/, message: '请输入有效的联系方式', trigger: 'blur' }
  ]
})

// 反馈类型选项
const feedbackTypes = ref([
  { value: 1, label: '功能问题' },
  { value: 2, label: '界面问题' },
  { value: 3, label: '性能问题' },
  { value: 4, label: '其他问题' }
])

// 上传处理
const handleUpload = (file) => {
  const isImage = file.raw.type.includes('image/')
  const isLt2M = file.raw.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  fileList.value.push(file)
  return true
}

// 删除文件处理
const handleRemove = (file) => {
  const index = fileList.value.indexOf(file)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

// 提交处理
const submitLoading = ref(false)
const feedbackForm = ref(null)

const submitForm = async () => {
  try {
    // 验证表单
    await feedbackForm.value.validate()

    submitLoading.value = true

    // 提交数据（不包含图片）
    await request.post('/api/feedback', form)

    ElMessage.success('反馈提交成功！')

    // 重置表单和上传列表
    feedbackForm.value.resetFields()
    fileList.value = []

  } catch (error) {
    if (error?.name !== 'ValidationError') {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.feedback-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.feedback-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
}

.header-icon {
  margin-right: 10px;
  color: #e6a23c;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
}
</style>