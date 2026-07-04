<template>
  <div class="register-container">
    <div class="animated-background"></div>

    <div class="register-box">
      <h2 class="register-title">用户注册</h2>

      <el-form
          :model="form"
          :rules="rules"
          ref="registerForm"
          label-position="top"
          class="register-form"
      >
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入4-16位用户名"
              prefix-icon="User"
              clearable
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="至少包含字母、数字和特殊字符"
              show-password
              prefix-icon="Lock"
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="form.email"
              placeholder="请输入有效邮箱"
              prefix-icon="Message"
          />
        </el-form-item>

        <!-- 手机号 -->
        <el-form-item label="手机号" prop="phone">
          <el-input
              v-model="form.phone"
              placeholder="11位中国大陆手机号"
              prefix-icon="Iphone"
          />
        </el-form-item>

        <el-button
            type="primary"
            class="register-btn"
            @click="handleRegister"
            :loading="loading"
        >
          立即注册
        </el-button>
      </el-form>

      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import router from "@/router/index.js";

// 表单数据
const form = reactive({
  username: '',
  password: '',
  email: '',
  phone: ''
})

// 加载状态
const loading = ref(false)

// 验证规则
const rules = reactive({
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 4, max: 16, message: '长度在4到16个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/,
      message: '至少8位，包含字母、数字和特殊字符' }
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码' }
  ]
})
let registerForm = ref()
// 注册处理
const handleRegister = () => {
  registerForm.value.validate(valid => {
    if (!valid) return

    loading.value = true
    request.post('/adminregister', form)
        .then(res => {
          ElMessage.success('注册成功')
          router.push('/login')
        })
        .catch(err => {
          ElMessage.error(err.message || '注册失败')
        })
        .finally(() => {
          loading.value = false
        })
  })
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);

  .animated-background {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
    background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
    background-size: 400% 400%;
    animation: gradientBG 15s ease infinite;
  }

  .register-box {
    position: relative;
    width: 480px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    z-index: 1;

    .register-title {
      text-align: center;
      margin-bottom: 30px;
      color: #2c3e50;
    }

    .register-form {
      .el-form-item__label {
        font-weight: 500;
        color: #666;
      }

      .register-btn {
        width: 100%;
        margin-top: 20px;
        height: 44px;
        font-size: 16px;
      }
    }

    .login-link {
      text-align: center;
      margin-top: 20px;
      color: #666;

      a {
        color: #409eff;
        text-decoration: none;
        &:hover {
          color: #66b1ff;
        }
      }
    }
  }
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
</style>