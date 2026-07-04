<template>
  <div class="not-found-container">
    <div class="content">
      <h1 class="status-code">404</h1>
      <h2 class="status-text">页面未找到</h2>
      <p class="description">您访问的页面可能已丢失或被移至其他位置</p>
      <button @click="goHome" class="home-button">返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/login')
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f8f9fa;
  padding: 20px;
  text-align: center;
}

.content {
  max-width: 600px;
  padding: 40px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.status-code {
  font-size: 8rem;
  font-weight: 700;
  color: #2d3436;
  margin: 0;
  line-height: 1;
}

.status-text {
  font-size: 2rem;
  color: #636e72;
  margin: 20px 0;
}

.description {
  color: #7f8c8d;
  margin-bottom: 30px;
}

.home-button {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 12px 30px;
  font-size: 1.1rem;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.home-button:hover {
  background-color: #2980b9;
  transform: translateY(-2px);
}

@media (max-width: 480px) {
  .status-code {
    font-size: 6rem;
  }

  .status-text {
    font-size: 1.5rem;
  }
}
/* 添加进入动画 */
.not-found-container {
  animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>