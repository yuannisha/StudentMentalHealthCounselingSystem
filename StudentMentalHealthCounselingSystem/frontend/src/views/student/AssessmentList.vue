<template>
  <div class="assessment-list">
    <a-card title="心理测评">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索问卷标题"
          style="width: 300px"
          @search="handleSearch"
        />
      </div>

      <!-- 问卷列表 -->
      <a-list
        :loading="loading"
        :data-source="questionnaires"
        :pagination="pagination"
        item-layout="vertical"
      >
        <template #renderItem="{ item }">
          <a-list-item key="item.id">
            <a-card :title="item.title" :bordered="false" style="width: 100%">
              <template #extra>
                <a-button type="primary" @click="startAssessment(item.id)">
                  开始测评
                </a-button>
              </template>
              <a-descriptions :column="2">
                <a-descriptions-item label="总分">
                  {{ calculateTotalScore(item) }}
                </a-descriptions-item>
                <a-descriptions-item label="及格分">
                  {{ item.passingScore }}
                </a-descriptions-item>
                <a-descriptions-item label="题目数量">
                  {{ item.questions.length }}
                </a-descriptions-item>
                <a-descriptions-item label="更新时间">
                  {{ formatDateTime(item.updatedAt || '') }}
                </a-descriptions-item>
              </a-descriptions>
              <p class="description">{{ item.description || '暂无描述' }}</p>
            </a-card>
          </a-list-item>
        </template>
        <template #empty>
          <a-empty description="暂无问卷" />
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import { questionnaireApi } from '@/api/questionnaire';
import type { Questionnaire, QuestionnaireListRequest } from '@/types/questionnaire';

const router = useRouter();
const loading = ref(false);
const searchKeyword = ref('');
const questionnaires = ref<Questionnaire[]>([]);

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  onChange: (page: number, pageSize: number) => {
    pagination.current = page;
    pagination.pageSize = pageSize;
    fetchQuestionnaires();
  }
});

// 计算总分
const calculateTotalScore = (questionnaire: Questionnaire) => {
  return questionnaire.questions.reduce((sum, q) => sum + q.score, 0);
};

// 获取问卷列表
const fetchQuestionnaires = async () => {
  loading.value = true;
  try {
    console.log('开始获取问卷列表...');
    const params: QuestionnaireListRequest = {
      page: (pagination.current || 1) - 1,
      size: pagination.pageSize || 10,
      keyword: searchKeyword.value || undefined,
      status: 'PUBLISHED'
    };
    console.log('请求参数:', JSON.stringify(params));
    
    const response = await questionnaireApi.getQuestionnaires(params);
    console.log('获取问卷列表响应:', response);
    
    if (response && response.content) {
      questionnaires.value = response.content;
      pagination.total = response.totalElements || 0;
      console.log('更新后的问卷列表:', questionnaires.value);
      console.log('更新后的分页信息:', pagination);
    } else {
      console.error('返回数据格式错误:', response);
      throw new Error('返回数据格式错误');
    }
  } catch (error: any) {
    console.error('获取问卷列表失败:', error);
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      stack: error.stack
    });
    message.error(error.response?.data?.message || '获取问卷列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 搜索问卷
const handleSearch = () => {
  pagination.current = 1;
  fetchQuestionnaires();
};

// 开始测评
const startAssessment = (id: number) => {
  router.push(`/student/assessment/${id}`);
};

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '暂无';
  try {
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return '日期格式错误';
  }
};

onMounted(() => {
  fetchQuestionnaires();
});
</script>

<style scoped>
.assessment-list {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.search-bar {
  margin-bottom: 16px;
}

.description {
  margin-top: 16px;
  color: rgba(0, 0, 0, 0.45);
}

:deep(.ant-list-item) {
  padding: 0;
  margin-bottom: 16px;
}

:deep(.ant-card) {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

:deep(.ant-card-head) {
  border-bottom: none;
}

:deep(.ant-descriptions-item-label) {
  color: rgba(0, 0, 0, 0.45);
}

:deep(.ant-descriptions-item-content) {
  color: rgba(0, 0, 0, 0.85);
}
</style> 