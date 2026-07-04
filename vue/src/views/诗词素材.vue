<template>
  <div class="poetry-management">
    <!-- 搜索和操作栏 -->
    <div class="operation-bar">
      <el-input
          v-model="searchKey"
          placeholder="输入诗词标题/作者/内容搜索"
          clearable
          style="width: 300px"
          @change="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button
          type="primary"
          :icon="CirclePlus"
          @click="handleCreate"
      >
        添加诗词
      </el-button>
    </div>

    <!-- 诗词表格 -->
    <el-table
        :data="poetryList"
        stripe
        style="width: 100%"
        v-loading="loading"
    >
      <el-table-column prop="title" label="标题" width="180" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="content" label="内容" min-width="300" />

      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button
              size="small"
              @click="handleEdit(row)"
              :icon="Edit"
          />
          <el-button
              size="small"
              type="danger"
              @click="handleDelete(row.id)"
              :icon="Delete"
          />
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 15,20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
      />
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="600px"
        @closed="resetForm"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="formData.author" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
              v-model="formData.content"
              type="textarea"
              :rows="4"
              placeholder="请输入完整诗词内容"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="submitForm"
            :loading="submitting"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import {
  Search, CirclePlus, Edit, Delete
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const poetryList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const searchKey = ref('')
const formRef = ref()

// 分页配置
const pagination = reactive({
  current: 1,
  size: 15,
  total: 0
})

// 表单数据
const formData = reactive({
  id: null,
  title: '',
  author: '',
  content: '',
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 0, max: 50, message: '长度在2-50个字符', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 0, max: 20, message: '长度在2-20个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入诗词内容', trigger: 'blur' },
    { min: 0, max: 500, message: '长度在10-500个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => formData.id ? '编辑诗词' : '新增诗词')

// 获取诗词列表
const fetchPoetryList = async () => {
  try {
    loading.value = true
    const { data } = await request({
      url: '/searchpoetrylistget',
      method: 'GET',
      params: {
        pageNum: pagination.current,
        pageSize: pagination.size,
        keyword: searchKey.value
      }
    })

    poetryList.value = data.list || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('数据加载失败')
    console.error('获取数据失败:', error)
  } finally {
    loading.value = false
  }
}


// 初始化加载数据
  fetchPoetryList()

// 监听分页变化
  watch(
      () => [pagination.current, pagination.size, searchKey.value], // 监听关键词变化
      () => fetchPoetryList()
  )

// 表单操作
  const handleCreate = () => {
    resetForm()
    dialogVisible.value = true
  }

  const handleEdit = (row) => {
    Object.assign(formData, {...row})
    dialogVisible.value = true
  }

  const resetForm = () => {
    formRef.value?.resetFields()
    formData.id = null
  }

// 提交表单
  const submitForm = async () => {
    try {
      await formRef.value.validate()
      submitting.value = true

      const method = formData.id ? 'PUT' : 'POST'
      const url = formData.id ? `/poetrylistput/${formData.id}` : '/poetrylistpost'

      await request({
        url,
        method,
        data: formData
      })

      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      fetchPoetryList()
    } catch (error) {
      if (error.name !== 'ElMessage') {
        ElMessage.error(error.message || '操作失败')
      }
    } finally {
      submitting.value = false
    }
  }

// 删除操作
  const handleDelete = async (id) => {
    try {
      await ElMessageBox.confirm('确定要删除该诗词吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })

      await request({
        url: `/poetrylistdelete/${id}`,
        method: 'DELETE'
      })

      ElMessage.success('删除成功')
      // 删除后重置到第一页
      if (poetryList.value.length === 1 && pagination.current > 1) {
        pagination.current--
      }
      fetchPoetryList()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }


// 搜索操作
const handleSearch = () => {
  pagination.current = 1 // 重置到第一页
    // 不需要手动调用 fetchPoetryList，watch 会自动触发
}

</script>

<style scoped>
/* 保持原有样式不变 */
.poetry-management {
  padding: 20px;
  background: #f5f7fa;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-table {
  --el-table-border-color: #ebeef5;
  --el-table-header-bg-color: #f8f9fa;
}

.el-form-item {
  margin-bottom: 22px;
}
</style>