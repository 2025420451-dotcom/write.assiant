<script>
export default {
  name: "作品数据"
}
</script>

<template>
  <div class="work-data-container">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6" v-for="card in dataCards" :key="card.title">
        <el-card class="data-card" :body-style="{ padding: '20px' }" shadow="hover">
          <div class="card-content">
            <el-icon :size="24" :class="card.iconClass"><component :is="card.icon" /></el-icon>
            <div class="card-info">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-value">{{ formatNumber(card.value) }}</div>
              <div class="card-trend" :class="card.trend > 0 ? 'up' : 'down'">
                {{ card.trend > 0 ? '+' : '' }}{{ card.trend }}%
                <el-icon><component :is="card.trend > 0 ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row" v-loading="chartLoading">
      <el-col :span="16">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>阅读趋势</span>
              <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
                <el-radio-button label="year">年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" ref="trendChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>读者分布</span>
            </div>
          </template>
          <div class="chart-container" ref="distributionChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细数据表格 -->
    <el-card class="table-card" shadow="hover" v-loading="tableLoading">
      <template #header>
        <div class="table-header">
          <span>章节数据</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>
      <el-table 
        :data="chapterData" 
        style="width: 100%"
        border
        stripe
        highlight-current-row
        @sort-change="handleSortChange"
      >
        <el-table-column prop="title" label="章节名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="views" label="阅读量" sortable="custom" width="120">
          <template #default="{ row }">
            {{ formatNumber(row.views) }}
          </template>
        </el-table-column>
        <el-table-column prop="comments" label="评论数" sortable="custom" width="120">
          <template #default="{ row }">
            {{ formatNumber(row.comments) }}
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞数" sortable="custom" width="120">
          <template #default="{ row }">
            {{ formatNumber(row.likes) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" sortable="custom" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetails(row)">
              详情
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="章节详情"
      width="600px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="章节名称">{{ currentChapter?.title }}</el-descriptions-item>
        <el-descriptions-item label="阅读量">{{ formatNumber(currentChapter?.views) }}</el-descriptions-item>
        <el-descriptions-item label="评论数">{{ formatNumber(currentChapter?.comments) }}</el-descriptions-item>
        <el-descriptions-item label="点赞数">{{ formatNumber(currentChapter?.likes) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentChapter?.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="字数">{{ formatNumber(currentChapter?.wordCount) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { 
  View, 
  Star, 
  ChatDotRound, 
  Money, 
  ArrowUp, 
  ArrowDown,
  Download
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 加载状态
const loading = ref(false)
const chartLoading = ref(false)
const tableLoading = ref(false)

// 图表实例
let trendChart = null
let distributionChart = null
const trendChartRef = ref(null)
const distributionChartRef = ref(null)

// 数据概览卡片
const dataCards = ref([
  {
    title: '总阅读量',
    value: '12,345',
    trend: 12.5,
    icon: 'View',
    iconClass: 'blue'
  },
  {
    title: '收藏数',
    value: '2,345',
    trend: 8.2,
    icon: 'Star',
    iconClass: 'yellow'
  },
  {
    title: '评论数',
    value: '1,234',
    trend: -2.1,
    icon: 'ChatDotRound',
    iconClass: 'green'
  },
  {
    title: '打赏金额',
    value: '¥3,456',
    trend: 15.8,
    icon: 'Money',
    iconClass: 'red'
  }
])

// 时间范围选择
const timeRange = ref('month')

// 章节数据
const chapterData = ref([
  {
    id: 1,
    title: '第一章 开始',
    views: 1234,
    comments: 56,
    likes: 234,
    updateTime: '2024-03-15',
    wordCount: 3000
  },
  {
    id: 2,
    title: '第二章 发展',
    views: 2345,
    comments: 78,
    likes: 345,
    updateTime: '2024-03-16',
    wordCount: 3500
  },
  {
    id: 3,
    title: '第三章 高潮',
    views: 3456,
    comments: 90,
    likes: 456,
    updateTime: '2024-03-17',
    wordCount: 4000
  }
])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 详情对话框
const detailsDialogVisible = ref(false)
const currentChapter = ref(null)

// 格式化数字
const formatNumber = (num) => {
  if (typeof num === 'string' && num.startsWith('¥')) {
    return num
  }
  return num?.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '0'
}

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [820, 932, 901, 934, 1290, 1330, 1320],
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  }
  trendChart.setOption(option)
}

// 初始化分布图表
const initDistributionChart = () => {
  if (!distributionChartRef.value) return
  
  distributionChart = echarts.init(distributionChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: [
          { value: 1048, name: '18-24岁' },
          { value: 735, name: '25-34岁' },
          { value: 580, name: '35-44岁' },
          { value: 484, name: '45-54岁' },
          { value: 300, name: '55岁以上' }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  distributionChart.setOption(option)
}

// 处理时间范围变化
const handleTimeRangeChange = () => {
  chartLoading.value = true
  // TODO: 根据时间范围重新加载图表数据
  setTimeout(() => {
    chartLoading.value = false
  }, 500)
}

// 处理排序变化
const handleSortChange = ({ prop, order }) => {
  tableLoading.value = true
  // TODO: 根据排序条件重新加载数据
  setTimeout(() => {
    tableLoading.value = false
  }, 500)
}

// 处理导出
const handleExport = () => {
  ElMessage.success('开始导出数据')
  // TODO: 实现导出逻辑
}

// 查看详情
const handleViewDetails = (row) => {
  currentChapter.value = row
  detailsDialogVisible.value = true
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

// 监听窗口大小变化
const handleResize = () => {
  trendChart?.resize()
  distributionChart?.resize()
}

onMounted(() => {
  initTrendChart()
  initDistributionChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  distributionChart?.dispose()
})
</script>

<style scoped>
.work-data-container {
  padding: 20px;
}

.data-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.data-card:hover {
  transform: translateY(-5px);
}

.card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.card-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-trend.up {
  color: #67c23a;
}

.card-trend.down {
  color: #f56c6c;
}

.blue { color: #409eff; }
.yellow { color: #e6a23c; }
.green { color: #67c23a; }
.red { color: #f56c6c; }

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-card__header) {
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

:deep(.el-descriptions__label) {
  width: 120px;
  font-weight: bold;
}
</style>