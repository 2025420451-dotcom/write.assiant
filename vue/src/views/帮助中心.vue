<template>
  <div class="help-center-plus">
    <!-- 搜索栏 -->
    <div class="search-banner">
      <el-input
        v-model="searchQuery"
        placeholder="搜索帮助文档..."
        size="large"
        clearable
        @input="filterGuides"
      >
        <template #prepend>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="content-wrapper">
      <!-- 左侧分类导航 -->
      <div class="category-sidebar">
        <h3 class="sidebar-title">
          <el-icon><Grid /></el-icon> 帮助分类
        </h3>
        <el-menu
          :default-active="activeCategory"
          class="category-menu"
          @select="handleCategorySelect"
        >
          <el-menu-item
            v-for="category in categories"
            :key="category.id"
            :index="category.id"
          >
            <el-icon><component :is="category.icon || 'Menu'" /></el-icon>
            <span>{{ category.name }}</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧帮助内容 -->
      <div class="main-content">
        <div v-if="searchQuery && filteredCategoryData.guides.length === 0" class="search-empty">
           <el-empty :description="'未找到与 \'' + searchQuery + '\' 相关的内容'" />
        </div>
        <div v-else>
          <!-- 当前分类指南 -->
          <div class="guide-section">
            <h2 class="section-title">
              <el-icon><component :is="currentCategoryIcon" /></el-icon>
              {{ activeCategoryData.name }}
            </h2>
            <el-collapse v-model="activeGuideNames" accordion @change="handleGuideChange">
              <el-collapse-item
                v-for="item in displayedGuides"
                :key="item.id"
                :name="item.id"
                class="guide-item-plus"
              >
                <template #title>
                  <span class="guide-title-text">{{ item.title }}</span>
                  <el-tag v-if="item.new" type="success" size="small" effect="light" style="margin-left: 8px;">新</el-tag>
                  <el-tag v-if="item.hot" type="danger" size="small" effect="light" style="margin-left: 8px;">热门</el-tag>
                </template>
                <div class="guide-detail-plus">
                  <p v-html="item.content"></p>
                  <div v-if="item.videoUrl" class="video-tutorial">
                    <el-button type="primary" link @click="openVideo(item.videoUrl)">
                      <el-icon><VideoPlay /></el-icon>观看视频教程
                    </el-button>
                  </div>
                  <div v-if="item.relatedLinks && item.relatedLinks.length > 0" class="related-links">
                    <h4>相关链接：</h4>
                    <ul>
                      <li v-for="link in item.relatedLinks" :key="link.text">
                        <el-link :href="link.url" type="primary" target="_blank">{{ link.text }}</el-link>
                      </li>
                    </ul>
                  </div>
                  <div class="guide-feedback">
                    <span>这篇文章有帮助吗？</span>
                    <el-button type="text" size="small" @click="submitFeedback(item.id, 'yes')"><el-icon><CircleCheck /></el-icon> 是</el-button>
                    <el-button type="text" size="small" @click="submitFeedback(item.id, 'no')"><el-icon><CircleClose /></el-icon> 否</el-button>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
            <el-pagination
              v-if="!searchQuery && activeCategoryData.guides.length > guidesPerPage"
              layout="prev, pager, next"
              :total="activeCategoryData.guides.length"
              :page-size="guidesPerPage"
              @current-change="handlePageChange"
              class="pagination-center"
            />
          </div>

          <!-- 常见问题 -->
          <div class="faq-section">
            <h3 class="section-title-small">
              <el-icon><QuestionFilled /></el-icon> 常见问题
            </h3>
            <div class="faq-grid">
              <el-card
                v-for="faq in commonFAQs"
                :key="faq.id"
                shadow="hover"
                class="faq-card"
                @click="navigateToGuide(faq.relatedGuideId)"
              >
                <div class="faq-card-content">
                  <el-icon class="faq-icon"><ChatDotSquare /></el-icon>
                  <p>{{ faq.question }}</p>
                </div>
              </el-card>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧快速操作和提示 -->
      <div class="extra-panel">
         <div class="quick-actions-plus">
          <h3 class="panel-title">
            <el-icon><Promotion /></el-icon> 快捷入口
          </h3>
          <el-button
            v-for="action in quickActions"
            :key="action.name"
            :type="action.type || 'primary'"
            :icon="action.icon"
            plain
            class="quick-action-btn"
            @click="handleQuickAction(action)"
          >
            {{ action.name }}
          </el-button>
        </div>

        <div class="tips-panel">
          <h3 class="panel-title">
            <el-icon><Opportunity /></el-icon> 小贴士
          </h3>
          <el-carousel height="100px" direction="vertical" :autoplay="true" motion-blur indicator-position="none">
            <el-carousel-item v-for="tip in tips" :key="tip" class="tip-item">
              <p>{{ tip }}</p>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </div>

    <!-- 视频播放弹窗 -->
    <el-dialog v-model="videoDialogVisible" title="视频教程" width="60%" destroy-on-close>
      <iframe
        v-if="currentVideoUrl"
        :src="currentVideoUrl"
        width="100%"
        height="400px"
        frameborder="0"
        allowfullscreen
      ></iframe>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import {
  Search,
  Grid,
  Menu,
  Setting,
  Document,
  Picture,
  User,
  CollectionTag,
  VideoPlay,
  QuestionFilled,
  ChatDotSquare,
  Promotion,
  Opportunity,
  CircleCheck,
  CircleClose,
  Link as LinkIcon // 重命名导入的 Link 图标以避免与 el-link 冲突
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const searchQuery = ref('');
const activeCategory = ref('general'); // 默认分类
const activeGuideNames = ref([]); // 折叠面板当前展开项

const categories = ref([
  {
    id: 'general',
    name: '入门指南',
    icon: 'Grid',
    guides: [
      { id: 'g1', title: '欢迎来到帮助中心', content: '在这里您可以找到关于平台使用的各种帮助信息。我们提供了详细的操作指南、常见问题解答和实用小贴士。', new: true, hot: true },
      { id: 'g2', title: '如何快速上手？', content: '建议您先浏览"入门指南"分类下的内容，了解平台基本功能和操作流程。', videoUrl: 'https://www.bilibili.com/video/BV1Ma411c7bA/' },
      { id: 'g3', title: '忘记密码怎么办？', content: '您可以通过绑定的邮箱或手机号找回密码。点击登录页面的"忘记密码"链接，按照提示操作即可。', relatedLinks: [{ text: '安全中心', url: '#' }] },
    ],
  },
  {
    id: 'creation',
    name: '创作相关',
    icon: 'Document',
    guides: [
      { id: 'c1', title: '如何新建作品？', content: '在导航栏点击"创作中心"，然后选择"新建作品"，填写作品信息即可开始。支持 Markdown 实时预览。', hot: true, videoUrl: 'https://www.bilibili.com/video/BV1Ma411c7bA/' },
      { id: 'c2', title: '作品如何发布？', content: '完成作品编辑后，在作品管理页面找到对应作品，点击"发布"按钮，选择发布平台即可。', new: true },
      { id: 'c3', title: '素材库使用指南', content: '素材库可以帮助您管理图片、视频等创作素材，支持标签分类和快速搜索。', relatedLinks: [{ text: '了解更多素材功能', url: '#' }] },
      { id: 'c4', title: '版本管理与回退', content: '系统会自动保存您的编辑历史，您可以随时查看历史版本并进行回退操作。', new: false, hot: false },
      { id: 'c5', title: '如何设置付费章节', content: '在作品发布设置中，您可以选择特定章节设为付费内容，并设置价格。', new: false, hot: true },
    ],
  },
  {
    id: 'account',
    name: '账户与安全',
    icon: 'User',
    guides: [
      { id: 'a1', title: '如何修改个人资料？', content: '进入"个人中心"，点击"编辑资料"即可修改头像、昵称等信息。' },
      { id: 'a2', title: '绑定邮箱/手机号', content: '为了您的账户安全，建议您绑定邮箱和手机号。', hot: true },
      { id: 'a3', title: '如何注销账户？', content: '请联系客服进行账户注销操作。注意：账户注销后数据无法恢复。', relatedLinks: [{ text: '联系客服', url: '#' }] },
    ],
  },
  {
    id: 'community',
    name: '社区与互动',
    icon: 'ChatDotSquare',
    guides: [
      { id: 'co1', title: '如何发表评论？', content: '在作品阅读页面或社区帖子下方，您可以找到评论输入框，输入内容后点击发表。', new: true},
      { id: 'co2', title: '社区行为规范', content: '请遵守社区规定，友好交流，共建和谐社区环境。', hot: true, relatedLinks: [{text: '查看社区规范详情', url: '#'}] },
      { id: 'co3', title: '如何屏蔽不友善用户？', content: '在用户个人主页或评论区，您可以找到屏蔽该用户的选项。'},
    ]
  }
]);

const guidesPerPage = ref(3);
const currentPage = ref(1);

const activeCategoryData = computed(() => {
  return categories.value.find(c => c.id === activeCategory.value) || categories.value[0];
});

const currentCategoryIcon = computed(() => activeCategoryData.value.icon || 'Menu');

const filteredCategoryData = computed(() => {
  if (!searchQuery.value) {
    return activeCategoryData.value;
  }
  const lowerSearchQuery = searchQuery.value.toLowerCase();
  const filteredGuides = activeCategoryData.value.guides.filter(guide =>
    guide.title.toLowerCase().includes(lowerSearchQuery) ||
    guide.content.toLowerCase().includes(lowerSearchQuery)
  );
  return { ...activeCategoryData.value, guides: filteredGuides };
});

const displayedGuides = computed(() => {
  if (searchQuery.value) {
    return filteredCategoryData.value.guides;
  }
  const start = (currentPage.value - 1) * guidesPerPage.value;
  const end = start + guidesPerPage.value;
  return activeCategoryData.value.guides.slice(start, end);
});


const filterGuides = () => {
  currentPage.value = 1; // 搜索时重置到第一页
  // 如果搜索词为空，则清空展开项
  if (!searchQuery.value) {
    activeGuideNames.value = [];
  } else {
    // 搜索时自动展开所有匹配项
    activeGuideNames.value = filteredCategoryData.value.guides.map(g => g.id);
  }
};

watch(searchQuery, (newVal) => {
  if (!newVal) {
    activeGuideNames.value = []; // 清空搜索时，折叠所有项
  }
});


const handleCategorySelect = (index) => {
  activeCategory.value = index;
  searchQuery.value = ''; // 切换分类时清空搜索
  activeGuideNames.value = []; // 切换分类时折叠所有项
  currentPage.value = 1;
};

const handleGuideChange = (val) => {
  // accordion模式下val是当前展开项的name (string)
  // 如果需要多项展开，val会是数组
  // console.log('Collapse changed:', val);
};

const handlePageChange = (page) => {
  currentPage.value = page;
  activeGuideNames.value = []; // 翻页时折叠所有项
};


const commonFAQs = ref([
  { id: 'faq1', question: '无法登录怎么办？', relatedGuideId: 'g3' },
  { id: 'faq2', question: '如何提高作品曝光度？', relatedGuideId: 'c1' },
  { id: 'faq3', question: '如何联系客服？', relatedGuideId: 'a3' },
  { id: 'faq4', question: '如何参与社区活动？', relatedGuideId: 'co2' }
]);

const quickActions = ref([
  { name: '联系客服', icon: 'Service', type: 'success', action: 'contact' },
  { name: '提交反馈', icon: 'EditPen', type: 'warning', action: 'feedback' },
  { name: '查看公告', icon: 'Bell', type: 'info', action: 'announcement' },
]);

const tips = ref([
  '定期更新您的作品，保持读者活跃度。',
  '参与社区讨论，与其他创作者交流心得。',
  '遇到问题先尝试在帮助中心搜索答案。',
  '保护好您的账户信息，谨防诈骗。'
]);

const videoDialogVisible = ref(false);
const currentVideoUrl = ref('');

const openVideo = (url) => {
  currentVideoUrl.value = url;
  videoDialogVisible.value = true;
};

const navigateToGuide = (guideId) => {
  const targetGuide = categories.value.flatMap(c => c.guides).find(g => g.id === guideId);
  if (targetGuide) {
    const categoryId = categories.value.find(c => c.guides.some(g => g.id === guideId))?.id;
    if (categoryId) {
      activeCategory.value = categoryId;
      // 等待DOM更新后再展开
      setTimeout(() => {
        activeGuideNames.value = [guideId];
         // 滚动到指定指南
        const element = document.querySelector(`.el-collapse-item[aria-controls="el-collapse-content-${guideId.substring(1)}"]`); // DOM选择器可能需要根据实际渲染调整
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 100);
    }
  }
};


const handleQuickAction = (action) => {
  switch (action.action) {
    case 'contact':
      ElMessageBox.alert('请通过邮件 xxx@example.com 或在线聊天联系客服。', '联系客服', {
        confirmButtonText: '好的',
      });
      break;
    case 'feedback':
      ElMessageBox.prompt('请输入您的反馈意见：', '提交反馈', {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '反馈内容不能为空',
      }).then(({ value }) => {
        ElMessage.success(`感谢您的反馈：${value}`);
      }).catch(() => {
        ElMessage.info('取消提交反馈');
      });
      break;
    case 'announcement':
      ElMessage.info('正在前往公告页面...');
      // 假设跳转到公告页
      // router.push('/announcements');
      break;
  }
};

const submitFeedback = (guideId, type) => {
  ElMessage.success(`感谢您对 ${guideId} 的反馈 (${type})！`);
  // 这里可以添加实际的反馈提交逻辑
};

// 初始化时默认展开第一项（如果存在）
watch(activeCategoryData, (newData) => {
  if (newData && newData.guides.length > 0 && !searchQuery.value) {
    // activeGuideNames.value = [newData.guides[0].id]; // 默认展开第一项
  }
}, { immediate: true });

</script>

<style lang="scss" scoped>
.help-center-plus {
  padding: 20px;
  background-color: #f4f6f8;
  min-height: 100vh;
}

.search-banner {
  background-color: #fff;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  .el-input {
    --el-input-border-radius: 8px;
    .el-input-group__prepend {
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }
  }
}

.content-wrapper {
  display: flex;
  gap: 25px;
}

.category-sidebar {
  width: 280px;
  background-color: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  align-self: flex-start; // 使其不随右侧内容拉伸

  .sidebar-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    .el-icon {
      margin-right: 8px;
      color: var(--el-color-primary);
    }
  }

  .category-menu {
    border-right: none;
    .el-menu-item {
      border-radius: 6px;
      margin-bottom: 8px;
      &:hover {
        background-color: var(--el-color-primary-light-9);
      }
      &.is-active {
        background-color: var(--el-color-primary);
        color: #fff;
        .el-icon {
          color: #fff;
        }
      }
      .el-icon {
        margin-right: 10px;
      }
    }
  }
}

