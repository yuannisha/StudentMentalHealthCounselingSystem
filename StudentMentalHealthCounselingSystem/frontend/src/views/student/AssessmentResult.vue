<template>
  <div class="assessment-result">
    <a-spin :spinning="loading">
      <template v-if="result">
        <!-- 结果概览 -->
        <a-card class="result-overview">
          <a-result
            :status="result.passed ? 'success' : 'warning'"
            :title="result.passed ? '测评通过' : '测评未通过'"
            :sub-title="`总分：${result.totalScore} / ${result.questionnaire.totalScore}`"
          >
            <!-- 增加测评结果评价 -->
            <div class="result-evaluation">
              <p class="result-summary">
                {{ getResultSummary() }}
              </p>
              <div class="score-progress">
                <a-progress 
                  :percent="result?.totalScore && result?.questionnaire?.totalScore ? 
                    (result.totalScore / result.questionnaire.totalScore * 100) : 0" 
                  :format="(percent: number) => result ? `${result.totalScore}分` : '0分'"
                  :status="result?.passed ? 'success' : 'exception'"
                />
              </div>
              <p class="result-advice">
                {{ getResultAdvice() }}
              </p>
            </div>
            
            <template #extra>
              <a-space>
                <a-button type="primary" @click="$router.push('/student/assessments')">
                  返回列表
                </a-button>
                <a-button 
                  v-if="result.questionnaire.status !== 'ARCHIVED'"
                  @click="$router.push(`/student/assessment/${result.questionnaire.id}`)"
                >
                  重新测评
                </a-button>
                <a-tag v-else color="default">已归档</a-tag>
              </a-space>
            </template>
          </a-result>
        </a-card>

        <!-- 问卷信息 -->
        <a-card title="问卷信息" class="questionnaire-info">
          <a-descriptions :column="2">
            <a-descriptions-item label="问卷标题">
              {{ result.questionnaire.title }}
            </a-descriptions-item>
            <a-descriptions-item label="及格分">
              {{ result.questionnaire.passingScore || '无' }}
            </a-descriptions-item>
            <a-descriptions-item label="完成时间">
              {{ formatDateTime(result.createTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="题目数量">
              {{ result.questionnaire.questions.length }}
            </a-descriptions-item>
          </a-descriptions>
          <p v-if="result.questionnaire.description" class="description">
            {{ result.questionnaire.description }}
          </p>
        </a-card>

        <!-- 答题详情 -->
        <a-card title="答题详情" class="answer-details">
          <div v-for="(answer, index) in result.answers" :key="index" class="question-result">
            <a-card
              :class="['question-card', { 'correct': isAnswerCorrect(answer) }]"
              size="small"
            >
              <template #title>
                <span>
                  第 {{ index + 1 }} 题
                  <a-tag :color="isAnswerCorrect(answer) ? 'success' : 'error'">
                    得分：{{ answer.score }}
                  </a-tag>
                </span>
              </template>

              <div class="question-content">
                <p class="question-text">
                  {{ getQuestionText(answer.questionId) }}
                </p>

                <div class="options">
                  <div
                    v-for="option in getQuestionOptions(answer.questionId)"
                    :key="option.id || 0"
                    :class="[
                      'option',
                      {
                        'selected': option.id && isOptionSelected(option.id, answer.selectedOptions),
                        'correct': option.id && isOptionCorrect(option.id, answer.correctOptions)
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
      </template>

      <!-- 错误提示 -->
      <a-result
        v-else-if="error"
        status="error"
        :title="error"
        :sub-title="'无法加载测评结果'"
      >
        <template #extra>
          <a-button type="primary" @click="$router.push('/student/assessments')">
            返回列表
          </a-button>
        </template>
      </a-result>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import type { AssessmentResultResponse, Question, QuestionOption } from '@/types/assessment';
import { assessmentApi } from '@/api/assessment';
import { message } from 'ant-design-vue';

const route = useRoute();
const loading = ref(false);
const error = ref('');
const result = ref<AssessmentResultResponse | null>(null);

// 加载测评结果
const loadResult = async () => {
  loading.value = true;
  error.value = '';
  try {
    const id = Number(route.params.id);
    if (isNaN(id)) {
      error.value = '结果ID无效';
      message.error('结果ID无效');
      return;
    }
    
    result.value = await assessmentApi.getResult(id);
    console.log("加载的测评结果:", result.value);
    
    // 检查问卷状态并记录
    if (result.value?.questionnaire?.status) {
      console.log(`问卷状态: ${result.value.questionnaire.status}，ID: ${result.value.questionnaire.id}，标题: ${result.value.questionnaire.title}`);
      
      // 如果问卷已归档，显示提示
      if (result.value.questionnaire.status === 'ARCHIVED') {
        console.warn(`检测到已归档问卷: ID=${result.value.questionnaire.id}, title=${result.value.questionnaire.title}`);
        message.warning('此问卷已归档，无法重新测评');
      }
    }
  } catch (err: any) {
    console.error('加载测评结果失败:', err);
    error.value = err.response?.data?.message || '加载测评结果失败';
    message.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 获取题目文本
const getQuestionText = (questionId: number): string => {
  const question = result.value?.questionnaire.questions.find((q: Question) => q.id === questionId);
  return question?.questionText || '';
};

// 获取题目选项
const getQuestionOptions = (questionId: number): QuestionOption[] => {
  const question = result.value?.questionnaire.questions.find((q: Question) => q.id === questionId);
  return question?.options || [];
};

// 判断选项是否被选中
const isOptionSelected = (optionId: number, selectedOptions: number[]): boolean => {
  return selectedOptions.includes(optionId);
};

// 判断选项是否正确
const isOptionCorrect = (optionId: number, correctOptions: number[]): boolean => {
  return correctOptions.includes(optionId);
};

// 判断答案是否完全正确
const isAnswerCorrect = (answer: { score: number; questionId: number }): boolean => {
  const question = result.value?.questionnaire.questions.find((q: Question) => q.id === answer.questionId);
  return question ? answer.score === question.score : false;
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

// 获取测评结果摘要
const getResultSummary = () => {
  if (result.value) {
    if (result.value.passed) {
      return '恭喜你，测评通过！';
    } else {
      return '很遗憾，测评未通过。';
    }
  }
  return '';
};

// 获取测评结果建议
const getResultAdvice = () => {
  if (result.value) {
    if (result.value.passed) {
      return '继续保持良好的学习习惯，为未来的学习打下坚实的基础。';
    } else {
      return '请认真分析测评结果，找出不足之处，并制定相应的改进计划。';
    }
  }
  return '';
};

onMounted(() => {
  loadResult();
});
</script>

<style scoped>
.assessment-result {
  padding: 24px;
}

.result-overview,
.questionnaire-info,
.answer-details {
  margin-bottom: 24px;
}

.description {
  color: rgba(0, 0, 0, 0.45);
  margin-top: 16px;
}

.question-result {
  margin-bottom: 16px;
}

.question-card {
  border: 1px solid #f0f0f0;
}

.question-card.correct {
  border-color: #52c41a;
}

.question-content {
  padding: 8px 0;
}

.question-text {
  font-weight: 500;
  margin-bottom: 16px;
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
  align-items: center;
}

.option.selected {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.option.correct {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}

.option-score {
  color: rgba(0, 0, 0, 0.45);
}

.result-evaluation {
  margin-bottom: 16px;
}

.result-summary {
  font-weight: 500;
  margin-bottom: 8px;
}

.score-progress {
  margin-bottom: 8px;
}

.result-advice {
  color: rgba(0, 0, 0, 0.45);
}
</style> 