<template>
  <div class="update-container">
    <!-- 动态背景元素 -->
    <div class="decorative-gradient"></div>
    <div class="particle-layer"></div>

    <!-- 主内容卡片 -->
    <el-card class="glassy-card">
      <!-- 版本标识区 -->
      <div class="version-header">
        <el-icon class="version-badge"><Coin /></el-icon>
        <h1 class="version-display">
          <span class="version-text">当前版本</span>
          <span class="version-number">v{{ currentVersion }}</span>
        </h1>
      </div>

      <!-- 状态显示 -->
      <div class="status-indicator">
        <el-icon class="status-icon"><SuccessFilled /></el-icon>
        <span class="status-message">已是最新版本</span>
      </div>

      <!-- 功能亮点 -->
      <el-scrollbar class="feature-wall">
        <div v-for="(feature, index) in features" :key="index" class="feature-card">
          <el-icon class="feature-icon"><MagicStick /></el-icon>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.description }}</p>
        </div>
      </el-scrollbar>

      <!-- 操作按钮 -->
      <div class="action-area">
        <el-button
            type="primary"
            class="gradient-button"
            :icon="Refresh"
            @click="handleCheckUpdate"
        >
          立即验证
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, SuccessFilled, MagicStick, Refresh } from '@element-plus/icons-vue'

const currentVersion = ref('3.2.1')
const features = ref([
  {
    title: '视觉焕新',
    description: '采用全新材质设计语言，提升视觉层次感'
  },
  {
    title: '性能飞跃',
    description: '核心引擎优化，响应速度提升40%'
  },
  {
    title: '智能辅助',
    description: '新增AI写作建议功能'
  },
  {
    title: '多端同步',
    description: '支持跨设备实时数据同步'
  }
])

const handleCheckUpdate = () => {
  ElMessage.success('已验证，当前为最新版本！')
}
</script>

<style scoped>
.update-container {
  height: 100vh;
  overflow: hidden;
  position: relative;
  background: radial-gradient(circle at 10% 20%, #f0f6ff 0%, #e3eeff 100%);
}

.decorative-gradient {
  position: absolute;
  width: 80vw;
  height: 80vh;
  background: linear-gradient(150deg, #a0c4ff30 0%, #bdb2ff30 100%);
  border-radius: 30% 70% 70% 30%;
  filter: blur(80px);
  animation: gradientFlow 20s infinite linear;
}

.particle-layer {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image:
      radial-gradient(circle at 10% 20%, #ffffff30 2px, transparent 0),
      radial-gradient(circle at 90% 70%, #ffffff30 1px, transparent 0);
  background-size: 40px 40px;
}

.glassy-card {
  width: 90%;
  max-width: 800px;
  margin: 5vh auto;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);
}

.version-header {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
}

.version-badge {
  font-size: 80px;
  color: #3b82f6;
  margin-right: 20px;
  filter: drop-shadow(0 4px 6px rgba(59, 130, 246, 0.2));
}

.version-display {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.version-text {
  font-size: 24px;
  color: #64748b;
}

.version-number {
  font-size: 36px;
  color: #1e40af;
  font-weight: 700;
  letter-spacing: 2px;
}

.status-indicator {
  display: inline-flex;
  align-items: center;
  padding: 12px 24px;
  background: linear-gradient(45deg, #4ade80, #22c55e);
  border-radius: 50px;
  margin: 0 auto 30px;
  color: white;
  box-shadow: 0 4px 15px rgba(74, 222, 128, 0.2);
}

.status-icon {
  font-size: 24px;
  margin-right: 12px;
}

.status-message {
  font-size: 18px;
  font-weight: 500;
}

.feature-wall {
  height: 40vh;
  margin: 30px 0;
}

.feature-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 20px;
  margin: 15px;
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 32px;
  color: #3b82f6;
  margin-bottom: 12px;
}

.feature-title {
  color: #1e293b;
  margin: 8px 0;
}

.feature-desc {
  color: #64748b;
  font-size: 14px;
}

.action-area {
  display: grid;
  place-items: center;
  padding: 30px 0;
}

.gradient-button {
  padding: 18px 48px;
  font-size: 16px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(45deg, #3b82f6, #60a5fa);
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.gradient-button::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 200%;
  height: 100%;
  background: linear-gradient(
      90deg,
      rgba(255,255,255,0) 0%,
      rgba(255,255,255,0.3) 50%,
      rgba(255,255,255,0) 100%
  );
  animation: shine 3s infinite;
}

@keyframes gradientFlow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes shine {
  0% { left: -100%; }
  100% { left: 100%; }
}
</style>