.main-content {
  flex: 1;
  background-color: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.search-empty {
  padding: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--el-color-primary);
  display: flex;
  align-items: center;
  .el-icon {
    margin-right: 10px;
    color: var(--el-color-primary);
    font-size: 24px;
  }
}

.guide-section {
  margin-bottom: 30px;
}

.el-collapse {
  border-top: none;
  border-bottom: none;
}

.guide-item-plus {
  margin-bottom: 15px;
  :deep(.el-collapse-item__header) {
    border-radius: 8px 8px 0 0;
    background-color: #fafafa;
    padding: 12px 20px;
    font-size: 16px;
    font-weight: 500;
    border: 1px solid #ebeef5;
    border-bottom: none; // 移除默认底部边框
    transition: background-color 0.3s;
    &:hover {
      background-color: #f0f2f5;
    }
  }
  :deep(.el-collapse-item__wrap) {
    border: 1px solid #ebeef5;
    border-top: none; // 移除默认顶部边框
    border-radius: 0 0 8px 8px;
    background-color: #fff;
  }
  :deep(.el-collapse-item__content) {
    padding: 20px;
  }
}

.guide-title-text {
 color: #303133;
}


.guide-detail-plus {
  p {
    line-height: 1.8;
    color: #555;
    margin-bottom: 15px;
    word-break: break-word;
  }

  .video-tutorial {
    margin-bottom: 15px;
    .el-button {
      font-size: 14px;
    }
  }

  .related-links {
    margin-top: 15px;
    padding: 10px;
    background-color: #f9fafb;
    border-radius: 6px;
    h4 {
      margin-bottom: 8px;
      font-size: 14px;
      color: #333;
    }
    ul {
      list-style: none;
      padding-left: 0;
      li {
        margin-bottom: 5px;
      }
    }
  }
  .guide-feedback {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px dashed #eee;
    font-size: 14px;
    color: #666;
    span {
      margin-right: 10px;
    }
    .el-button {
      margin-left: 5px;
      .el-icon {
        margin-right: 3px;
      }
    }
  }
}

