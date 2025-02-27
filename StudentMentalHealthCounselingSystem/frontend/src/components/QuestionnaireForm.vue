<template>
  <a-form
    ref="formRef"
    :model="formState"
    :rules="rules"
    layout="vertical"
    @finish="handleFinish"
  >
    <!-- 基本信息 -->
    <a-form-item label="问卷标题" name="title">
      <a-input v-model:value="formState.title" placeholder="请输入问卷标题" />
    </a-form-item>

    <a-form-item label="问卷描述" name="description">
      <a-textarea
        v-model:value="formState.description"
        placeholder="请输入问卷描述"
        :rows="4"
      />
    </a-form-item>

    <a-form-item label="及格分数" name="passingScore">
      <a-input-number
        v-model:value="formState.passingScore"
        placeholder="请输入及格分数"
        :min="0"
        style="width: 200px"
      />
    </a-form-item>

    <!-- 题目列表 -->
    <a-form-item label="题目列表" required>
      <a-space direction="vertical" style="width: 100%">
        <a-card
          v-for="(question, questionIndex) in formState.questions"
          :key="questionIndex"
          size="small"
          :title="`题目 ${questionIndex + 1}`"
        >
          <template #extra>
            <a-space>
              <a-button
                type="link"
                size="small"
                @click="() => moveQuestion(questionIndex, -1)"
                :disabled="questionIndex === 0"
              >
                上移
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="() => moveQuestion(questionIndex, 1)"
                :disabled="questionIndex === formState.questions.length - 1"
              >
                下移
              </a-button>
              <a-button
                type="link"
                danger
                size="small"
                @click="() => removeQuestion(questionIndex)"
              >
                删除
              </a-button>
            </a-space>
          </template>

          <!-- 题目内容 -->
          <a-form-item
            :name="['questions', questionIndex, 'questionText']"
            label="题目内容"
            required
          >
            <a-input
              v-model:value="question.questionText"
              placeholder="请输入题目内容"
            />
          </a-form-item>

          <!-- 题目类型 -->
          <a-form-item
            :name="['questions', questionIndex, 'questionType']"
            label="题目类型"
            required
          >
            <a-select
              v-model:value="question.questionType"
              placeholder="请选择题目类型"
            >
              <a-select-option value="SINGLE_CHOICE">单选题</a-select-option>
              <a-select-option value="MULTIPLE_CHOICE">多选题</a-select-option>
            </a-select>
          </a-form-item>

          <!-- 题目分数 -->
          <a-form-item
            :name="['questions', questionIndex, 'score']"
            label="题目分数"
            required
          >
            <a-input-number
              v-model:value="question.score"
              placeholder="请输入题目分数"
              :min="0"
              style="width: 200px"
            />
          </a-form-item>

          <!-- 选项列表 -->
          <a-form-item label="选项列表" required>
            <a-space direction="vertical" style="width: 100%">
              <a-row
                v-for="(option, optionIndex) in question.options"
                :key="optionIndex"
                :gutter="16"
                align="middle"
              >
                <a-col :span="12">
                  <a-form-item
                    :name="['questions', questionIndex, 'options', optionIndex, 'optionText']"
                    required
                    :validate-trigger="['change', 'blur']"
                  >
                    <a-input
                      v-model:value="option.optionText"
                      placeholder="请输入选项内容"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item
                    :name="['questions', questionIndex, 'options', optionIndex, 'score']"
                    required
                    :validate-trigger="['change', 'blur']"
                  >
                    <a-input-number
                      v-model:value="option.score"
                      placeholder="选项分数"
                      :min="0"
                      style="width: 100%"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="4">
                  <a-space>
                    <a-button
                      type="link"
                      size="small"
                      @click="() => moveOption(questionIndex, optionIndex, -1)"
                      :disabled="optionIndex === 0"
                    >
                      上移
                    </a-button>
                    <a-button
                      type="link"
                      size="small"
                      @click="() => moveOption(questionIndex, optionIndex, 1)"
                      :disabled="optionIndex === question.options.length - 1"
                    >
                      下移
                    </a-button>
                    <a-button
                      type="link"
                      danger
                      size="small"
                      @click="() => removeOption(questionIndex, optionIndex)"
                      :disabled="question.options.length <= 2"
                    >
                      删除
                    </a-button>
                  </a-space>
                </a-col>
              </a-row>

              <!-- 添加选项按钮 -->
              <a-button
                type="dashed"
                block
                @click="() => addOption(questionIndex)"
              >
                <template #icon><plus-outlined /></template>
                添加选项
              </a-button>
            </a-space>
          </a-form-item>
        </a-card>

        <!-- 添加题目按钮 -->
        <a-button type="dashed" block @click="addQuestion">
          <template #icon><plus-outlined /></template>
          添加题目
        </a-button>
      </a-space>
    </a-form-item>

    <!-- 表单按钮 -->
    <a-form-item>
      <a-space>
        <a-button type="primary" html-type="submit">保存</a-button>
        <a-button @click="$emit('cancel')">取消</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import type { FormInstance } from 'ant-design-vue';
