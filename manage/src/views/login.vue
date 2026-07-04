<template>
  <div class="login-container">
    <div class="animated-background">
      <div class="particles">
        <div class="particle"></div>
        <div class="particle"></div>
        <div class="particle"></div>
        <!-- 更多particle... -->
      </div>
    </div>

    <!--登陆盒子-->
    <div class="login-box">
      <div class="logo">
        <span class="logo-text">Login</span>
      </div>

      <el-form
          :model="form"
          :rules="rules"
          ref="loginForm"
          class="login-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              size="large"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="handleLogin"
              size="large"
              class="login-btn"
          >
            立即登录
          </el-button>
        </el-form-item>

        <div class="social-login">
          <div>
            还没有账号？
            <a style="color:#274afa" href="/register">注册</a>
          </div>
          <div class="social-icons">
            <svg-icon icon-class="wechat" />
            <svg-icon icon-class="weibo" />
            <svg-icon icon-class="qq" />
          </div>
        </div>
      </el-form>
    </div>


  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js';
import router from "@/router/index.js";

// 表单响应式数据
const form = reactive({
  username: '',
  password: ''
})

// 表单引用（用于触发验证）
const loginForm = ref()

// 验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ]
})

// 登录处理函数
const handleLogin = () => {
  // 先进行表单验证
  loginForm.value.validate(valid => {
    if (valid) {
      // 发送登录请求
      request.post('/adminlogin', {
        username: form.username,
        password: form.password
      }).then(res => {
        if (res.code === '200') {
          ElMessage.success('登录成功')
          localStorage.setItem("code_information", JSON.stringify(res.data || {}))
          router.push('/home')
        } else {
          ElMessage.error(res.data.message || '登录失败，请检查用户名和密码')
        }
      }).catch(error => {
        // 处理网络错误
        ElMessage.error(error.message || '网络请求异常，请稍后重试')
        console.error('登录请求失败:', error)
      })
    } else {
      ElMessage.warning('请正确填写登录信息')
      return false
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.animated-background {
  position: absolute;
  width: 100%;
  height: 100%;
  background: #1a1a2e;
  z-index: 0;
}

.particles {
  position: relative;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.particle {
  position: absolute;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: float 3s infinite ease-in-out;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(0.9); }
}

/* 随机生成粒子 */
.particle:nth-child(1) { left: 15%; top: 20%; width: 8px; height: 8px; animation-delay: 0s; }
.particle:nth-child(2) { left: 70%; top: 40%; width: 12px; height: 12px; animation-delay: 0.5s; }
.particle:nth-child(3) { left: 45%; top: 60%; width: 6px; height: 6px; animation-delay: 1s; }



@keyframes gradient {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.login-box {
  position: relative;
  width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.logo {
  text-align: center;
  margin-bottom: 40px;
}

.logo-text {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.login-form {
  :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.8);
    border-radius: 8px;
    padding: 5px 15px;
  }

  :deep(.el-input__inner) {
    color: #333;
  }
}

.login-btn {
  width: 100%;
  background: linear-gradient(45deg, #409EFF, #64b5f6);
  border: none;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(64, 158, 255, 0.4);
  }
}

.social-login {
  margin-top: 30px;
  text-align: center;
  color: #999;

  span {
    display: block;
    margin-bottom: 15px;
  }

  .social-icons {
    display: flex;
    justify-content: center;
    gap: 20px;

    .svg-icon {
      width: 24px;
      height: 24px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.2);
      }
    }
  }
}
</style>