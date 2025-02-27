# 创建测评历史页面组件
<template>
  <div class="assessment-history">
    <a-card title="测评历史">
      <!-- 历史记录列表 -->
      <a-table
        :loading="loading"
        :data-source="historyList"
        :columns="columns"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <!-- 问卷标题列 -->
        <template #questionnaire="{ text }">
          {{ text.title }}
        </template>

        <!-- 总分列 -->
        <template #totalScore="{ text, record }">
          {{ text }} / {{ record.questionnaire.totalScore }}
        </template>

        <!-- 通过状态列 -->
        <template #passed="{ text }">
          <a-tag :color="text ? 'success' : 'error'">
            {{ text ? '通过' : '未通过' }}
          </a-tag>
        </template>

        <!-- 问卷状态列 -->
        <template #questionnaireStatus="{ text }">
          <a-tag :color="getStatusColor(text)">
            {{ getStatusText(text) }}
          </a-tag>
        </template>

        <!-- 完成时间列 -->
        <template #createTime="{ text }">
          {{ formatDateTime(text) }}
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <router-link :to="`/student/assessment-result/${record.id}`">
              查看详情
            </router-link>
            <a-button 
              v-if="record.questionnaire.status !== 'ARCHIVED'"
              type="link"
              @click="$router.push(`/student/assessment/${record.questionnaire.id}`)"
            >
              重新测评
            </a-button>
            <a-tag v-else color="default">已归档</a-tag>
          </a-space>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import type { AssessmentResultResponse } from '@/types/assessment';
import { assessmentApi } from '@/api/assessment';
import { message } from 'ant-design-vue';

// 表格列定义
const columns = [
  {
    title: '问卷标题',
    dataIndex: ['questionnaire'],
    slots: { customRender: 'questionnaire' },
    width: '30%'
  },
  {
    title: '得分',
    dataIndex: 'totalScore',
    slots: { customRender: 'totalScore' },
    width: '15%'
  },
  {
    title: '是否通过',
    dataIndex: 'passed',
    slots: { customRender: 'passed' },
    width: '15%'
  },
  {
    title: '问卷状态',
    dataIndex: ['questionnaire', 'status'],
    slots: { customRender: 'questionnaireStatus' },
    width: '15%'
  },
  {
    title: '完成时间',
    dataIndex: 'createTime',
    slots: { customRender: 'createTime' },
    width: '15%'
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: '20%'
  }
];

// 状态定义
const loading = ref(false);
const historyList = ref<AssessmentResultResponse[]>([]);
const pagination = ref<TablePaginationConfig>({
  total: 0,
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 加载历史记录
const fetchHistory = async () => {
  loading.value = true;
  try {
    const { current = 1, pageSize = 10 } = pagination.value;
    const response = await assessmentApi.getHistory({
      page: current - 1,
      size: pageSize
    });
    historyList.value = response.content;
    console.log("historyList.value", historyList.value);
    // 调试输出每个问卷的状态
    if (historyList.value && historyList.value.length > 0) {
      console.log("问卷状态信息：", historyList.value.map(item => ({
        id: item.id,
        questionnaire_id: item.questionnaire.id,
        title: item.questionnaire.title,
        status: item.questionnaire.status
      })));
    }
    pagination.value.total = response.totalElements;
  } catch (error) {
    message.error('获取历史记录失败');
  } finally {
    loading.value = false;
  }
};

// 处理表格分页变化
const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  fetchHistory();
};

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取问卷状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return '已发布';
    case 'ARCHIVED':
      return '已归档';
    case 'DRAFT':
      return '草稿';
    default:
      return '未知状态';
  }
};

// 获取问卷状态颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return 'green';
    case 'ARCHIVED':
      return 'gray';
    case 'DRAFT':
      return 'orange';
    default:
      return 'default';
  }
};

// 生命周期钩子
onMounted(() => {
  fetchHistory();
});
</script>

<style scoped>
.assessment-history {
  padding: 24px;
}

.ant-card {
  margin-bottom: 24px;
}

.ant-table-wrapper {
  background: #fff;
}
</style> 