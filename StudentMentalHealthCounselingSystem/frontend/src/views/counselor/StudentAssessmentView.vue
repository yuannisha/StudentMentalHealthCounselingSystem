<template>
  <div class="student-assessment-view">
    <a-card title="学生测评记录查询">
      <a-form layout="inline" class="search-form">
        <a-form-item label="学生">
          <a-select
            v-model:value="searchParams.studentId"
            placeholder="请选择学生"
            :loading="loadingStudents"
            style="width: 200px"
            show-search
            :filter-option="filterOption"
          >
            <a-select-option v-for="student in students" :key="student.id" :value="student.id">
              {{ student.realName }} ({{ student.username }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="fetchAssessmentHistory" :loading="loading">
            查询
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card v-if="selectedStudent" title="学生测评历史" style="margin-top: 16px">
      <!-- 学生基本信息显示 -->
      <div class="student-info" v-if="selectedStudent">
        <a-descriptions title="学生信息" bordered :column="2">
          <a-descriptions-item label="姓名">{{ selectedStudent.realName }}</a-descriptions-item>
          <a-descriptions-item label="学号">{{ selectedStudent.studentId }}</a-descriptions-item>
          <a-descriptions-item label="专业">{{ selectedStudent.major }}</a-descriptions-item>
          <a-descriptions-item label="学院">{{ selectedStudent.college }}</a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 测评历史列表 -->
      <a-table
        :loading="loading"
        :data-source="assessmentHistory"
        :columns="columns"
        :pagination="pagination"
        @change="handleTableChange"
        style="margin-top: 16px"
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
          <a-button type="link" @click="viewAssessmentDetail(record)">
            查看详情
          </a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 测评详情对话框 -->
    <a-modal
      v-model:visible="detailVisible"
      title="测评详情"
      width="800px"
      :footer="null"
    >
      <div v-if="selectedAssessment">
        <a-result
          :status="selectedAssessment.passed ? 'success' : 'warning'"
          :title="selectedAssessment.passed ? '测评通过' : '测评未通过'"
          :sub-title="`总分：${selectedAssessment.totalScore} / ${selectedAssessment.questionnaire.totalScore}`"
        >
          <div class="result-evaluation">
            <p class="result-summary">
              {{ getResultSummary(selectedAssessment) }}
            </p>
            <div class="score-progress">
              <a-progress 
                :percent="selectedAssessment?.totalScore && selectedAssessment?.questionnaire?.totalScore ? 
                  (selectedAssessment.totalScore / selectedAssessment.questionnaire.totalScore * 100) : 0" 
                :format="(percent: number) => selectedAssessment ? `${selectedAssessment.totalScore}分` : '0分'"
                :status="selectedAssessment?.passed ? 'success' : 'exception'"
              />
            </div>
            <p class="result-advice">
              {{ getResultAdvice(selectedAssessment) }}
            </p>
          </div>
        </a-result>

        <a-divider />

        <!-- 问卷信息 -->
        <a-card title="问卷信息" class="questionnaire-info">
          <a-descriptions :column="2">
            <a-descriptions-item label="问卷标题">
              {{ selectedAssessment.questionnaire.title }}
            </a-descriptions-item>
            <a-descriptions-item label="及格分">
              {{ selectedAssessment.questionnaire.passingScore || '无' }}
            </a-descriptions-item>
            <a-descriptions-item label="完成时间">
              {{ formatDateTime(selectedAssessment.createTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="问卷状态">
              <a-tag :color="getStatusColor(selectedAssessment.questionnaire.status)">
                {{ getStatusText(selectedAssessment.questionnaire.status) }}
              </a-tag>
            </a-descriptions-item>
          </a-descriptions>
          <p v-if="selectedAssessment.questionnaire.description" class="description">
            {{ selectedAssessment.questionnaire.description }}
          </p>
        </a-card>

        <!-- 答题详情 -->
        <a-card title="答题详情" class="answer-details" style="margin-top: 16px">
          <div v-for="(answer, index) in selectedAssessment.answers" :key="index" class="question-result">
            <a-card
              :class="['question-card', { 'correct': isAnswerCorrect(answer, selectedAssessment) }]"
              size="small"
              style="margin-bottom: 12px"
            >
              <template #title>
                <span>
                  第 {{ index + 1 }} 题
                  <a-tag :color="isAnswerCorrect(answer, selectedAssessment) ? 'success' : 'error'">
                    得分：{{ answer.score }}
                  </a-tag>
                </span>
              </template>

              <div class="question-content">
                <p class="question-text">
                  {{ getQuestionText(answer.questionId, selectedAssessment) }}
                </p>

                <div class="options">
                  <div
                    v-for="option in getQuestionOptions(answer.questionId, selectedAssessment)"
                    :key="option.id"
                    :class="[
                      'option',
                      {
                        'selected': isOptionSelected(option.id, answer.selectedOptions),
                        'correct': isOptionCorrect(option.id, answer.correctOptions)
                      }
                    ]"
                  >
                    <span class="option-text">{{ option.optionText }}</span>
                    <span class="option-score">({{ option.score }}分)</span>
                  </div>
                </div>
              </div>
            </a-card>
          </div>
        </a-card>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import type { AssessmentResultResponse, Question, QuestionOption } from '@/types/assessment';
import type { User } from '@/types/user';
import { assessmentApi } from '@/api/assessment';
import { userApi } from '@/api/user';
import { message } from 'ant-design-vue';

// 状态定义
const loading = ref(false);
const loadingStudents = ref(false);
const students = ref<User[]>([]);
const assessmentHistory = ref<AssessmentResultResponse[]>([]);
const selectedStudent = ref<User | null>(null);
const detailVisible = ref(false);
const selectedAssessment = ref<AssessmentResultResponse | null>(null);

// 搜索参数
const searchParams = ref({
  studentId: undefined as number | undefined,
});

// 分页配置
const pagination = ref<TablePaginationConfig>({
  total: 0,
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 表格列定义
const columns = [
  {
    title: '问卷标题',
    dataIndex: ['questionnaire'],
    slots: { customRender: 'questionnaire' },
    width: '25%'
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
    width: '10%'
  },
  {
    title: '问卷状态',
    dataIndex: ['questionnaire', 'status'],
    slots: { customRender: 'questionnaireStatus' },
    width: '10%'
  },
  {
    title: '完成时间',
    dataIndex: 'createTime',
    slots: { customRender: 'createTime' },
    width: '20%'
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: '10%'
  }
];

// 加载学生列表
const loadStudents = async () => {
  loadingStudents.value = true;
  try {
    // 获取学生用户
    const response = await userApi.getStudents({
      page: 0,
      size: 100,
      keyword: ''
    });
    // 直接使用返回的学生列表
    students.value = response.content;
    console.log('已加载学生列表:', students.value.length);
  } catch (error) {
    console.error('加载学生列表失败:', error);
    message.error('加载学生列表失败');
  } finally {
    loadingStudents.value = false;
  }
};

// 加载学生测评历史
const fetchAssessmentHistory = async () => {
  loading.value = true;
  assessmentHistory.value = [];

  try {
    // 确保studentId是有效的数字
    if (!searchParams.value.studentId) {
      message.warning('请先选择学生');
      loading.value = false;
      return;
    }

    const studentId = ensureNumber(searchParams.value.studentId);
    
    // 设置选中的学生信息
    selectedStudent.value = students.value.find(s => s.id === studentId) || null;
    
    console.log('获取学生测评历史，学生ID:', studentId);
    
    const { current = 1, pageSize = 10 } = pagination.value;
    const response = await assessmentApi.getUserHistory(studentId, {
      page: current - 1,
      size: pageSize
    });
    
    assessmentHistory.value = response.content;
    pagination.value.total = response.totalElements;
    
    console.log('学生测评历史:', assessmentHistory.value);
    
    // 调试输出每个问卷的状态
    if (assessmentHistory.value && assessmentHistory.value.length > 0) {
      console.log("问卷状态信息：", assessmentHistory.value.map(item => ({
        id: item.id,
        questionnaire_id: item.questionnaire.id,
        title: item.questionnaire.title,
        status: item.questionnaire.status
      })));
    }
  } catch (error) {
    console.error('获取学生测评历史失败:', error);
    message.error('获取学生测评历史失败');
  } finally {
    loading.value = false;
  }
};

// 查看测评详情
const viewAssessmentDetail = (assessment: AssessmentResultResponse) => {
  selectedAssessment.value = assessment;
  detailVisible.value = true;
  
  // 记录查看详情操作，包括问卷状态
  console.log('查看测评详情:', {
    id: assessment.id,
    status: assessment.questionnaire.status,
    title: assessment.questionnaire.title
  });
};

// 处理表格分页变化
const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  fetchAssessmentHistory();
};

/**
 * 确保值为number类型，如果是undefined则返回默认值
 */
const ensureNumber = (value: number | undefined, defaultValue: number = 0): number => {
  return (value === undefined) ? defaultValue : value;
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

// 下拉框过滤功能
const filterOption = (input: string, option: any) => {
  return (
    option.children[0].toLowerCase().indexOf(input.toLowerCase()) >= 0 ||
    option.children[2].toLowerCase().indexOf(input.toLowerCase()) >= 0
  );
};

// 获取测评结果摘要
const getResultSummary = (assessment: AssessmentResultResponse) => {
  if (assessment.passed) {
    return '测评通过，学生心理状态良好';
  } else {
    return '测评未通过，学生可能需要心理辅导';
  }
};

// 获取测评结果建议
const getResultAdvice = (assessment: AssessmentResultResponse) => {
  if (assessment.passed) {
    return '建议定期关注学生心理状态，保持良好的沟通';
  } else {
    return '建议安排面谈咨询，评估学生心理状况，制定相应的辅导计划';
  }
};

// 获取题目文本
const getQuestionText = (questionId: number, assessment: AssessmentResultResponse): string => {
  const question = assessment.questionnaire.questions.find((q: Question) => q.id === questionId);
  return question?.questionText || '';
};

// 获取题目选项
const getQuestionOptions = (questionId: number, assessment: AssessmentResultResponse): QuestionOption[] => {
  const question = assessment.questionnaire.questions.find((q: Question) => q.id === questionId);
  return question?.options || [];
};

/**
 * 判断选项是否被选中
 */
const isOptionSelected = (optionId: number | undefined, selectedOptions: number[] | undefined): boolean => {
  if (!optionId || !selectedOptions) return false;
  return selectedOptions.includes(optionId);
};

/**
 * 判断选项是否正确
 */
const isOptionCorrect = (optionId: number | undefined, correctOptions: number[] | undefined): boolean => {
  if (!optionId || !correctOptions) return false;
  return correctOptions.includes(optionId);
};

// 判断答案是否完全正确
const isAnswerCorrect = (answer: { score: number; questionId: number }, assessment: AssessmentResultResponse): boolean => {
  const question = assessment.questionnaire.questions.find((q: Question) => q.id === answer.questionId);
  return question ? answer.score === question.score : false;
};

onMounted(() => {
  // 组件加载时初始化学生列表
  loadStudents();
  
  // 重置搜索参数和分页
  searchParams.value.studentId = undefined;
  assessmentHistory.value = [];
  pagination.value.current = 1;
  pagination.value.total = 0;
  
  console.log('学生测评视图组件已加载');
});
</script>

<style scoped>
.student-assessment-view {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}

.student-info {
  margin-bottom: 24px;
}

.question-card {
  margin-bottom: 8px;
  border: 1px solid #f0f0f0;
}

.question-card.correct {
  border-left: 4px solid #52c41a;
}

.question-text {
  font-weight: 500;
  margin-bottom: 12px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option {
  padding: 8px 12px;
  border-radius: 4px;
  background-color: #f5f5f5;
  display: flex;
  justify-content: space-between;
}

.option.selected {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.option.correct {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}

.option.selected.correct {
  background-color: #f6ffed;
  border: 1px solid #52c41a;
}

.option-score {
  color: #999;
}

.result-evaluation {
  margin: 16px 0;
  text-align: center;
}

.result-summary {
  font-size: 16px;
  margin-bottom: 16px;
}

.score-progress {
  margin: 24px 0;
}

.result-advice {
  color: #666;
  margin-top: 16px;
}

.description {
  color: rgba(0, 0, 0, 0.65);
  margin-top: 16px;
}
</style> 