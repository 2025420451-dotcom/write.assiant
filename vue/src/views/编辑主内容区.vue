<template>
  <div class="book-manager" :class="bgModeClass">
    <!-- 左侧树形结构 -->
    <div class="structure-panel" :class="{ 'structure-panel-collapsed': isCollapsed }">
      <div class="collapse-btn" @click="toggleCollapse">
        <el-icon :size="20"><component :is="isCollapsed ? 'ArrowRight' : 'ArrowLeft'" /></el-icon>
      </div>
      <div class="panel-content" v-show="!isCollapsed">
        <div class="toolbar">
          <el-button type="primary" @click="handleNewVolume" round>+ 新建卷</el-button>
          <el-button :disabled="!activeVolume" @click="handleNewChapter" round>
            + 新建章节
          </el-button>
        </div>

        <el-tree
            :data="volumeList"
            node-key="id"
            :props="treeProps"
            :expand-on-click-node="false"
            highlight-current
            @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="tree-node" :class="{ 'is-hover': node.isCurrent }" @mouseenter="data._hover = true" @mouseleave="data._hover = false">
              <el-icon class="node-icon">
                <component :is="node.level === 1 ? 'Folder' : 'Document'" />
              </el-icon>
              <div class="node-content">
                <span class="title">{{ data.title }}</span>
              </div>
              <transition name="fade">
                <div class="node-actions" v-if="data._hover">
                  <el-tooltip content="编辑" v-if="node.level === 1">
                    <el-button size="small" circle text @click.stop="handleEditVolume(data)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="删除">
                    <el-button size="small" circle text type="danger" @click.stop="node.level === 1 ? handleDeleteVolume(data) : handleDeleteChapter(data)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-tooltip>
                </div>
              </transition>
            </div>
          </template>
        </el-tree>
      </div>
    </div>

    <!-- 右侧编辑区 -->
    <div class="editor-panel-large">
      <div v-if="activeChapter" class="editor-content-large" :style="editorStyle">
        <div class="editor-header">
          <el-input 
            v-model="activeChapter.title" 
            size="large" 
            class="chapter-title-input" 
            placeholder="请输入章节标题"
            :style="{ fontFamily: fontFamily, fontSize: `${Number(fontSize) + 4}px` }"
          />
          <div class="header-actions">
            <el-tooltip content="字数统计">
              <span class="word-count">{{ wordCount }} 字</span>
            </el-tooltip>
            <el-button
              type="primary"
              @click="saveChapter"
              :loading="saving"
              size="large"
              round
              class="save-btn"
            >
              保存
            </el-button>
          </div>
        </div>
        <el-input
          v-model="activeChapter.content"
          type="textarea"
          :rows="28"
          resize="none"
          placeholder="请输入章节内容..."
          class="chapter-content-input"
          @input="handleContentInput"
          ref="contentInput"
          :style="{
            fontFamily: fontFamily,
            fontSize: `${fontSize}px`,
            lineHeight: lineHeight,
            letterSpacing: '0.5px',
            textAlign: textAlign,
            textIndent: textIndent,
            padding: '20px'
          }"
        />
      </div>
      <div v-else class="empty-tip-large">
        <el-icon :size="80" style="color: #dbeafe;"><Document /></el-icon>
        <p>请选择要编辑的章节</p>
      </div>
    </div>

    <!-- 新建对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420px" :close-on-click-modal="false" class="custom-dialog">
      <el-form :model="newItem" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="newItem.title" size="large" autofocus />
        </el-form-item>
        <el-form-item label="排序" v-if="dialogType === 'chapter'">
          <el-input-number
              v-model="newItem.sort"
              :min="1"
              controls-position="right"
              size="large"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="confirmCreate" round>确定</el-button>
      </template>
    </el-dialog>

    <!-- 闭关设置对话框 -->
    <el-dialog v-model="focusDialogVisible" title="闭关设置" width="360px" class="custom-dialog">
      <el-form :model="focusSettings" label-width="100px">
        <el-form-item label="闭关时长">
          <el-input-number
            v-model="focusSettings.duration"
            :min="15"
            :max="120"
            :step="15"
            controls-position="right"
          />
          <span class="unit">分钟</span>
        </el-form-item>
        <el-form-item label="目标字数">
          <el-input-number
            v-model="focusSettings.targetWords"
            :min="0"
            :step="100"
            controls-position="right"
          />
          <span class="unit">字</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="focusDialogVisible = false" round>取消</el-button>
        <el-button type="primary" @click="startFocusMode" round>开始闭关</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed, nextTick } from 'vue'
