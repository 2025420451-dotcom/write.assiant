<template>
  <div class="personal-center">
    <!-- 动态粒子背景 -->
    <vue-particles
        class="particles"
        color="#7C3AED"
        :particleOpacity="0.7"
        :particlesNumber="80"
        shapeType="circle"
        :particleSize="4"
        linesColor="#6366F1"
        :linesWidth="1"
        :lineLinked="true"
        :lineOpacity="0.4"
        :linesDistance="150"
        :moveSpeed="3"
    />

    <!-- 主卡片容器 -->
    <el-card class="profile-card">
      <!-- 头部区域 -->
      <div class="header">
        <div class="avatar-wrapper">
          <el-avatar
              :size="110"
              src="/123.jpg"
              :src="avatarUrl"
              class="avatar"
              :class="{ 'avatar-edit-mode': isEditMode }"
          />
          <div class="decorative-wave"></div>
        </div>
        <h1 class="username">
          {{ userInfo.username }}
          <el-tooltip
              v-if="userInfo.emailVerified"
              content="已验证邮箱"
              placement="right"
          >
            <el-icon class="verified-badge"><CircleCheck /></el-icon>
          </el-tooltip>
        </h1>
      </div>

      <!-- 内容区域 -->
      <div class="content">
        <transition name="fade-transform" mode="out-in">
          <!-- 展示模式 -->
          <div v-if="!isEditMode" class="info-display">
            <div class="info-item">
              <el-icon class="info-icon"><User /></el-icon>
              <div class="info-content">
                <div class="info-label">用户名</div>
                <div class="info-value">{{ userInfo.username }}</div>
              </div>
            </div>

            <div class="info-item">
              <el-icon class="info-icon"><Message /></el-icon>
              <div class="info-content">
                <div class="info-label">电子邮箱</div>
                <div class="info-value">{{ userInfo.email }}</div>
              </div>
            </div>

            <div class="info-item">
              <el-icon class="info-icon"><Iphone /></el-icon>
              <div class="info-content">
                <div class="info-label">联系电话</div>
                <div class="info-value">{{ userInfo.phone || '未绑定' }}</div>
              </div>
            </div>
          </div>

          <!-- 编辑模式 -->
          <el-form
              v-else
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="edit-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                  v-model="form.username"
                  placeholder="请输入新用户名"
                  clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="电子邮箱" prop="email">
              <el-input
                  v-model="form.email"
                  type="email"
                  placeholder="请输入电子邮箱"
                  clearable
              >
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="联系电话" prop="phone">
              <el-input
                  v-model="form.phone"
                  placeholder="请输入联系电话"
                  clearable
              >
                <template #prefix>
                  <el-icon><Iphone /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="password">
              <el-input
                  v-model="form.password"
                  type="password"
                  show-password
                  placeholder="请输入新密码"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入密码"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </transition>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <transition name="fade" mode="out-in">
          <!-- 用 div 包裹需要切换的内容 -->
          <div v-if="!isEditMode" key="view-mode">
            <el-button
                type="primary"
                class="action-btn"
                @click="isEditMode = true"
            >
              <el-icon><Edit /></el-icon>
              <span>编辑资料</span>
            </el-button>
          </div>

          <div v-else key="edit-mode">
            <el-button
                type="success"
                class="action-btn"
                :loading="submitting"
                @click="submitForm"
            >
              <el-icon><Check /></el-icon>
              <span>保存修改</span>
            </el-button>
            <el-button
                class="action-btn"
                @click="cancelEdit"
            >
              <el-icon><Close /></el-icon>
              <span>取消编辑</span>
            </el-button>
          </div>
        </transition>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  User, Message, Iphone, Lock,
  Edit, Check, Close, CircleCheck
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 用户信息
const userInfo = reactive({
  id: null,
  username: '加载中...',
  email: '加载中...',
  phone: null,
  emailVerified: false
})

// 表单相关
const isEditMode = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

// 验证规则
const rules = {/* 保持原有规则不变 */}

const loadUserInfo = async () => {
  try {
    const codeInfo = JSON.parse(localStorage.getItem('code_information'))
    if (!codeInfo?.id) {
      ElMessage.error('请重新登录')
      return router.push('/login')
    }
    // 添加时间戳防止缓存
    const  data  = await request.get('/user/getperson', {
      params: {
        id: codeInfo.id,
        _t: Date.now()
      }
    })
    // 统一使用 status 字段校验
    if (data.code === '200') {
      // 深度合并对象保证响应式

      Object.keys(data.data).forEach(key => {
        userInfo[key] = data.data[key]
      })

      // 安全重置表单（保留密码字段）
      form.username = data.data.username
      form.email = data.data.email
      form.phone = data.data.phone || ''
    }
  } catch (error) {
    handleError('数据加载失败', error)
  }
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const  data  = await request.post('/user/updataperson', {
      ...form,
      id: JSON.parse(localStorage.code_information).id
    })

    if (data.code === '200') {
      // 强制刷新数据
      await loadUserInfo()
      ElMessage.success('资料更新成功')
      isEditMode.value = false
    }
  } catch (error) {
    handleError('更新失败', error)
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditMode.value = false
  Object.assign(form, {
    username: userInfo.username,
    email: userInfo.email,
    phone: userInfo.phone,
    password: '',
    confirmPassword: ''
  })
}

