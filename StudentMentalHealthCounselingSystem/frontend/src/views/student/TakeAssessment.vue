<template>
  <div class="take-assessment">
    <a-spin :spinning="loading">
      <a-card v-if="questionnaire">
        <!-- 问卷标题和描述 -->
        <template #title>
          <div class="questionnaire-header">
            <h2>{{ questionnaire.title }}</h2>
            <p v-if="questionnaire.description" class="description">
              {{ questionnaire.description }}
            </p>
            <div class="questionnaire-info">
              <span>总分：{{ questionnaire.totalScore }}</span>
              <a-divider type="vertical" />
              <span>及格分：{{ questionnaire.passingScore || '无' }}</span>
            </div>
          </div>
        </template>

        <!-- 答题区域 -->
        <a-form
          ref="formRef"
          :model="formState"
          @finish="handleSubmit"
          @change="handleFormChange"
          layout="vertical"
        >
          <div v-for="(question, index) in questionnaire.questions" :key="question.id" class="question-item">
            <a-card :title="`${index + 1}. ${question.questionText}`" size="small">
              <a-form-item
                :name="['answers', index]"
                :rules="[{ required: true, message: '请选择答案' }]"
              >
                <a-radio-group
                  v-if="question.questionType === 'SINGLE_CHOICE'"
                  v-model:value="formState.answers[index]"
                >
                  <a-space direction="vertical">
                    <a-radio
                      v-for="option in question.options"
                      :key="option.id"
                      :value="option.id"
                    >
                      {{ option.optionText }}
                    </a-radio>
                  </a-space>
                </a-radio-group>

                <a-checkbox-group
                  v-else
                  v-model:value="formState.answers[index]"
                >
                  <a-space direction="vertical">
                    <a-checkbox
                      v-for="option in question.options"
                      :key="option.id"
                      :value="option.id"
                    >
                      {{ option.optionText }}
                    </a-checkbox>
                  </a-space>
                </a-checkbox-group>
              </a-form-item>
            </a-card>
          </div>

          <div class="form-actions">
            <a-space>
              <a-button @click="$router.push('/student/assessments')">返回列表</a-button>
              <a-button type="primary" html-type="submit" :loading="submitting">提交答案</a-button>
            </a-space>
          </div>
        </a-form>
      </a-card>
      
      <!-- 显示错误信息 -->
      <a-result
        v-else-if="error"
        status="warning"
        :title="error"
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
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import type { FormInstance } from 'ant-design-vue';
import { questionnaireApi } from '@/api/questionnaire';
import { assessmentApi } from '@/api/assessment';
import type { Questionnaire } from '@/types/questionnaire';
import type { AssessmentSubmitRequest } from '@/types/assessment';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(true);
const submitting = ref(false);
const error = ref('');
const questionnaire = ref<Questionnaire | null>(null);

// 表单状态
const formState = reactive({
  answers: [] as (number | number[])[]
});

// 检查本地存储中是否有保存的答案
const checkSavedAnswers = () => {
  if (!questionnaire.value) return;
  
  const savedAnswers = localStorage.getItem(`assessment_answers_${questionnaire.value.id}`);
  if (savedAnswers) {
    try {
      const parsedAnswers = JSON.parse(savedAnswers);
      
      // 检查答案是否有效
      const hasValidAnswers = parsedAnswers && Array.isArray(parsedAnswers) && 
                              parsedAnswers.some(ans => ans !== undefined);
      
      if (hasValidAnswers) {
        // 弹出提示框确认是否恢复
        const confirm = window.confirm('检测到您上次未完成的答案，是否恢复？');
        if (confirm) {
          formState.answers = parsedAnswers;
          message.success('已恢复您之前保存的答案');
          console.log('已恢复保存的答案:', parsedAnswers);
        } else {
          // 用户选择不恢复，清除保存的答案
          localStorage.removeItem(`assessment_answers_${questionnaire.value.id}`);
        }
      }
    } catch (e) {
      console.error('解析保存的答案出错:', e);
      localStorage.removeItem(`assessment_answers_${questionnaire.value.id}`);
    }
  }
};