import { Folder, Document, Edit, Delete, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getBookStructure,
  createVolume,
  createChapter,
  updateChapter,
  deleteVolume,
  deleteChapter,
  updateVolume
} from '@/api/book'

const props = defineProps({
  workId: {
    type: Number,
    required: true
  }
})

// 树形结构配置
const treeProps = {
  label: 'title',
  children: 'chapters'
}

// 组件状态
const volumeList = ref([])
const activeVolume = ref(null)
const activeChapter = ref(null)
const dialogVisible = ref(false)
const dialogType = ref('volume')
const saving = ref(false)
const isCollapsed = ref(false)
const contentInput = ref(null)
const focusDialogVisible = ref(false)
const focusSettings = reactive({
  duration: 30,
  targetWords: 1000
})

const newItem = reactive({
  title: '',
  sort: 1,
  workId: localStorage.getItem('tempData')
})

// 字体和样式相关的状态
const fontFamily = ref(localStorage.getItem('editor_font_family') || '汉仪旗黑')
const fontSize = ref(Number(localStorage.getItem('editor_font_size')) || 16)
const lineHeight = ref(Number(localStorage.getItem('editor_line_height')) || 1.5)
const autoSpacing = ref(localStorage.getItem('editor_auto_spacing') === 'true')
const bgMode = ref(localStorage.getItem('editor_bg_mode') || 'default')
const textAlign = ref(localStorage.getItem('editor_text_align') || 'left')
const textIndent = ref(localStorage.getItem('editor_text_indent') || '0')

// 计算样式
const editorStyle = computed(() => ({
  fontFamily: fontFamily.value,
  fontSize: `${fontSize.value}px`,
  lineHeight: lineHeight.value,
  letterSpacing: '0.5px'
}))

const bgModeClass = computed(() => `bg-mode-${bgMode.value}`)

// 计算字数
const wordCount = computed(() => {
  if (!activeChapter.value?.content) return 0
  return activeChapter.value.content.replace(/\s/g, '').length
})

// 监听样式变化并保存到localStorage
watch([fontFamily, fontSize, lineHeight, autoSpacing, bgMode, textAlign, textIndent], ([newFont, newSize, newHeight, newSpacing, newBg, newAlign, newIndent]) => {
  localStorage.setItem('editor_font_family', newFont)
  localStorage.setItem('editor_font_size', newSize)
  localStorage.setItem('editor_line_height', newHeight)
  localStorage.setItem('editor_auto_spacing', newSpacing)
  localStorage.setItem('editor_bg_mode', newBg)
  localStorage.setItem('editor_text_align', newAlign)
  localStorage.setItem('editor_text_indent', newIndent)
})

// 更新样式的方法
const updateFontFamily = (font) => {
  fontFamily.value = font
  localStorage.setItem('editor_font_family', font)
}

const updateFontSize = (size) => {
  fontSize.value = size
  localStorage.setItem('editor_font_size', size)
}

const updateLineHeight = (height) => {
  lineHeight.value = height
  localStorage.setItem('editor_line_height', height)
}

const updateAutoSpacing = (spacing) => {
  autoSpacing.value = spacing
  localStorage.setItem('editor_auto_spacing', spacing)
}

const updateBgMode = (mode) => {
  bgMode.value = mode
  localStorage.setItem('editor_bg_mode', mode)
}

