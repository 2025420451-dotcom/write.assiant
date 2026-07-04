<template>
  <el-container class="layout-container">
    <!-- 左侧导航栏 -->
    <el-aside width="200px">
      <!-- 头像区域 -->
      <el-dropdown trigger="hover" placement="right-start">
        <div class="avatar-wrapper">
          <el-avatar
              class="el-avatar--icon"
              src="/123.jpg"
          />
          <span class="username">{{ username }}</span>
        </div>
        <!-- 下拉菜单 -->
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('../home/person')">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item @click="logout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-menu
          router
          :default-active="$route.path"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
      >
        <el-menu-item index="/home/item-1">
          <el-icon><Notebook /></el-icon>
          <span>全部书籍</span>
        </el-menu-item>

        <el-menu-item index="/home/item-2">
          <el-icon><Edit /></el-icon>
          <span>码字统计</span>
        </el-menu-item>

        <el-menu-item index="../home/item-3">
          <el-icon><List /></el-icon>
          <span>诗词素材</span>
        </el-menu-item>

        <el-menu-item index="../home/item-4">
          <el-icon><User /></el-icon>
          <span>码字好友</span>
        </el-menu-item>

        <el-divider />

        <el-menu-item index="../home/item-5">
          <el-icon><PieChart /></el-icon>
          <span>作品数据</span>
        </el-menu-item>
<!--
        <el-menu-item index="../home/item-6">
          <el-icon><Histogram /></el-icon>
          <span>订阅统计</span>
        </el-menu-item>
-->
        <el-menu-item index="../home/item-7">
          <el-icon><ChatLineRound /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
<!--
        <el-menu-item index="../home/item-8">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
        </el-menu-item>
-->
        <el-menu-item index="../home/item-9">
          <el-icon><Phone /></el-icon>
          <span>联系编辑</span>
        </el-menu-item>

        <el-menu-item index="../home/item-10">
          <el-icon><QuestionFilled /></el-icon>
          <span>帮助中心</span>
        </el-menu-item>

        <el-divider />

        <el-menu-item index="../home/item-11">
          <el-icon><MoreFilled /></el-icon>
          <span>故障反馈</span>
        </el-menu-item>

        <el-menu-item index="../home/item-12">
          <el-icon><Refresh /></el-icon>
          <span>检查更新</span>
        </el-menu-item>

      </el-menu>
    </el-aside>

    <!-- 右侧内容区 -->
    <el-container>
      <el-main>

        <router-view v-slot="{ Component }">

          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>

        </router-view>

      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>

import {ElMessage} from "element-plus";
import request from '@/utils/request.js';
import router from "@/router/index.js";
import { ref } from 'vue'

const username = ref('')

const logout=()=>{
  localStorage.removeItem('code_information')
  location.href = '/login'
}
//防止直接访问页面
let userStr = localStorage.getItem('code_information')
if(userStr){
  let user = JSON.parse(userStr)
  if(!user.id){
    location.href ='/login'
  }
  username.value = user.username || '未登录'
}
else{
  location.href = '/login'
}


</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.avatar-wrapper {
  width: 200px;
  padding: 44px;
  background-color: #535b63;
  border-bottom: 1px solid #363d42;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  box-sizing: border-box;
}

.el-avatar--icon{

}
.username {

  font-size: 14px;
  font-weight: 500;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.avatar-wrapper .username {
  margin-left: 10px;
  color: #fff;
  font-size: 14px;
  transition: color 0.3s;
}

.avatar-wrapper:hover .username {
  color: #ffd04b;
}

/* 调整菜单位置 */
.el-menu {
  border-right: none;
}

.el-aside {
  background-color: #545c64;
}

/* 过渡动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

:deep(.el-menu) {
  --el-menu-item-height: 40px !important; /* 降低菜单项高度 */
}

:deep(.el-menu-item) {
  padding: 0 16px !important;
  margin: 4px 0; /* 增加垂直间距控制 */
}

:deep(.el-menu-item .el-icon) {
  margin-right: 8px !important; /* 缩小图标间距 */
  font-size: 16px; /* 统一图标大小 */
}

:deep(.el-menu-item span) {
  font-size: 13px; /* 缩小文字大小 */
  letter-spacing: 0.5px; /* 增加字距提升可读性 */
}

/* 紧凑分隔线 */
:deep(.el-divider--horizontal) {
  margin: 8px 0;
}

/* 头像区域优化 */
.avatar-wrapper {
  padding: 20px 16px; /* 缩小头像区域高度 */
  gap: 6px; /* 缩小头像与文字间距 */
}

.username {
  font-size: 13px; /* 匹配菜单文字大小 */
}
</style>