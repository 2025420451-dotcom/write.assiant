<template>
  <div class="container">
    <!-- 轮播图 -->
    <el-carousel height="160px" class="mb-4">
      <el-carousel-item v-for="(item, index) in banners" :key="index">
        <div class="banner-item">
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <el-button-group>
        <el-button type="primary" @click="openDialog('draft')">
          <el-icon><DocumentAdd /></el-icon>新建草稿
        </el-button>
        <el-button type="primary" @click="openDialog('work')">
          <el-icon><DocumentAdd /></el-icon>新建作品
        </el-button>
        <el-button @click="showComingSoon">
          <el-icon><Upload /></el-icon>导入草稿
        </el-button>
      </el-button-group>

      <el-button-group class="filter-group">
        <el-button
            v-for="filter in filters"
            :key="filter.value"
            :type="activeFilter === filter.value ? 'primary' : ''"
            @click="activeFilter = filter.value"
        >
          {{ filter.label }}
        </el-button>
        <el-button @click="showComingSoon">
          <el-icon><Setting /></el-icon>管理
        </el-button>
      </el-button-group>
    </div>

    <!-- 作品列表 -->
    <div v-loading="loading" class="work-grid">
      <template v-if="filteredWorks.length > 0">
        <div
            v-for="work in filteredWorks"
            :key="work.id"
            class="work-card"
            :class="{ 'draft-card': work.isDraft === 1 }"
        >
          <div class="card-header">
            <el-tag :type="work.isDraft === 1 ? 'warning' : 'success'" size="small">
              {{ work.isDraft === 1 ? '草稿' : '作品' }}
            </el-tag>
            <div class="actions">
              <el-button text @click="editWork(work.id)">
                <!--根据鼠标点击传入相应作品的id，以便后续操作执行-->
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button text @click="deleteWork(work.id)">
                <!--根据鼠标点击传入相应作品的id，以便后续操作执行-->
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>

          <h4 class="title">{{ work.titile }}</h4>

          <div class="card-footer">
            <div class="time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(work.createdAt) }}</span>
            </div>
            <el-button size="small" @click="viewDetail(work.id)">查看详情</el-button>
          </div>
        </div>
      </template>
      <div v-else class="empty-container">
        <el-empty description="暂无数据" />
      </div>
    </div>

    <!-- 创建对话框 -->
    <el-dialog
        v-model="showDialog"
        :title="dialogTitle"
        width="400px"
        @closed="resetForm"
    >
      <!-- 修改点：将label-width移动到el-form -->
      <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="80px"
      >
        <el-form-item label="标题" prop="titile">
          <el-input
              v-model="form.titile"
              placeholder="请输入标题"
              maxlength="30"
              show-word-limit
              clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button
            type="primary"
            :loading="submitting"
            @click="submitForm"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentAdd,
  Upload,
  Setting,
  Edit,
  Delete,
  Clock
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

const router = useRouter()

// 数据状态
const works = ref([])
const loading = ref(false)
const activeFilter = ref('all')
const showDialog = ref(false)
const formType = ref('')
const form = ref({ titile: '' })
const formRef = ref(null)
const submitting = ref(false)
const userInfo = JSON.parse(localStorage.getItem('code_information'));
// 静态数据
const banners = ref([
  { title: '系统公告', desc: '📢 系统维护通知：8月15日 00:00-02:00' },
  { title: '欢迎使用', desc: '🎉 欢迎使用全新创作平台！' },
  { title: '操作指南', desc: '✨ 点击查看最新使用教程' }
])

const filters = ref([
  { label: '全部', value: 'all' },
  { label: '草稿', value: 'draft' },
  { label: '作品', value: 'work' }
])

// 表单验证规则
const rules = {
  titile: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 30, message: '标题长度不能超过30个字符', trigger: 'blur' }
  ]
}

// 计算属性
const filteredWorks = computed(() => {
  const list = works.value.filter(work => {
    if (activeFilter.value === 'all') return true
    return activeFilter.value === 'draft'
        ? work.isDraft === 1
        : work.isDraft === 0
  })
  return list
})

const dialogTitle = computed(() => {
  return formType.value === 'draft' ? '新建草稿' : '新建作品'
})

// 生命周期
onMounted(() => {
  fetchWorks()
})

// 方法
const fetchWorks = async () => {
  try {
    loading.value = true
    const  data  = await request.get('/works/selectAll')
    works.value = data.data || []
  } catch (error) {
    console.error('获取作品失败:', error)
    ElMessage.error('数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const openDialog = (type) => {
  formType.value = type
  showDialog.value = true
}

const resetForm = () => {
  form.value.titile = ''
  formRef.value?.resetFields()
}

const submitForm = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    console.log(activeFilter.value)
    await request.post('/works/creatework', {
      titile: form.value.titile.trim(),
      userId: userInfo.id,
      isDraft: formType.value === 'draft' ? 1 : 0
    })

    ElMessage.success('创建成功')
    showDialog.value = false
    await fetchWorks()
  } catch (error) {
    if (error.name !== 'ValidationError') {
      console.error('创建失败:', error)
      ElMessage.error(error.response?.data?.message || '创建失败')
    }
  } finally {
    submitting.value = false
  }
}

const deleteWork = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该内容吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.delete(`/works/${id}`)
    ElMessage.success('删除成功')
    await fetchWorks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const showComingSoon = () => {
  ElMessage.info('功能开发中，敬请期待')
}

const editWork = (id) => {
  localStorage.setItem('tempData', JSON.stringify(id))
  router.push('/main/editor')
}

</script>

<style scoped>

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.banner-item {
  height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f8faff, #ebf0ff);

  h3 {
    font-size: 24px;
    margin-bottom: 12px;
    color: #303133;
  }

  p {
    color: #606266;
    font-size: 14px;
  }
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  margin: 20px 0;
}

.filter-group {
  margin-left: auto;
}

.work-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 400px;
}

.work-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
  border-left: 2px solid #67a971;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  }

  &.draft-card {
    border-left: 2px solid #e6a23c;
  }

}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .actions {
    opacity: 0;
    transition: opacity 0.3s;

    .el-button {
      padding: 4px;
      margin-left: 8px;
    }
  }

  &:hover .actions {
    opacity: 1;
  }
}

.title {
  margin: 0 0 16px;
  font-size: 16px;
  color: #303133;
  line-height: 1.5;
  min-height: 48px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #eee;
  padding-top: 12px;

  .time {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #909399;
    font-size: 12px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

.empty-container {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>