import type {
  QuestionnaireResponse,
  QuestionnaireRequest,
  Question,
  QuestionOption,
} from '@/types/assessment';

const props = defineProps<{
  questionnaire?: QuestionnaireResponse | null;
}>();

const emit = defineEmits<{
  (e: 'submit', data: QuestionnaireRequest): void;
  (e: 'cancel'): void;
}>();

// 表单实例
const formRef = ref<FormInstance>();

// 表单状态
const formState = reactive<QuestionnaireRequest>({
  title: '',
  description: '',
  passingScore: undefined,
  questions: [],
});

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入问卷标题' }],
  questions: [{ required: true, type: 'array', min: 1, message: '至少添加一个题目' }],
};

// 监听问卷数据变化
watch(
  () => props.questionnaire,
  (questionnaire) => {
    if (questionnaire) {
      formState.title = questionnaire.title;
      formState.description = questionnaire.description;
      formState.passingScore = questionnaire.passingScore;
      formState.questions = questionnaire.questions.map(q => ({
        ...q,
        options: [...q.options],
      }));
    } else {
      formState.title = '';
      formState.description = '';
      formState.passingScore = undefined;
      formState.questions = [];
    }
  },
  { immediate: true }
);

// 创建新题目
const createQuestion = (): Question => ({
  questionText: '',
  questionType: 'SINGLE_CHOICE',
  score: 0,
  options: [
    { optionText: '', score: 0 },
    { optionText: '', score: 0 },
  ],
});

// 创建新选项
const createOption = (): QuestionOption => ({
  optionText: '',
  score: 0,
});

// 添加题目
const addQuestion = () => {
  formState.questions.push(createQuestion());
};

// 移动题目
const moveQuestion = (index: number, direction: number) => {
  const newIndex = index + direction;
  if (newIndex >= 0 && newIndex < formState.questions.length) {
    const questions = [...formState.questions];
    [questions[index], questions[newIndex]] = [questions[newIndex], questions[index]];
    formState.questions = questions;
  }
};

// 删除题目
const removeQuestion = (index: number) => {
  formState.questions.splice(index, 1);
};

// 添加选项
const addOption = (questionIndex: number) => {
  formState.questions[questionIndex].options.push(createOption());
};

// 移动选项
const moveOption = (questionIndex: number, optionIndex: number, direction: number) => {
  const options = formState.questions[questionIndex].options;
  const newIndex = optionIndex + direction;
  if (newIndex >= 0 && newIndex < options.length) {
    [options[optionIndex], options[newIndex]] = [options[newIndex], options[optionIndex]];
  }
};

// 删除选项
const removeOption = (questionIndex: number, optionIndex: number) => {
  formState.questions[questionIndex].options.splice(optionIndex, 1);
};

// 提交表单
const handleFinish = (values: QuestionnaireRequest) => {
  // 更新题目和选项的排序
  const data = {
    ...values,
    questions: values.questions.map((question, qIndex) => ({
      ...question,
      sortOrder: qIndex + 1,
      options: question.options.map((option, oIndex) => ({
        ...option,
        sortOrder: oIndex + 1,
      })),
    })),
  };
  emit('submit', data);
};
</script>

<style scoped>
.ant-form-item {
  margin-bottom: 16px;
}

.ant-card {
  margin-bottom: 16px;
}
</style> 