// 新增排版方法
const setAlignment = (align) => {
  textAlign.value = align
  localStorage.setItem('editor_text_align', align)
}

const setIndent = () => {
  textIndent.value = textIndent.value === '0' ? '2em' : '0'
  localStorage.setItem('editor_text_indent', textIndent.value)
}

const increaseSpacing = () => {
  const currentSpacing = Number(lineHeight.value)
  const newSpacing = Math.min(currentSpacing + 0.2, 2)
  lineHeight.value = newSpacing
  localStorage.setItem('editor_line_height', newSpacing)
}

// 新增插入方法
const insertText = (text) => {
  if (!contentInput.value) return
  const textarea = contentInput.value.$el.querySelector('textarea')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const content = activeChapter.value.content
  
  activeChapter.value.content = content.substring(0, start) + text + content.substring(end)
  
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + text.length, start + text.length)
  })
}

const insertQuote = () => {
  if (!contentInput.value) return
  const textarea = contentInput.value.$el.querySelector('textarea')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const content = activeChapter.value.content
  const selectedText = content.substring(start, end)
  const quote = selectedText ? `\n\n> ${selectedText}\n\n` : '\n\n> 引用内容\n\n'
  
  activeChapter.value.content = content.substring(0, start) + quote + content.substring(end)
  
  nextTick(() => {
    textarea.focus()
    const newCursorPos = start + quote.length - (selectedText ? 0 : 6)
    textarea.setSelectionRange(newCursorPos, newCursorPos)
  })
}

const insertCodeBlock = () => {
  if (!contentInput.value) return
  const textarea = contentInput.value.$el.querySelector('textarea')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const content = activeChapter.value.content
  const selectedText = content.substring(start, end)
  const codeBlock = selectedText ? `\n\n\`\`\`\n${selectedText}\n\`\`\`\n\n` : '\n\n```\n代码块\n```\n\n'
  
  activeChapter.value.content = content.substring(0, start) + codeBlock + content.substring(end)
  
  nextTick(() => {
    textarea.focus()
    const newCursorPos = start + codeBlock.length - (selectedText ? 0 : 7)
    textarea.setSelectionRange(newCursorPos, newCursorPos)
  })
}

// 处理内容输入
const handleContentInput = () => {
  // 可以在这里添加自动保存等功能
}

// 切换侧边栏
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 开始闭关模式
const startFocusMode = () => {
  focusDialogVisible.value = false
  // 进入全屏
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  }
  
  // 设置定时器
  const endTime = Date.now() + focusSettings.duration * 60 * 1000
  const timer = setInterval(() => {
    const remaining = Math.ceil((endTime - Date.now()) / 1000 / 60)
    if (remaining <= 0) {
      clearInterval(timer)
      document.exitFullscreen()
      ElMessage.success('闭关结束！')
    }
  }, 60000)

  // 监听退出全屏事件
  document.addEventListener('fullscreenchange', () => {
    if (!document.fullscreenElement) {
      clearInterval(timer)
      ElMessage.warning('闭关已中断！')
    }
  })
}

// 插入分隔线
const insertDivider = () => {
  if (!contentInput.value) return
  const textarea = contentInput.value.$el.querySelector('textarea')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const content = activeChapter.value.content
  const divider = '\n\n' + '─'.repeat(50) + '\n\n'
  
  activeChapter.value.content = content.substring(0, start) + divider + content.substring(end)
  
  // 设置光标位置
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + divider.length, start + divider.length)
  })
}

// 暴露方法给父组件
defineExpose({
  updateFontFamily,
  updateFontSize,
  updateLineHeight,
  updateAutoSpacing,
  updateBgMode,
  insertDivider,
  setAlignment,
  setIndent,
  increaseSpacing,
  insertText,
  insertQuote,
  insertCodeBlock
})

