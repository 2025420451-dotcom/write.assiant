<template>
  <div class="stats-container">
    <!-- 头部控制 -->
    <div class="header">
      <el-date-picker
          v-model="dateRange"
          type="daterange"
          @change="loadData"
      />
      <el-button @click="loadData" type="primary">查询</el-button>
    </div>

    <!-- 作品字数统计卡片 -->
    <div class="books-bar-card">
      <div class="books-bar-title">各作品总字数统计</div>
      <div ref="booksBarChart" class="books-bar-chart"></div>
    </div>

    <!-- 其它统计卡片 -->
    <div class="cards">
      <div v-for="item in stats" :key="item.title" class="card">
        <div class="card-content">
          <div class="value">{{ item.value }}</div>
          <div class="title">{{ item.title }}</div>
        </div>
        <el-icon :size="24" class="icon"><component :is="item.icon" /></el-icon>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="charts">
      <div ref="lineChart" class="chart"></div>
      <div ref="pieChart" class="chart"></div>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" height="250">
      <el-table-column prop="date" label="日期" width="120" />
      <el-table-column prop="words" label="字数" />
      <el-table-column prop="time" label="时长" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import {
  Document,
  Clock,
  Calendar,
  TrendCharts
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const dateRange = ref([])
const stats = ref([
  { title: '总字数', value: '5.3万', icon: Document },
  { title: '日均字数', value: '4136', icon: Clock },
  { title: '写作天数', value: '9', icon: Calendar },
  { title: '最高纪录', value: '4635', icon: TrendCharts }
])
const tableData = ref([])
const booksBarChart = ref(null)
const booksBarChartInstance = ref(null)
const booksBarData = ref({
  books: [], // [{title: 'xxx', words: 12345}]
})
let lineChart = null
let pieChart = null

// 加载作品字数统计
const loadBooksBarData = async () => {
  // 假设后端接口 /api/stats/book-words 返回 [{title, words}]
  const res = await request.get('/api/books/stats/book-words', {
    params: {
      start: dateRange.value?.[0],
      end: dateRange.value?.[1]
    }
  })
  booksBarData.value.books = res.data || []
  renderBooksBarChart()
}

// 渲染作品字数柱状图
const renderBooksBarChart = () => {
  if (!booksBarChart.value) return
  if (!booksBarChartInstance.value) {
    booksBarChartInstance.value = echarts.init(booksBarChart.value)
  }
  const option = {
    grid: { left: 40, right: 20, top: 30, bottom: 40 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: booksBarData.value.books.map(b => b.title),
      axisLabel: { rotate: 20, fontSize: 13 }
    },
    yAxis: {
      type: 'value',
      name: '字数',
      splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
    },
    series: [{
      data: booksBarData.value.books.map(b => b.words),
      type: 'bar',
      itemStyle: {
        color: '#60a5fa',
        borderRadius: [6,6,0,0]
      },
      barWidth: 32
    }]
  }
  booksBarChartInstance.value.setOption(option)
}


const mockData = {
  stats: [
    { title: '总字数', value: '12.8万', icon: Document },
    { title: '日均字数', value: '3200', icon: Clock },
    { title: '写作天数', value: '28', icon: Calendar },
    { title: '最高纪录', value: '6800', icon: TrendCharts }
  ],
  lineData: {
    dates: ['周一','周二','周三','周四','周五','周六','周日'],
    words: [3200, 4500, 2800, 5100, 4900, 6200, 3800]
  },
  pieData: [
    { name: '小说', value: 65430 },
    { name: '散文', value: 23450 },
    { name: '其他', value: 15980 }
  ],
  tableData: [
    { date: '2023-08-01', words: '3200', time: '2h15m' },
    { date: '2023-08-02', words: '4500', time: '3h00m' }
  ]
}

const loadData = async () => {
  // 这里可对接后端其它统计接口
  await loadBooksBarData()
  // 其它统计数据可继续完善
}

onMounted(() => {
  loadData()
  // 其它图表初始化
  lineChart = echarts.init(document.querySelector('.charts .chart:first-child'))
  lineChart.setOption({
    xAxis: { data: mockData.lineData.dates },
    yAxis: {},
    series: [{
      data: mockData.lineData.words,
      type: 'line',
      smooth: true
    }]
  })
  pieChart = echarts.init(document.querySelector('.charts .chart:last-child'))
  pieChart.setOption({
    series: [{
      type: 'pie',
      data: mockData.pieData
    }]
  })
  window.addEventListener('resize', () => {
    booksBarChartInstance.value?.resize()
    lineChart?.resize()
    pieChart?.resize()
  })
})

watch(dateRange, () => {
  loadData()
})
</script>

<style scoped>
.stats-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.header {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
}
.books-bar-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  margin-bottom: 28px;
  padding: 18px 24px 10px 24px;
}
.books-bar-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #22223b;
}
.books-bar-chart {
  width: 100%;
  height: 260px;
}
.cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 25px;
}
.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.card-content {
  .value {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 5px;
  }
  .title {
    color: #666;
    font-size: 14px;
  }
}
.charts {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 25px;
  .chart {
    height: 300px;
    background: #fff;
    border-radius: 8px;
    padding: 15px;
  }
}
</style>