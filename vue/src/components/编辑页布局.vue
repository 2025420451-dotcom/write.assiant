<template>
  <div class="writer-container">
    <!-- 顶栏优化 -->
    <el-header class="toolbar">
      <div class="left-tools">
        <span class="brand">流光笺</span>
        <el-dropdown trigger="click" class="font-control">
          <!-- 触发按钮 -->
          <el-button type="text" class="font-trigger">
            T字体<el-icon><arrow-down /></el-icon>
          </el-button>

          <!-- 下拉面板 -->
          <template #dropdown>
            <el-dropdown-menu class="font-panel">
              <!-- 字体选择 -->
              <el-dropdown-item>
                <div class="menu-item">
                  <span>字体类型</span>
                  <el-select
                      v-model="fontFamily"
                      placeholder="选择字体"
                      size="small"
                      style="width: 160px"
                      @change="handleFontChange"
                  >
                    <el-option
                        v-for="item in fonts"
                        :key="item"
                        :label="item"
                        :value="item"
                        :style="{ fontFamily: item }"
                    />
                  </el-select>
                </div>
              </el-dropdown-item>

              <!-- 字号调节 -->
              <el-dropdown-item>
                <div class="menu-item">
                  <span>字体大小</span>
                  <div class="slider-wrapper">
                    <span class="size-label">A</span>
                    <el-slider
                        v-model="fontSize"
                        :min="12"
                        :max="24"
                        :step="2"
                        show-stops
                        style="width: 140px"
                        @change="handleFontSizeChange"
                    />
                    <span class="size-label">A</span>
                  </div>
                </div>
              </el-dropdown-item>

              <!-- 行距调节 -->
              <el-dropdown-item>
                <div class="menu-item">
                  <span>行间距</span>
                  <el-slider
                      v-model="lineHeight"
                      :min="1"
                      :max="2"
                      :step="0.2"
                      :format-tooltip="val => val.toFixed(1)"
                      style="width: 180px"
                      @change="handleLineHeightChange"
                  />
                </div>
              </el-dropdown-item>

              <!-- 段落间距 -->
              <el-dropdown-item>
                <div class="menu-item">
                  <el-checkbox v-model="autoSpacing" @change="handleAutoSpacingChange">段间自动空1行</el-checkbox>
                </div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown trigger="click" @command="handleBgModeChange">
          <el-button type="text" class="tool-btn">背景</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="default">默认</el-dropdown-item>
              <el-dropdown-item command="eye">护眼模式</el-dropdown-item>
              <el-dropdown-item command="dark">暗黑模式</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-divider direction="vertical" />
        
        <!-- 排版功能 -->
        <el-dropdown trigger="click" @command="handleFormat">
          <el-button type="text" class="tool-btn">排版</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="center">居中对齐</el-dropdown-item>
              <el-dropdown-item command="left">左对齐</el-dropdown-item>
              <el-dropdown-item command="right">右对齐</el-dropdown-item>
              <el-dropdown-item command="justify">两端对齐</el-dropdown-item>
              <el-divider />
              <el-dropdown-item command="indent">首行缩进</el-dropdown-item>
              <el-dropdown-item command="spacing">增加段间距</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 插入功能 -->
        <el-dropdown trigger="click" @command="handleInsert">
          <el-button type="text" class="tool-btn">插入</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="divider">分隔线</el-dropdown-item>
              <el-dropdown-item command="date">当前日期</el-dropdown-item>
              <el-dropdown-item command="time">当前时间</el-dropdown-item>
              <el-divider />
              <el-dropdown-item command="quote">引用块</el-dropdown-item>
              <el-dropdown-item command="code">代码块</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="right-tools">
        <el-tooltip content="全屏模式">
          <el-button type="text" class="tool-btn" @click="handleFullscreen">
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="闭关写作">
          <el-button type="text" class="tool-btn" @click="handleFocus">
            <el-icon><Timer /></el-icon>
          </el-button>
        </el-tooltip>
        
        <el-tooltip content="历史版本">
          <el-button type="text" class="tool-btn" @click="handleHistory">
            <el-icon><Clock /></el-icon>
          </el-button>
        </el-tooltip>

        <el-dropdown trigger="click" @command="handlePublish">
          <el-button type="primary" size="small">
            发布
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="weixin">微信公众号</el-dropdown-item>
              <el-dropdown-item command="zhihu">知乎专栏</el-dropdown-item>
              <el-dropdown-item command="jianshu">简书</el-dropdown-item>
              <el-dropdown-item command="other">其他平台</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="content-container">
      <!--
      <el-aside class="custom-aside">
        <div class="aside-content">
          <el-input
              v-model="input1"
              placeholder="全书"
              :prefix-icon="Search"
              :style="{ width: `calc(100% - 20px)` }"
              class="dynamic-input"
          />

          <div style="display: flex;">
            <el-button type="primary">新建章节</el-button>
            <el-button plain @click="newVolumeTitle = true">新建分卷</el-button>
            <el-dialog v-model="newVolumeTitle">
              <el-input placeholder="输入分卷名称"></el-input>
              <el-input placeholder="输入分卷简介"></el-input>
              <template #footer>
                <el-button @click="createVolume">确认创建</el-button>
              </template>
            </el-dialog>
          </div>

          <div style="display: flex;">
            <el-button
                v-for="button in buttons"
                :key="button.text"
                :type="button.type"
                text
            >
              {{ button.text }}
            </el-button>

            <el-button-group class="ml-4" style="margin-left:auto">
              <el-button type="primary" :icon="Edit" size="small" />
              <el-button type="primary" :icon="Share" size="small"/>
              <el-button type="primary" :icon="Delete" size="small"/>
            </el-button-group>
          </div>

          <el-tree
              style="max-width: 600px"
              :data="data"
              :props="defaultProps"
              @node-click="handleNodeClick"
          />
        </div>
      </el-aside>
      -->
      <el-container>
        <el-main>
          <router-view v-slot="{ Component }">
            <component :is="Component" ref="editorRef" />
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import { 
  ArrowDown, 
  Delete, 
  Edit, 
  Plus, 
  Search, 
  Share,
  FullScreen,
  Timer,
  Clock
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const newVolumeTitle = ref(false)
const createVolume = ref()
interface Tree {
  label: string
  children?: Tree[]
}

const handleNodeClick = (data: Tree) => {
  console.log(data)
}

const data: Tree[] = [
  {
    label: '作品相关',
    children: [
      {
        label: '',
      },
    ],
  },
  {
    label: '第一卷',
    children: [
      {
        label: '第一章',
      },
    ],
  },
]

const defaultProps = {
  children: 'children',
  label: 'label',
}

// 编辑器引用
const editorRef = ref(null)

// 字体和样式相关的状态
const fontFamily = ref(localStorage.getItem('editor_font_family') || '汉仪旗黑')
const fontSize = ref(Number(localStorage.getItem('editor_font_size')) || 16)
const lineHeight = ref(Number(localStorage.getItem('editor_line_height')) || 1.5)
const autoSpacing = ref(localStorage.getItem('editor_auto_spacing') === 'true')
const bgMode = ref(localStorage.getItem('editor_bg_mode') || 'default')

// 字体列表
const fonts = [
  '汉仪旗黑',
  '思源宋体',
  '微软雅黑',
  'Arial',
  'Times New Roman'
]

const buttons = [
  { type: '', text: '目录' },
]

// 样式更新处理函数
const handleFontChange = (font) => {
  editorRef.value?.updateFontFamily(font)
}

const handleFontSizeChange = (size) => {
  editorRef.value?.updateFontSize(size)
}

const handleLineHeightChange = (height) => {
  editorRef.value?.updateLineHeight(height)
}

const handleAutoSpacingChange = (spacing) => {
  editorRef.value?.updateAutoSpacing(spacing)
}

const handleBgModeChange = (mode) => {
  editorRef.value?.updateBgMode(mode)
}

// 排版功能处理
const handleFormat = (command) => {
  if (!editorRef.value) return
  
  switch (command) {
    case 'center':
      editorRef.value.setAlignment('center')
      break
    case 'left':
      editorRef.value.setAlignment('left')
      break
    case 'right':
      editorRef.value.setAlignment('right')
      break
    case 'justify':
      editorRef.value.setAlignment('justify')
      break
    case 'indent':
      editorRef.value.setIndent()
      break
    case 'spacing':
      editorRef.value.increaseSpacing()
      break
  }
}

// 插入功能处理
const handleInsert = (command) => {
  if (!editorRef.value) return
  
  switch (command) {
    case 'divider':
      editorRef.value.insertDivider()
      break
    case 'date':
      editorRef.value.insertText(new Date().toLocaleDateString())
      break
    case 'time':
      editorRef.value.insertText(new Date().toLocaleTimeString())
      break
    case 'quote':
      editorRef.value.insertQuote()
      break
    case 'code':
      editorRef.value.insertCodeBlock()
      break
  }
}

// 发布功能处理
const handlePublish = (platform) => {
  switch (platform) {
    case 'weixin':
      ElMessage.info('正在开发微信公众号发布功能...')
      break
    case 'zhihu':
      ElMessage.info('正在开发知乎专栏发布功能...')
      break
    case 'jianshu':
      ElMessage.info('正在开发简书发布功能...')
      break
    case 'other':
      ElMessage.info('正在开发其他平台发布功能...')
      break
  }
}

// 历史版本功能
const handleHistory = () => {
  ElMessage.info('正在开发历史版本功能...')
}

// 其他功能按钮处理函数
const handleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleFocus = () => {
  if (editorRef.value) {
    editorRef.value.focusDialogVisible = true
  }
}

// 监听样式变化并保存到localStorage
watch([fontFamily, fontSize, lineHeight, autoSpacing, bgMode], ([newFont, newSize, newHeight, newSpacing, newBg]) => {
  localStorage.setItem('editor_font_family', newFont)
  localStorage.setItem('editor_font_size', newSize)
  localStorage.setItem('editor_line_height', newHeight)
  localStorage.setItem('editor_auto_spacing', newSpacing)
  localStorage.setItem('editor_bg_mode', newBg)
})

let userStr = localStorage.getItem('code_information')
if(userStr){
  let user = JSON.parse(userStr)
  if(!user.id){
    location.href ='/login'
  }
}
else{
  location.href = '/login'
}

onMounted(() => {
  document.body.classList.remove('editor-bg-default', 'editor-bg-eye', 'editor-bg-dark')
  document.body.classList.add('editor-bg-' + bgMode.value)
})

watch(bgMode, (val) => {
  document.body.classList.remove('editor-bg-default', 'editor-bg-eye', 'editor-bg-dark')
  document.body.classList.add('editor-bg-' + val)
})
</script>

<style scoped lang="scss">
.writer-container {
  height: 100vh;
  background: #fdfdfd;

  // 顶部工具栏
  .toolbar {
    height: 48px;
    background: #fbfbfb;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid #4a525a;

    .brand {
      color: #67a971;
      font-size: 18px;
      margin-right: 20px;
    }

    .font-trigger {
      padding: 0 12px;
      color: #606266;
      &:hover {
        color: #409eff;
        background: rgba(64,158,255,.1);
      }
    }

    /* 下拉面板样式 */
    .font-panel {
      width: 240px;
      padding: 12px 16px;

      .menu-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 100%;
        padding: 8px 0;

        span:first-child {
          color: #5f6165;
          min-width: 60px;
        }
      }

      .slider-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;

        .size-label {
          color: #5e6064;
          font-weight: bold;
          &:first-child { font-size: 14px }
          &:last-child { font-size: 18px }
        }
      }

      :deep(.el-checkbox__label) {
        color: #5d5f63;
      }
    }
    .tool-btn {
      color: #5d5f63;
      padding: 0 12px;

      &:hover {
        color: #409eff;
        background: rgba(255, 255, 255, 0.1);
      }
    }

    .el-divider {
      background: #475669;
      margin: 0 8px;
    }
  }
}

/* 侧边栏优化 */
.custom-aside {
  width: 15%; /* 设置侧边栏宽度为30% */
  background: #ffffff;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  transition: width 0.2s ease;
}

.aside-content {
  padding: 15px;
  height: calc(100vh - 60px);
}

.dynamic-input {
  margin-bottom: 15px;
  color: #f3f4f4;
}

.dynamic-tree {
  overflow: auto;
}

/* 主内容区域 */
.main-content {
  padding: 20px;
  height: calc(100vh - 60px);
  background: #f5f7fa;
}

:global(.editor-bg-default) {
  background: #fdfdfd !important;
}
:global(.editor-bg-eye) {
  background: #e9f7ef !important;
}
:global(.editor-bg-dark) {
  background: #23272e !important;
  color: #e0e0e0 !important;
}

/* 新增样式 */
.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  
  .el-icon {
    font-size: 16px;
  }
}

.right-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.el-dropdown-menu {
  padding: 4px 0;
  
  .el-dropdown-item {
    padding: 8px 16px;
    font-size: 14px;
    
    &:hover {
      background-color: #f5f7fa;
    }
  }
  
  .el-divider {
    margin: 4px 0;
  }
}
</style>