.pagination-center {
  display: flex;
  justify-content: center;
  margin-top: 25px;
}

.faq-section {
  margin-top: 40px;
}

.section-title-small {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  .el-icon {
    margin-right: 8px;
    color: var(--el-color-warning);
  }
}

.faq-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 15px;
}

.faq-card {
  cursor: pointer;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
  border-radius: 8px;
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  }
  .faq-card-content {
    display: flex;
    align-items: center;
    .faq-icon {
      font-size: 20px;
      color: var(--el-color-primary);
      margin-right: 12px;
    }
    p {
      font-size: 14px;
      color: #333;
      margin: 0;
    }
  }
}

.extra-panel {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 25px;
  align-self: flex-start;
}

.quick-actions-plus, .tips-panel {
  background-color: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    .el-icon {
      margin-right: 8px;
    }
  }
}

.quick-actions-plus .panel-title .el-icon { color: var(--el-color-success); }
.tips-panel .panel-title .el-icon { color: var(--el-color-info); }


.quick-action-btn {
  width: 100%;
  margin-bottom: 10px !important; // 覆盖el-button默认margin
  justify-content: flex-start; // 图标和文字左对齐
  padding: 10px 15px !important;
  font-size: 14px;
  .el-icon {
    margin-right: 8px;
  }
}


.tip-item {
  background-color: var(--el-color-primary-light-9);
  padding: 15px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  p {
    color: var(--el-color-primary);
    font-size: 14px;
    text-align: center;
    line-height: 1.6;
    margin:0;
  }
}

// 响应式调整
@media (max-width: 992px) {
  .content-wrapper {
    flex-direction: column;
  }
  .category-sidebar, .extra-panel {
    width: 100%;
  }
  .main-content {
    order: -1; // 在小屏幕上将主要内容显示在分类导航之前
  }
}

@media (max-width: 768px) {
  .faq-grid {
    grid-template-columns: 1fr; // 单列显示
  }
  .search-banner .el-input {
    font-size: 14px; // 缩小搜索框字体
  }
}
</style>