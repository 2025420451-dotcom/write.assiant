<script>
export default {
  name: "订阅统计"
}
</script>

<template>
  <div class="subscription-stats-container">
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
              <span>订阅趋势</span>
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
              <span>订阅类型分布</span>
            </div>
          </template>
          <div class="chart-container" ref="distributionChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 订阅列表 -->
    <el-card class="table-card" shadow="hover" v-loading="tableLoading">
      <template #header>
        <div class="table-header">
          <span>订阅记录</span>
          <div class="header-actions">
            <el-button type="primary" size="small" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>
      <el-table 
        :data="subscriptionList" 
        style="width: 100%"
        border
        stripe
        highlight-current-row
        @sort-change="handleSortChange"
      >
        <el-table-column prop="userName" label="用户名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="type" label="订阅类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getSubscriptionType(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="订阅金额" sortable="custom" width="120">
          <template #default="{ row }">
            ¥{{ formatNumber(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始时间" sortable="custom" width="180" />
        <el-table-column prop="endDate" label="结束时间" sortable="custom" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
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
      title="订阅详情"
      width="600px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名称">{{ currentSubscription?.userName }}</el-descriptions-item>
        <el-descriptions-item label="订阅类型">{{ currentSubscription?.type }}</el-descriptions-item>
        <el-descriptions-item label="订阅金额">¥{{ formatNumber(currentSubscription?.amount) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentSubscription?.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentSubscription?.endDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentSubscription?.status }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentSubscription?.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentSubscription?.orderNo }}</el-descriptions-item>
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
  User, 
  Money, 
  Timer, 
  Star, 
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
    title: '总订阅数',
    value: '8,234',
    trend: 15.5,
    icon: 'User',
    iconClass: 'blue'
  },
  {
    title: '本月收入',
    value: '¥45,678',
    trend: 12.3,
    icon: 'Money',
    iconClass: 'green'
  },
  {
    title: '活跃订阅',
    value: '6,789',
    trend: 8.7,
    icon: 'Timer',
    iconClass: 'yellow'
  },
  {
    title: '续订率',
    value: '85%',
    trend: 5.2,
    icon: 'Star',
    iconClass: 'red'
  }
])

// 时间范围选择
const timeRange = ref('month')

// 订阅列表数据
const subscriptionList = ref([
  {
    id: 1,
    userName: '张三',
    type: '月度会员',
    amount: 29.9,
    startDate: '2024-03-01',
    endDate: '2024-04-01',
    status: '生效中',
    paymentMethod: '微信支付',
    orderNo: 'SUB202403010001'
  },
  {
    id: 2,
    userName: '李四',
    type: '年度会员',
    amount: 299,
    startDate: '2024-02-15',
    endDate: '2025-02-15',
    status: '生效中',
    paymentMethod: '支付宝',
    orderNo: 'SUB202402150002'
  },
  {
    id: 3,
    userName: '王五',
    type: '季度会员',
    amount: 79.9,
    startDate: '2024-01-01',
    endDate: '2024-04-01',
    status: '已过期',
    paymentMethod: '银行卡',
    orderNo: 'SUB202401010003'
  }
])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

// 详情对话框
const detailsDialogVisible = ref(false)
const currentSubscription = ref(null)

// 格式化数字
const formatNumber = (num) => {
  if (typeof num === 'string' && num.startsWith('¥')) {
    return num
  }
  return num?.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '0'
}

// 获取订阅类型样式
const getSubscriptionType = (type) => {
  const types = {
    '月度会员': 'info',
    '季度会员': 'success',
    '年度会员': 'warning',
    '终身会员': 'danger'
  }
  return types[type] || 'info'
}

// 获取状态样式
const getStatusType = (status) => {
  const types = {
    '生效中': 'success',
    '已过期': 'info',
    '已取消': 'danger'
  }
  return types[status] || 'info'
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
      data: [120, 132, 101, 134, 90, 230, 210],
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
          { value: 1048, name: '月度会员' },
          { value: 735, name: '季度会员' },
          { value: 580, name: '年度会员' },
          { value: 484, name: '终身会员' }
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
  currentSubscription.value = row
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
.subscription-stats-container {
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