// 加载问卷信息
const loadQuestionnaire = async () => {
  error.value = '';
  loading.value = true;
  try {
    const id = Number(route.params.id);
    if (isNaN(id)) {
      error.value = '问卷ID无效';
      return;
    }
    
    console.log(`开始加载问卷 ID=${id}`);
    const response = await questionnaireApi.getQuestionnaire(id);
    console.log("加载的问卷信息:", response);
    
    // 检查问卷是否存在
    if (!response) {
      error.value = '问卷不存在';
      message.error('加载问卷失败，问卷不存在');
      return;
    }
    
    // 检查问卷状态是否存在
    if (!response.status) {
      console.error('问卷数据异常，缺少status属性:', response);
      error.value = '加载问卷失败，问卷数据异常';
      message.error('加载问卷失败，请稍后重试');
      return;
    }
    
    // 记录问卷状态
    console.log("问卷状态:", response.status, "问卷ID:", id, "标题:", response.title);
    
    // 检查问卷状态
    if (response.status === 'ARCHIVED') {
      console.warn('该问卷已归档，无法参与测评:', id, response.title);
      error.value = '此问卷已归档，无法参与测评。请联系管理员或选择其他问卷。';
      message.warning('此问卷已归档，无法参与测评');
      return;
    }
    
    // 检查问卷是否已发布
    if (response.status !== 'PUBLISHED') {
      console.warn('该问卷未发布，无法参与测评:', id, response.title);
      error.value = '此问卷尚未发布，无法参与测评。请联系管理员或选择其他问卷。';
      message.warning('此问卷尚未发布，无法参与测评');
      return;
    }
    
    // 问卷加载成功，设置问卷数据
    questionnaire.value = response;
    
    // 初始化答案数组
    formState.answers = new Array(response.questions.length).fill(undefined);
    
    // 检查是否有保存的答案
    checkSavedAnswers();
    
    console.log('问卷加载成功，准备开始测评:', response.title);
  } catch (err: any) {
    console.error('加载问卷失败:', err);
    // 处理常见的错误类型
    if (err.response && err.response.status === 404) {
      error.value = '问卷不存在';
    } else if (err.message && err.message.includes('Network Error')) {
      error.value = '网络错误，请检查网络连接';
    } else {
      error.value = err.response?.data?.message || '加载问卷失败';
    }
    message.error(error.value);
  } finally {
    loading.value = false;
  }
};

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validateFields();
    
    // 显示确认对话框
    if (window.confirm('确定要提交测评吗？提交后将无法修改。')) {
      submitting.value = true;
      console.log('准备提交的问卷ID:', questionnaire.value?.id);
      console.log('准备提交的答案:', formState.answers);
      
      try {
        // 提交前先保存答案到本地存储，以备提交失败时恢复
        localStorage.setItem(
          `assessment_answers_${questionnaire.value?.id}`, 
          JSON.stringify(formState.answers)
        );
        
        // 提交答案
        const submitData: AssessmentSubmitRequest = {
          questionnaireId: questionnaire.value!.id,
          answers: questionnaire.value!.questions
            .filter(question => question.id !== undefined) // 过滤掉 id 为 undefined 的问题
            .map((question, index) => {
              return {
                questionId: question.id!,  // 使用非空断言
                selectedOptions: Array.isArray(formState.answers[index]) 
                  ? formState.answers[index] as number[]
                  : [formState.answers[index] as number]
              };
            })
        };
        
        console.log('格式化后的提交数据:', submitData);
        
        // 保存当前路径，以便在401错误后重定向回来
        sessionStorage.setItem('assessmentSubmitData', JSON.stringify({
          path: router.currentRoute.value.fullPath,
          questionnaireId: questionnaire.value?.id,
          timestamp: new Date().getTime()
        }));
        
        const result = await assessmentApi.submitAnswers(submitData);
        console.log('测评提交成功:', result);
        
        // 提交成功后清除本地存储的答案
        localStorage.removeItem(`assessment_answers_${questionnaire.value?.id}`);
        
        message.success('测评提交成功！');
        
        // 使用延时确保消息显示后再跳转
        setTimeout(() => {
          try {
            const resultPath = `/student/assessment-result/${result.id}`;
            console.log('准备跳转到结果页面:', resultPath);
            
            // 记录提交成功的状态到sessionStorage，以便登录页面可以检测
            sessionStorage.setItem('assessmentSubmitSuccess', JSON.stringify({
              id: result.id,
              timestamp: new Date().getTime(),
              redirectPath: resultPath
            }));
            
            // 添加状态记录这是从成功提交后直接导航
            router.push({
              path: resultPath,
              query: { from: 'submit', t: new Date().getTime().toString() }
            }).catch(err => {
              console.error('路由跳转失败:', err);
              
              // 检查是否是权限问题导致的跳转失败
              const authStore = useAuthStore();
              if (!authStore.isAuthenticated) {
                console.warn('检测到未认证状态，可能是令牌已过期');
                // 保存目标路径
                sessionStorage.setItem('redirectPath', resultPath);
                // 特别标记这是提交后导航
                sessionStorage.setItem('fromAssessmentSubmit', 'true');
                // 延迟跳转到登录页
                setTimeout(() => router.push('/login'), 500);
              } else {
                message.error('跳转到结果页面失败，请手动前往查看结果');
              }
            });
          } catch (navError) {
            console.error('导航错误:', navError);
            message.error('跳转到结果页面出错，请手动前往查看结果');
          }
        }, 500);
      } catch (submitError: any) {
        // 特殊处理提交错误
        console.error('提交答案错误:', submitError);
        
        if (submitError.response && submitError.response.status === 401) {
          // 401错误，可能是会话过期
          message.error('您的会话已过期，系统已为您保存答案，请重新登录后继续');
          
          // 保存更详细的信息，包括当前时间戳
          sessionStorage.setItem('assessmentSubmitInfo', JSON.stringify({
            questionnaireId: questionnaire.value?.id,
            path: router.currentRoute.value.fullPath,
            timestamp: new Date().getTime(),
            action: 'submit'
          }));
          
          console.log('已保存测评提交状态，准备重定向到登录页');
          
          // 设置重定向路径
          sessionStorage.setItem('redirectPath', router.currentRoute.value.fullPath);
          // 特别标记这是提交时的认证错误
          sessionStorage.setItem('fromAssessmentSubmit', 'true');
          
          // 跳转到登录页
          setTimeout(() => {
            router.push('/login?from=submit&t=' + new Date().getTime());
          }, 1000);
        } else if (submitError.isAssessmentAuthError) {
          // 特殊处理拦截器标记的测评授权错误
          console.warn('检测到测评授权错误，准备跳转到登录页');
          
          // 保存当前状态
          localStorage.setItem(
            `assessment_submit_state_${questionnaire.value?.id}`, 
            JSON.stringify({
              timestamp: new Date().getTime(),
              answers: formState.answers,
              questionnaireId: questionnaire.value?.id
            })
          );
          
          // 跳转到登录页
          setTimeout(() => {
            router.push('/login?from=submit&t=' + new Date().getTime());
          }, 1000);
        } else {
          // 其他错误
          message.error(submitError.response?.data?.message || '提交失败，请稍后重试');
        }
      }
    }
  } catch (validationError) {
    message.error('请检查所有问题是否已回答');
  } finally {
    submitting.value = false;
  }
};

