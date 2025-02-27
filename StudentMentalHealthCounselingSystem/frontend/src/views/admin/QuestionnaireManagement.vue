<template>
  <div class="questionnaire-management">
    <!-- 搜索和操作栏 -->
    <div class="search-bar">
      <a-form layout="inline">
        <a-form-item label="标题">
          <a-input v-model:value="searchParams.keyword" placeholder="请输入问卷标题" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select 
            v-model:value="searchParams.statuses" 
            placeholder="请选择状态" 
            allowClear
            @change="handleStatusChange"
          >
            <a-select-option value="DRAFT">草稿</a-select-option>
            <a-select-option value="PUBLISHED">已发布</a-select-option>
            <a-select-option value="ARCHIVED">已归档</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button style="margin-left: 8px" @click="handleAdd">新建问卷</a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 问卷列表 -->
    <a-table
      :columns="columns"
      :data-source="dataSource"
      :pagination="pagination"
      :loading="loading"
      @change="handleTableChange"
    >
      <!-- 状态列 -->
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <!-- 操作列 -->
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button 
              v-if="record.status === 'DRAFT'"
              type="link" 
              @click="handlePublish(record.id)"
            >
              发布
            </a-button>
            <a-button 
              v-if="record.status === 'PUBLISHED'"
              type="link" 
              @click="handleArchive(record.id)"
            >
              归档
            </a-button>
            <a-popconfirm
              title="确定要删除这份问卷吗？"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
            <a-button type="link" @click="handleViewStatistics(record.id)">统计</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 创建/编辑问卷对话框 -->
    <a-modal
      v-model:visible="showEditModal"
      :title="editForm.id ? '编辑问卷' : '新建问卷'"
      width="800px"
      @ok="handleSave"
    >
      <a-form :model="editForm" ref="formRef" :rules="rules">
        <a-form-item label="标题" name="title">
          <a-input v-model:value="editForm.title" placeholder="请输入问卷标题" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="editForm.description" placeholder="请输入问卷描述" />
        </a-form-item>
        <a-form-item label="及格分" name="passingScore">
          <a-input-number v-model:value="editForm.passingScore" :min="0" :max="100" />
        </a-form-item>
        
        <div v-for="(question, qIndex) in editForm.questions" :key="qIndex" class="question-item">
          <a-card class="question-card" :title="`问题 ${qIndex + 1}`">
            <template #extra>
              <a-button type="link" danger @click="removeQuestion(qIndex)">删除问题</a-button>
            </template>
            <a-form-item :name="['questions', qIndex, 'questionText']" label="问题内容">
              <a-input v-model:value="question.questionText" placeholder="请输入问题内容" />
            </a-form-item>
            <a-form-item :name="['questions', qIndex, 'questionType']" label="问题类型">
              <a-select v-model:value="question.questionType">
                <a-select-option value="SINGLE_CHOICE">单选题</a-select-option>
                <a-select-option value="MULTIPLE_CHOICE">多选题</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item :name="['questions', qIndex, 'score']" label="分值">
              <a-input-number 
                v-model:value="question.score" 
                :min="0" 
                :max="100"
                :controls="true"
                button-style="horizontal"
                style="width: 200px"
                @change="handleQuestionScoreChange(question)"
              />
            </a-form-item>
            
            <div class="options-list">
              <div v-for="(option, oIndex) in question.options" :key="oIndex" class="option-item">
                <a-row :gutter="16">
                  <a-col :span="16">
                    <a-form-item 
                      :name="['questions', qIndex, 'options', oIndex, 'optionText']"
                      :label="`选项 ${oIndex + 1}`"
                    >
                      <a-input v-model:value="option.optionText" placeholder="请输入选项内容" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="6">
                    <a-form-item 
                      :name="['questions', qIndex, 'options', oIndex, 'score']"
                      label="分值"
                    >
                      <a-input-number 
                        v-model:value="option.score" 
                        :min="0" 
                        :max="question.score"
                        :controls="true"
                        button-style="horizontal"
                        style="width: 100%"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="2">
                    <a-button 
                      type="link" 
                      danger 
                      @click="removeOption(question, oIndex)"
                      :disabled="question.options.length <= 2"
                    >
                      删除
                    </a-button>
                  </a-col>
                </a-row>
              </div>
            </div>
            
            <a-button type="dashed" block @click="addOption(question)">
              添加选项
            </a-button>
          </a-card>
        </div>
        
        <a-button type="dashed" block style="margin-top: 16px" @click="addQuestion">
          添加问题
        </a-button>
      </a-form>
    </a-modal>

    <!-- 统计数据对话框 -->
    <a-modal
      v-model:visible="showStatsModal"
      title="问卷统计"
      width="600px"
      :footer="null"
    >
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="问卷标题" :span="2">
          {{ statisticsData?.title }}
        </a-descriptions-item>
        <a-descriptions-item label="完成次数">
          {{ statisticsData?.completedCount }}
        </a-descriptions-item>
        <a-descriptions-item label="独立参与人数">
          {{ statisticsData?.distinctUsersCount }}
        </a-descriptions-item>
        <a-descriptions-item label="平均分">
          {{ statisticsData?.averageScore?.toFixed(1) }}
        </a-descriptions-item>
        <a-descriptions-item label="最高分">
          {{ statisticsData?.highestScore || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="最低分">
          {{ statisticsData?.lowestScore || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="及格次数">
          {{ statisticsData?.passedCount }}
        </a-descriptions-item>
        <a-descriptions-item label="未通过次数">
          {{ statisticsData?.failedCount || 0 }}
        </a-descriptions-item>
        <a-descriptions-item label="及格率">
          {{ (statisticsData?.passRate * 100)?.toFixed(1) }}%
        </a-descriptions-item>
      </a-descriptions>
      
      <!-- 添加统计图表 -->
      <div v-if="statisticsData?.completedCount > 0" style="margin-top: 20px;">
        <h3>得分分布</h3>
        <div style="display: flex; margin-top: 10px;">
          <div style="flex: 1; margin-right: 10px;">
            <div style="display: flex; justify-content: space-between;">
              <span>通过: {{ statisticsData?.passedCount }}</span>
              <span>{{ (statisticsData?.passRate * 100)?.toFixed(1) }}%</span>
            </div>
            <a-progress 
              :percent="(statisticsData?.passRate * 100)" 
              status="success" 
              :show-info="false"
            />
          </div>
          <div style="flex: 1;">
            <div style="display: flex; justify-content: space-between;">
              <span>未通过: {{ statisticsData?.failedCount }}</span>
              <span>{{ ((1 - statisticsData?.passRate) * 100)?.toFixed(1) }}%</span>
            </div>
            <a-progress 
              :percent="((1 - statisticsData?.passRate) * 100)" 
              status="exception" 
              :show-info="false"
            />
          </div>
        </div>
      </div>
      
      <div style="margin-top: 16px; color: rgba(0, 0, 0, 0.45);">
        <p>注：</p>
        <p>- 完成次数：表示该问卷被提交的总次数</p>
        <p>- 独立参与人数：表示参与该问卷测评的不同用户数量</p>
        <p>- 及格率：基于问卷设置的及格分数，统计通过的次数占总参与人次的比例</p>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import { questionnaireApi } from '@/api/questionnaire';
import { assessmentApi } from '@/api/assessment';
import type { 
  Questionnaire, 
  Question,
  CreateQuestionnaireRequest,
  QuestionnaireListRequest 
} from '@/types/questionnaire';
import type { QuestionnaireStatistics } from '@/types/assessment';

// 表格列定义
const columns = [
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
  },
  {
    title: '问题数量',
    key: 'questionCount',
    customRender: ({ record }: { record: Questionnaire }) => record.questions.length,
  },
  {
    title: '总分',
    key: 'totalScore',
    customRender: ({ record }: { record: Questionnaire }) => 
      record.questions.reduce((sum, q) => sum + q.score, 0),
  },
  {
    title: '及格分',
    dataIndex: 'passingScore',
    key: 'passingScore',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: string }) => {
      const statusMap = {
        DRAFT: '草稿',
        PUBLISHED: '已发布',
        ARCHIVED: '已归档'
      };
      return statusMap[text as keyof typeof statusMap] || text;
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    customRender: ({ text }: { text: string }) => {
      if (!text) return '-';
      const date = new Date(text);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 280,
  },
];

// 状态管理
const loading = ref(false);
const dataSource = ref<Questionnaire[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

// 模态框状态
const showEditModal = ref(false);
const showStatsModal = ref(false);
const formRef = ref();
const statisticsData = ref<QuestionnaireStatistics>({
  questionnaireId: 0,
  title: '',
  completedCount: 0,
  distinctUsersCount: 0,
  averageScore: 0,
  passedCount: 0,
  passRate: 0,
});

interface EditingForm extends CreateQuestionnaireRequest {
  id?: number;
}

const editForm = ref<EditingForm>({
  title: '',
  description: '',
  passingScore: 60,
  questions: []
});

const rules = {
  title: [{ required: true, message: '请输入问卷标题' }],
  description: [{ required: true, message: '请输入问卷描述' }],
  passingScore: [{ required: true, message: '请输入及格分数' }],
};

const searchParams = ref<QuestionnaireListRequest>({
  keyword: '',
  statuses: undefined,
  page: 1,
  size: 10
});

const fetchQuestionnaires = async () => {
  try {
    loading.value = true;
    
    // 确保页码是从0开始的，并正确构建参数
    const params = {
      keyword: searchParams.value.keyword,
      statuses: searchParams.value.statuses,
      page: (searchParams.value.page || 1) - 1,  // 后端分页从0开始
      size: searchParams.value.size
    };
    
    console.log('请求参数:', JSON.stringify(params));
    
    const response = await questionnaireApi.getQuestionnaires(params);
    console.log('获取结果:', response);
    
    dataSource.value = response.content;
    pagination.value.total = response.totalElements;
  } catch (error) {
    console.error('获取问卷列表失败:', error);
    message.error('获取问卷列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  searchParams.value.page = 1;
  
  // 不要尝试转换，直接使用状态信息
  console.log('发送请求前的参数:', JSON.stringify(searchParams.value));
  
  fetchQuestionnaires();
};

const handleTableChange = (pag: TablePaginationConfig) => {
  searchParams.value.page = pag.current || 1;
  searchParams.value.size = pag.pageSize || 10;
  fetchQuestionnaires();
};

// 状态相关方法
const getStatusColor = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return 'blue';
    case 'PUBLISHED':
      return 'green';
    case 'ARCHIVED':
      return 'gray';
    default:
      return 'default';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'DRAFT':
      return '草稿';
    case 'PUBLISHED':
      return '已发布';
    case 'ARCHIVED':
      return '已归档';
    default:
      return '未知';
  }
};

// 问卷操作方法
const handleAdd = () => {
  resetForm();
  showEditModal.value = true;
};

const handleEdit = (record: Questionnaire) => {
  const { id, title, description, passingScore, questions } = record;
  editForm.value = {
    id,
    title,
    description,
    passingScore,
    questions: questions.map(q => ({
      questionText: q.questionText,
      questionType: q.questionType,
      score: q.score,
      sortOrder: q.sortOrder,
      options: q.options.map(o => ({
        optionText: o.optionText,
        score: o.score,
        sortOrder: o.sortOrder
      }))
    }))
  };
  showEditModal.value = true;
};

const handlePassingScoreChange = (value: number | undefined) => {
  if (value !== undefined) {
    editForm.value.passingScore = value;
  }
};

const handleSave = async () => {
  try {
    await formRef.value?.validate();
    
    // 验证问题和选项数据
    const hasQuestions = editForm.value.questions.length > 0;
    if (!hasQuestions) {
      message.error('请至少添加一个问题');
      return;
    }
    
    // 检查每个问题是否设置了分值
    const hasInvalidQuestions = editForm.value.questions.some(q => !q.questionText || q.score <= 0);
    if (hasInvalidQuestions) {
      message.error('请确保每个问题都有内容和分值');
      return;
    }
    
    // 检查选项
    const hasInvalidOptions = editForm.value.questions.some(q => 
      q.options.some(o => !o.optionText) || // 检查选项内容
      q.options.length < 2 // 确保至少有两个选项
    );
    if (hasInvalidOptions) {
      message.error('请确保每个问题至少有两个选项，且每个选项都有内容');
      return;
    }
    
    const { id, ...data } = editForm.value;
    if (id) {
      await questionnaireApi.updateQuestionnaire(id, data);
      message.success('更新问卷成功');
    } else {
      await questionnaireApi.createQuestionnaire(data);
      message.success('创建问卷成功');
    }
    showEditModal.value = false;
    fetchQuestionnaires();
  } catch (error) {
    message.error('保存失败，请检查表单');
  }
};

const handleDelete = async (id: number) => {
  try {
    await questionnaireApi.deleteQuestionnaire(id);
    message.success('删除成功');
    fetchQuestionnaires();
  } catch (error) {
    message.error('删除失败');
  }
};

const handleArchive = async (id: number) => {
  try {
    await questionnaireApi.archiveQuestionnaire(id);
    message.success('归档成功');
    fetchQuestionnaires();
  } catch (error) {
    message.error('归档失败');
  }
};

const handleViewStatistics = async (id: number) => {
  try {
    const stats = await assessmentApi.getQuestionnaireStatistics(id);
    statisticsData.value = stats;
    showStatsModal.value = true;
  } catch (error) {
    message.error('获取统计数据失败');
  }
};

const handlePublish = async (id: number) => {
  try {
    await questionnaireApi.publishQuestionnaire(id);
    message.success('发布成功');
    fetchQuestionnaires();
  } catch (error) {
    message.error('发布失败');
  }
};

// 题目操作方法
const addQuestion = () => {
  editForm.value.questions.push({
    questionText: '',
    questionType: 'SINGLE_CHOICE',
    score: 10,
    sortOrder: editForm.value.questions.length + 1,
    options: [
      { optionText: '', score: 0, sortOrder: 1 },
      { optionText: '', score: 0, sortOrder: 2 }
    ]
  });
};

const removeQuestion = (index: number) => {
  editForm.value.questions.splice(index, 1);
  // 更新剩余问题的排序
  editForm.value.questions.forEach((q, idx) => {
    q.sortOrder = idx + 1;
  });
};

const addOption = (question: Question) => {
  question.options.push({
    optionText: '',
    score: 0,
    sortOrder: question.options.length + 1
  });
};

const removeOption = (question: Question, index: number) => {
  question.options.splice(index, 1);
  // 更新剩余选项的排序
  question.options.forEach((opt, idx) => {
    opt.sortOrder = idx + 1;
  });
};

const hasOtherCorrectOption = (question: Question, currentIndex: number): boolean => {
  return question.options.some((opt, idx) => idx !== currentIndex && opt.score > 0);
};

const resetForm = () => {
  editForm.value = {
    title: '',
    description: '',
    passingScore: 60,
    questions: []
  };
};

// 添加状态变更处理方法
const handleStatusChange = (value: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | undefined) => {
  if (value) {
    searchParams.value.statuses = [value]; // 修改为数组格式
  } else {
    searchParams.value.statuses = undefined;
  }
  console.log('状态变更后的参数:', JSON.stringify(searchParams.value));
};

// 处理问题分值变更，确保选项分值不超过问题分值
const handleQuestionScoreChange = (question: Question) => {
  // 如果问题分值小于任何选项分值，将选项分值设置为问题分值
  question.options.forEach(option => {
    if (option.score > question.score) {
      option.score = question.score;
    }
  });
};

// 初始化
fetchQuestionnaires();
</script>

<style scoped>
.questionnaire-management {
  padding: 24px;
}

.search-bar {
  margin-bottom: 16px;
}

.question-item {
  margin-bottom: 16px;
}

.question-card {
  background: #fafafa;
}

.options-list {
  margin-bottom: 16px;
}

.option-item {
  margin-bottom: 8px;
}

/* 修复数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-input-number-handler-wrap) {
  width: 22px;
}

:deep(.ant-form-item-has-error .ant-input-number-handler-wrap) {
  background-color: #fff;
  border-color: #ff4d4f;
}
</style> 