<template>
  <div class="not-found-container">
    <el-row class="content" type="flex" justify="center" align="middle">
      <el-col :xs="24" :sm="12" :md="10" :lg="8">
        <div class="animation-box">
          <el-icon class="error-icon" :size="100">
            <svg viewBox="0 0 1024 1024">
              <path fill="currentColor" d="M512 64a448 448 0 1 1 0 896 448 448 0 0 1 0-896zm0 832a384 384 0 0 0 0-768 384 384 0 0 0 0 768zm48-176a48 48 0 1 1-96 0 48 48 0 0 1 96 0zm-48-464a32 32 0 0 1 32 32v256a32 32 0 0 1-64 0V288a32 32 0 0 1 32-32z"/>
            </svg>
          </el-icon>
          <h1 class="title">404</h1>
          <h2 class="subtitle">页面不存在</h2>
          <p class="description">您访问的页面可能已删除或暂时不可用</p>
          <el-button
              type="primary"
              size="large"
              class="action-btn"
              @click="goHome"
          >
            <el-icon class="mr-1">
              <svg viewBox="0 0 1024 1024">
                <path fill="currentColor" d="M946.5 505L534.6 93.4a31.93 31.93 0 0 0-45.2 0L77.5 505c-12 12-18.8 28.3-18.8 45.3 0 35.3 28.7 64 64 64h43.4V908c0 17.7 14.3 32 32 32H448V716h112v224h265.9c17.7 0 32-14.3 32-32V614.3h43.4c17 0 33.3-6.7 45.3-18.8 24.9-25 24.9-65.5-.1-90.5z"/>
              </svg>
            </el-icon>
            返回首页
          </el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.not-found-container {
  height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);

  .content {
    height: 100%;

    .animation-box {
      text-align: center;
      padding: 40px;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 16px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
      backdrop-filter: blur(10px);
      animation: fadeInUp 0.6s ease;
    }
  }

  .error-icon {
    color: #6c757d;
    margin-bottom: 24px;
    animation: float 3s ease-in-out infinite;
  }

  .title {
    font-size: 6rem;
    color: #495057;
    margin: 0.5em 0;
    line-height: 1;
  }

  .subtitle {
    color: #6c757d;
    margin-bottom: 1rem;
  }

  .description {
    color: #868e96;
    margin-bottom: 2rem;
  }

  .action-btn {
    padding: 12px 32px;
    font-size: 1.1rem;
    transition: transform 0.3s ease;

    &:hover {
      transform: translateY(-2px);
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 4rem !important;
  }

  .animation-box {
    padding: 20px !important;
  }
}
</style>