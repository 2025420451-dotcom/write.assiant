<script>
export default {
  name: "评论管理"
}
</script>

<template>
  <div class="comment-management-container">
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="评论内容">
          <el-input 
            v-model="filterForm.content" 
            placeholder="请输入评论内容关键词"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="评论状态">
          <el-select 
            v-model="filterForm.status" 
            placeholder="请选择状态"
            clearable
          >
            <el-option label="全部" value="" />
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetFilter">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评论列表 -->
    <el-card class="comment-list-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">评论列表</span>
            <el-tag type="info" class="total-count">共 {{ total }} 条</el-tag>
          </div>
          <div class="header-actions">
            <el-button 
              type="primary" 
              @click="handleBatchApprove" 
              :disabled="!selectedComments.length"
              :loading="batchLoading"
            >
              <el-icon><Check /></el-icon>
              批量通过
            </el-button>
            <el-button 
              type="danger" 
              @click="handleBatchReject" 
              :disabled="!selectedComments.length"
              :loading="batchLoading"
            >
              <el-icon><Close /></el-icon>
              批量拒绝
            </el-button>
            <el-button 
              type="danger" 
              @click="handleBatchDelete" 
              :disabled="!selectedComments.length"
              :loading="batchLoading"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="commentList"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        border
        stripe
        highlight-current-row
        v-loading="tableLoading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="content" label="评论内容" min-width="300">
          <template #default="{ row }">
            <div class="comment-content">
              <div class="comment-text">{{ row.content }}</div>
              <div class="comment-meta">
                <span class="chapter">章节：{{ row.chapterTitle }}</span>
                <span class="time">{{ row.createTime }}</span>
              </div>
              <div v-if="row.reply" class="comment-reply">
                <span class="reply-label">作者回复：</span>
                <span class="reply-content">{{ row.reply }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="评论用户" width="120">
          <template #default="{ row }">
            <el-tooltip 
              :content="row.userInfo" 
              placement="top" 
              :show-after="500"
            >
              <span class="user-name">{{ row.userName }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'pending'"
              type="success" 
              size="small" 
              @click="handleApprove(row)"
              :loading="row.loading"
            >
              通过
            </el-button>
            <el-button 
              v-if="row.status === 'pending'"
              type="danger" 
              size="small" 
              @click="handleReject(row)"
              :loading="row.loading"
            >
              拒绝
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              @click="handleReply(row)"
              :disabled="row.status === 'rejected'"
            >
              回复
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :loading="row.loading"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复评论"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="original-comment">
        <div class="comment-header">
          <span class="user-name">{{ currentComment?.userName }}</span>
          <span class="time">{{ currentComment?.createTime }}</span>
        </div>
        <div class="comment-content">{{ currentComment?.content }}</div>
      </div>
      <el-form 
        :model="replyForm" 
        class="reply-form"
        :rules="replyRules"
        ref="replyFormRef"
      >
        <el-form-item prop="content">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitReply"
            :loading="replyLoading"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Check, 
  Close, 
  Delete 
} from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)
const tableLoading = ref(false)
const batchLoading = ref(false)
const replyLoading = ref(false)