// 初始化加载数据
const loadStructure = async () => {
  try {
    const data = JSON.parse(localStorage.getItem('tempData'))
    const res = await getBookStructure(data)
    volumeList.value = res.data.map(v => ({
      ...v,
      chapters: v.chapters.sort((a, b) => a.sort - b.sort)
    }))
  } catch (error) {
    console.error('加载数据结构失败:', error)
  }
}

// 时间格式化
const formatTime = (isoString) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const pad = (n) => n.toString().padStart(2, '0')

// 节点点击处理
const handleNodeClick = (data, node) => {
  if (node.level === 1) {
    activeVolume.value = data
    activeChapter.value = null
  } else {
    activeChapter.value = { ...data }
  }
}

// 保存章节
const saveChapter = async () => {
  saving.value = true
  try {
    await updateChapter(activeChapter.value)
    await loadStructure()
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 新建卷处理
const handleNewVolume = () => {
  dialogType.value = 'volume'
  Object.assign(newItem, {
    title: '',
    sort: volumeList.value.length + 1,
  })
  dialogVisible.value = true
}

// 新建章节处理
const handleNewChapter = () => {
  dialogType.value = 'chapter'
  Object.assign(newItem, {
    title: '',
    sort: activeVolume.value.chapters.length + 1
  })
  dialogVisible.value = true
}

// 删除卷
const handleDeleteVolume = async (volume) => {
  try {
    await ElMessageBox.confirm('确定要删除该卷及其下所有章节吗？', '警告', { type: 'warning' })
    await deleteVolume(volume.id)
    ElMessage.success('删除成功')
    await loadStructure()
    if (activeVolume.value && activeVolume.value.id === volume.id) activeVolume.value = null
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// 删除章节
const handleDeleteChapter = async (chapter) => {
  try {
    await ElMessageBox.confirm('确定要删除该章节吗？', '警告', { type: 'warning' })
    await deleteChapter(chapter.id)
    ElMessage.success('删除成功')
    await loadStructure()
    if (activeChapter.value && activeChapter.value.id === chapter.id) activeChapter.value = null
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// 编辑卷
const handleEditVolume = (volume) => {
  dialogType.value = 'volume-edit'
  Object.assign(newItem, { ...volume })
  dialogVisible.value = true
}

// 弹窗关闭时重置表单
watch(dialogVisible, (val) => {
  if (!val) {
    Object.assign(newItem, {
      title: '',
      sort: 1,
      workId: props.workId,
      id: undefined
    })
  }
})

// 修改confirmCreate支持卷编辑
const confirmCreate = async () => {
  try {
    if (dialogType.value === 'volume') {
      await createVolume(newItem)
    } else if (dialogType.value === 'volume-edit') {
      await updateVolume({
        id: newItem.id,
        title: newItem.title,
        sort: newItem.sort,
        workId: newItem.workId
      })
    } else {
      await createChapter({
        ...newItem,
        volumeId: activeVolume.value.id
      })
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    await loadStructure()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 初始化加载
onMounted(() => {
  loadStructure()
})
</script>

<style scoped>
.book-manager {
  display: flex;
  height: 100vh;
  background: linear-gradient(90deg, #f8fafc 60%, #e0e7ef 100%);
  font-family: 'HarmonyOS Sans', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

.structure-panel {
  width: 340px;
  background: #fff;
  border-radius: 18px;
  margin: 24px 0 24px 24px;
  box-shadow: 0 4px 24px 0 rgba(80,120,200,0.07);
  padding: 20px 12px 20px 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: relative;
}

.structure-panel-collapsed {
  width: 60px;
  padding: 20px 0;
}

.collapse-btn {
  position: absolute;
  right: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 1;
}

.panel-content {
  flex: 1;
  overflow: hidden;
}

.toolbar {
  margin-bottom: 18px;
  display: flex;
  gap: 10px;
  justify-content: center;
}

.el-tree {
  background: transparent;
}

.tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 8px 6px;
  border-radius: 10px;
  transition: background 0.2s, box-shadow 0.2s;
  position: relative;
}

.tree-node.is-hover,
.tree-node:hover {
  background: #f0f6ff;
  box-shadow: none;
}

.node-icon {
  font-size: 20px;
  color: #60a5fa;
  margin-right: 14px;
}

.node-content .title {
  font-size: 15px;
  font-weight: 500;
  color: #22223b;
  letter-spacing: 0.5px;
}

.node-actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
  align-items: center;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.editor-panel-large {
  flex: 1;
  height: 100vh;
  padding: 0 0 0 0;
  display: flex;
  flex-direction: column;
  background: transparent;
  justify-content: center;
  align-items: stretch;
}

.editor-content-large {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(80,120,200,0.09);
  margin: 32px 32px 32px 0;
  padding: 40px 48px 32px 48px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
}

.editor-header {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  gap: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.word-count {
  color: #64748b;
  font-size: 14px;
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 20px;
}

.chapter-title-input {
  font-size: 22px;
  font-weight: 600;
  flex: 1;
}

.save-btn {
  align-self: flex-start;
  font-size: 16px;
  padding: 0 32px;
  height: 44px;
}

.chapter-content-input {
  font-size: 17px;
  min-height: 420px;
  flex: 1;
  border-radius: 12px;
  line-height: 1.8;
  padding: 20px;
}

.chapter-content-input :deep(textarea) {
  font-family: inherit;
  line-height: inherit;
  padding: 20px;
  background: transparent;
  border: none;
  resize: none;
  box-shadow: none;
  transition: all 0.3s ease;
}

.chapter-content-input :deep(textarea):focus {
  box-shadow: none;
  outline: none;
}

.empty-tip-large {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #b6c2d6;
  font-size: 22px;
  user-select: none;
}

.empty-tip-large p {
  margin-top: 22px;
  font-size: 22px;
  color: #7b8fa1;
}

.custom-dialog .el-dialog__body {
  padding-top: 24px;
  padding-bottom: 12px;
}

.custom-dialog .el-dialog {
  border-radius: 18px;
  box-shadow: 0 8px 32px 0 rgba(80,120,200,0.13);
}

/* 背景模式样式 */
.bg-mode-default {
  background: linear-gradient(90deg, #f8fafc 60%, #e0e7ef 100%);
}

.bg-mode-eye {
  background: linear-gradient(90deg, #e9f7ef 60%, #d4e7e0 100%);
}

.bg-mode-dark {
  background: linear-gradient(90deg, #23272e 60%, #1a1d23 100%);
  color: #e0e0e0;
}

.bg-mode-dark .structure-panel {
  background: #2c313a;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.2);
}

.bg-mode-dark .editor-content-large {
  background: #2c313a;
  color: #e0e0e0;
}

.bg-mode-dark .chapter-title-input :deep(input) {
  background: #2c313a;
  color: #e0e0e0;
}

.bg-mode-dark .chapter-content-input :deep(textarea) {
  background: #2c313a;
  color: #e0e0e0;
}

.bg-mode-dark .word-count {
  background: #3a3f48;
  color: #a0aec0;
}

/* 自动段落间距 */
.chapter-content-input :deep(textarea) {
  white-space: pre-wrap;
}

.chapter-content-input :deep(textarea)::placeholder {
  color: #a0aec0;
}

.bg-mode-dark .chapter-content-input :deep(textarea)::placeholder {
  color: #718096;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.bg-mode-dark ::-webkit-scrollbar-thumb {
  background: #4a5568;
}

.bg-mode-dark ::-webkit-scrollbar-thumb:hover {
  background: #2d3748;
}

/* 护眼模式 */
.bg-mode-eye .chapter-content-input :deep(textarea) {
  background: #f0f7f4;
  color: #2d3748;
}

/* 引用块样式 */
.chapter-content-input :deep(textarea) {
  white-space: pre-wrap;
}

/* 代码块样式 */
.chapter-content-input :deep(textarea) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}
</style>