// 错误处理
const handleError = (defaultMsg, error) => {
  const message = error.response?.data?.message || error.message || defaultMsg
  ElMessage.error(message)
}

// 初始化
onMounted(() => {
  loadUserInfo()
})

// 示例头像地址
const avatarUrl = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')
</script>

<style lang="scss" scoped>
.personal-center {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f3e8ff 0%, #e0f0ff 100%);
  position: relative;
  overflow: hidden;

  .particles {
    position: absolute;
    width: 100%;
    height: 100%;
    z-index: 0;
    opacity: 0.6;
  }

  .profile-card {
    width: 720px;
    border: none;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(12px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      transform: translateY(-8px) scale(1.02);
    }

    .header {
      text-align: center;
      padding: 2rem 0 1rem;

      .avatar-wrapper {
        position: relative;
        display: inline-block;
        margin-bottom: 1.5rem;

        .avatar {
          border: 3px solid #fff;
          box-shadow: 0 8px 24px rgba(124, 58, 237, 0.2);
          transition: all 0.3s ease;

          &-edit-mode {
            transform: scale(1.05);
            box-shadow: 0 12px 32px rgba(124, 58, 237, 0.3);
          }
        }

        .decorative-wave {
          position: absolute;
          bottom: -16px;
          left: 50%;
          transform: translateX(-50%);
          width: 80%;
          height: 4px;
          background: linear-gradient(90deg,
              rgba(124, 58, 237, 0) 0%,
              rgba(124, 58, 237, 0.6) 50%,
              rgba(124, 58, 237, 0) 100%
          );
          border-radius: 2px;
        }
      }

      .username {
        font-size: 2rem;
        color: #2d3748;
        margin: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;

        .verified-badge {
          color: #10b981;
          font-size: 1.4rem;
          filter: drop-shadow(0 2px 4px rgba(16, 185, 129, 0.2));
        }
      }
    }

    .content {
      padding: 0 2rem;

      .info-display {
        .info-item {
          display: flex;
          align-items: center;
          padding: 1.5rem;
          margin: 1rem 0;
          background: rgba(249, 250, 251, 0.8);
          border-radius: 16px;
          transition: all 0.3s ease;

          &:hover {
            transform: translateX(10px);
            box-shadow: 4px 4px 15px rgba(0, 0, 0, 0.05);
          }

          .info-icon {
            font-size: 1.8rem;
            color: #7c3aed;
            margin-right: 1.5rem;
          }

          .info-content {
            .info-label {
              font-size: 0.9rem;
              color: #718096;
              margin-bottom: 0.3rem;
            }

            .info-value {
              font-size: 1.1rem;
              color: #2d3748;
              font-weight: 500;
            }
          }
        }
      }

      .edit-form {
        :deep(.el-form-item) {
          margin-bottom: 1.8rem;
        }

        :deep(.el-form-item__label) {
          font-weight: 600;
          color: #4a5568;
          padding-bottom: 0.5rem;
        }

        :deep(.el-input__inner) {
          height: 48px;
          border-radius: 12px;
          padding: 0 16px;
          font-size: 1rem;
          transition: all 0.3s ease;

          &:focus {
            border-color: #7c3aed;
            box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.1);
          }
        }
      }
    }

    .actions {
      padding: 2rem 0;
      text-align: center;

      .action-btn {
        padding: 14px 32px;
        border-radius: 14px;
        font-size: 1rem;
        font-weight: 500;
        letter-spacing: 0.5px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        &.el-button--success {
          background: linear-gradient(135deg, #7c3aed, #8b5cf6);
          border-color: transparent;
        }

        .el-icon {
          margin-right: 8px;
          font-size: 1.2em;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .profile-card {
    width: 90% !important;
    margin: 20px;
    border-radius: 16px;

    .header {
      .avatar {
        width: 80px !important;
        height: 80px !important;
      }

      .username {
        font-size: 1.6rem !important;
      }
    }

    .content {
      padding: 0 1rem !important;

      .info-item {
        padding: 1rem !important;
      }
    }

    .actions {
      padding: 1.5rem 0 !important;
    }
  }
}

/* 过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from,
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>