// 筛选表单
const filterForm = reactive({
  content: '',
  status: '',
  dateRange: []
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 评论列表数据
const commentList = ref([
  {
    id: 1,
    content: '写得真好，期待后续发展！',
    userName: '读者A',
    userInfo: '注册时间：2024-01-01\n评论数：23',
    chapterTitle: '第一章 开始',
    createTime: '2024-03-15 10:30',
    status: 'pending',
    reply: '',
    loading: false
  },
  {
    id: 2,
    content: '这个情节设计很巧妙',
    userName: '读者B',
    userInfo: '注册时间：2024-02-01\n评论数：15',
    chapterTitle: '第二章 发展',
    createTime: '2024-03-15 11:20',
    status: 'approved',
    reply: '谢谢支持，我会继续努力！',
    loading: false
  },
  {
    id: 3,
    content: '感觉有点拖沓了',
    userName: '读者C',
    userInfo: '注册时间：2024-03-01\n评论数：5',
    chapterTitle: '第三章 高潮',
    createTime: '2024-03-15 14:15',
    status: 'rejected',
    reply: '',
    loading: false
  }
])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 选中的评论
const selectedComments = ref([])

// 回复对话框
const replyDialogVisible = ref(false)
const currentComment = ref(null)
const replyFormRef = ref(null)
const replyForm = reactive({
  content: ''
})

// 回复表单验证规则
const replyRules = {
  content: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 1, max: 500, message: '回复内容长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return texts[status] || '未知'
}

// 搜索
const handleSearch = () => {
  loading.value = true
  // TODO: 实现搜索逻辑
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 重置筛选
const resetFilter = () => {
  filterForm.content = ''
  filterForm.status = ''
  filterForm.dateRange = []
  handleSearch()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedComments.value = selection
}

// 批量通过
const handleBatchApprove = () => {
  ElMessageBox.confirm(
    `确定要通过选中的 ${selectedComments.value.length} 条评论吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    batchLoading.value = true
    // TODO: 实现批量通过逻辑
    setTimeout(() => {
      batchLoading.value = false
      ElMessage.success('批量通过成功')
    }, 1000)
  })
}

// 批量拒绝
const handleBatchReject = () => {
  ElMessageBox.confirm(
    `确定要拒绝选中的 ${selectedComments.value.length} 条评论吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    batchLoading.value = true
    // TODO: 实现批量拒绝逻辑
    setTimeout(() => {
      batchLoading.value = false
      ElMessage.success('批量拒绝成功')
    }, 1000)
  })
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedComments.value.length} 条评论吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    batchLoading.value = true
    // TODO: 实现批量删除逻辑
    setTimeout(() => {
      batchLoading.value = false
      ElMessage.success('批量删除成功')
    }, 1000)
  })
}

// 通过单条评论
const handleApprove = (row) => {
  row.loading = true
  // TODO: 实现通过逻辑
  setTimeout(() => {
    row.loading = false
    row.status = 'approved'
    ElMessage.success('评论已通过')
  }, 500)
}

// 拒绝单条评论
const handleReject = (row) => {
  row.loading = true
  // TODO: 实现拒绝逻辑
  setTimeout(() => {
    row.loading = false
    row.status = 'rejected'
    ElMessage.success('评论已拒绝')
  }, 500)
}

// 回复评论
const handleReply = (row) => {
  currentComment.value = row
  replyForm.content = row.reply || ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyFormRef.value) return
  
  try {
    await replyFormRef.value.validate()
    replyLoading.value = true
    // TODO: 实现回复提交逻辑
    setTimeout(() => {
      replyLoading.value = false
      currentComment.value.reply = replyForm.content
      ElMessage.success('回复成功')
      replyDialogVisible.value = false
    }, 500)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 删除评论
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这条评论吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.loading = true
    // TODO: 实现删除逻辑
    setTimeout(() => {
      row.loading = false
      const index = commentList.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        commentList.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    }, 500)
  })
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  tableLoading.value = true
  // TODO: 重新加载数据
  setTimeout(() => {
    tableLoading.value = false
  }, 500)
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  tableLoading.value = true
  // TODO: 重新加载数据
  setTimeout(() => {
    tableLoading.value = false
  }, 500)
}

// 初始化
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.comment-management-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.total-count {
  font-size: 12px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.comment-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-text {
  line-height: 1.5;
}

.comment-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 16px;
}

.comment-reply {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 8px;
  border-radius: 4px;
  margin-top: 8px;
}

.reply-label {
  font-weight: bold;
  margin-right: 4px;
}

.user-name {
  cursor: pointer;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.original-comment {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.time {
  color: #909399;
  font-size: 12px;
}

.reply-form {
  margin-top: 16px;
}

:deep(.el-card__header) {
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

:deep(.el-button .el-icon) {
  margin-right: 4px;
}
</style>