// 取消答题
const handleCancel = () => {
  router.push('/student/assessments');
};

// 自动保存答案
const autoSaveAnswers = () => {
  if (!questionnaire.value) return;
  
  const storageKey = `assessment_answers_${questionnaire.value.id}`;
  localStorage.setItem(storageKey, JSON.stringify(formState.answers));
  console.log('自动保存答案:', formState.answers);
  
  // 显示自动保存提示
  const hasAnswers = formState.answers.some(ans => ans !== undefined);
  if (hasAnswers) {
    message.info('已自动保存您的答案', 1);
  }
};

// 设置自动保存定时器
let autoSaveTimer: number | null = null;

// 监听表单变化
const handleFormChange = () => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer);
  }
  
  autoSaveTimer = window.setTimeout(() => {
    autoSaveAnswers();
  }, 3000); // 3秒后自动保存
};

// 监听页面离开事件
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (questionnaire.value && formState.answers.some(ans => ans !== undefined)) {
    // 在离开前自动保存
    autoSaveAnswers();
    
    // 显示确认对话框
    e.preventDefault();
    e.returnValue = '您尚未提交答案，确定要离开吗？';
    return e.returnValue;
  }
};

onMounted(() => {
  loadQuestionnaire();
  
  // 添加页面离开监听
  window.addEventListener('beforeunload', handleBeforeUnload);
});

// 页面卸载时清理
onUnmounted(() => {
  if (autoSaveTimer) {
    clearTimeout(autoSaveTimer);
  }
  
  // 移除页面离开监听
  window.removeEventListener('beforeunload', handleBeforeUnload);
});
</script>

<style scoped>
.take-assessment {
  padding: 24px;
}

.questionnaire-header {
  text-align: center;
}

.description {
  color: rgba(0, 0, 0, 0.45);
  margin: 16px 0;
}

.questionnaire-info {
  margin: 16px 0;
  color: rgba(0, 0, 0, 0.65);
}

.question-item {
  margin-bottom: 24px;
}

.submit-area {
  margin-top: 32px;
  text-align: center;
}
</style> 