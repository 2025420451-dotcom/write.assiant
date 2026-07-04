<template>
  <div class="feedback-container">
    <el-card class="main-card" shadow="hover">
      <!-- 标题区 -->
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><MessageBox /></el-icon>
          <span class="header-text">用户反馈管理</span>
          <el-tag type="info" effect="dark">共 {{ total }} 条反馈</el-tag>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
          :data="tableData"
          v-loading="loading"
          element-loading-text="数据加载中..."
          element-loading-background="rgba(255, 255, 255, 0.8)"
          stripe
          style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />

        <el-table-column label="反馈类型" width="120">
          <template #default="{ row }">
            <el-tag :type="typeStyle[row.type]">
              {{ typeMap[row.type] || '未知类型' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="问题描述" min-width="200">
          <template #default="{ row }">
            <div class="description-box">
              {{ row.description }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" width="150">
          <template #default="{ row }">
            <el-link type="primary" :href="`tel:${row.contact}`">
              {{ formatContact(row.contact) }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="create_time" label="提交时间" width="180" sortable>
          <template #default="{ row }">
            <el-icon><Clock /></el-icon>
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已解决' : '未解决' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 1"
              type="info" 
              link 
              disabled
            >
              <el-icon><Timer /></el-icon>
              解决于 {{ formatTime(row.handleTime) }}
            </el-button>
            <el-button 
              v-if="row.status === 1"
              type="warning" 
              link 
              @click="handleReopen(row)"
            >
              <el-icon><RefreshRight /></el-icon>重新打开
            </el-button>
            <el-button 
              v-if="row.status === 0"
              type="success" 
              link 
              @click="handleResolve(row)"
            >
              <el-icon><Check /></el-icon>标记解决
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
            background
            layout="prev, pager, next, sizes"
            :page-sizes="[10, 20, 50]"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Clock, MessageBox, View, Check, Timer, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import request from '@/utils/request' // 导入封装好的请求库

// 状态管理
const tableData = ref([])
const loading = ref(true)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 类型映射
const typeMap = {
  1: '界面问题',
  2: '功能异常',
  3: '优化建议',
  4: '其他反馈'
}

const typeStyle = {
  1: 'primary',
  2: 'danger',
  3: 'success',
  4: 'warning'
}

// 数据加载
const loadData = async () => {
  try {
    loading.value = true
    const { data } = await request.get('/GetAllFeedBack', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        _t: Date.now() // 防止缓存
      }
    })

    tableData.value = data.list || []
    total.value = data.total || 0

  } catch (error) {
    console.error('数据加载失败:', error)
    ElMessage.error('数据加载失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return '暂无'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

// 联系方式脱敏
const formatContact = (contact) => {
  return contact.replace()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  loadData()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadData()
}

// 处理标记解决
const handleResolve = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要将该反馈标记为已解决吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await request.put('/UpdateFeedbackStatus', null, {
      params: {
        id: row.id,
        status: 1
      }
    })
    
    if (res.code === '200') {
      ElMessage.success('已成功标记为已解决')
      loadData() // 重新加载数据
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + error.message)
    }
  }
}

// 处理重新打开
const handleReopen = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要将该反馈重新标记为未解决吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await request.put('/UpdateFeedbackStatus', null, {
      params: {
        id: row.id,
        status: 0
      }
    })
    
    if (res.code === '200') {
      ElMessage.success('已重新标记为未解决')
      loadData() // 重新加载数据
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + error.message)
    }
  }
}

// 初始化加载
onMounted(loadData)
</script>

<style lang="scss" scoped>
/* 保持原有样式不变 */
.feedback-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;

  .main-card {
    border-radius: 12px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.06);

    :deep(.el-card__header) {
      background: linear-gradient(135deg, #f8f9fc, #e9ecef);
      border-bottom: 1px solid #ebeef5;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;

    .header-icon {
      font-size: 24px;
      color: #409eff;
    }

    .header-text {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }

  .description-box {
    line-height: 1.6;
    max-height: 60px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    padding: 16px 0;
  }

  :deep(.el-table) {
    --el-table-border-color: #ebeef5;
    --el-table-header-bg-color: #f8f9fc;

    th.el-table__cell {
      font-weight: 600;
      color: #606266;
    }

    .el-table__row:hover {
      td {
        background-color: #f5f7fa !important;
      }
    }
  }

  .el-button {
    margin-left: 8px;
    
    .el-icon {
      margin-right: 4px;
    }

    &.is-disabled {
      opacity: 0.8;
      cursor: default;
    }

    &.el-button--warning {
      &:hover {
        color: #e6a23c;
      }
    }
  }
}
</style>