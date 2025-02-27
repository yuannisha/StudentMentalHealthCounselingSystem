<template>
  <div class="login-container">
    <div class="login-content">
      <h1 class="site-title">心理咨询系统</h1>
      <a-card title="用户登录" :bordered="false" class="login-card">
        <a-form
          :model="formState"
          name="login"
          @finish="onFinish"
          @finishFailed="onFinishFailed"
          autocomplete="off"
          layout="vertical"
        >
          <a-form-item
            label="用户名"
            name="username"
            :rules="[{ required: true, message: '请输入用户名!' }]"
          >
            <a-input v-model:value="formState.username" size="large" />
          </a-form-item>

          <a-form-item
            label="密码"
            name="password"
            :rules="[{ required: true, message: '请输入密码!' }]"
          >
            <a-input-password v-model:value="formState.password" size="large" />
          </a-form-item>

          <a-form-item>
            <a-button type="primary" html-type="submit" :loading="loading" size="large" block>
              登录
            </a-button>
          </a-form-item>

          <a-form-item>
            <a-button type="link" @click="$router.push('/register')" block>
              还没有账号？立即注册
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { authApi } from '../api/auth';
import type { LoginRequest } from '../api/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const loading = ref(false);

const formState = reactive<LoginRequest>({
  username: '',
  password: ''
});

// 检查是否从测评页面跳转而来
const checkFromAssessment = () => {
  // 检查URL参数
  const fromSubmit = route.query.from === 'submit';
  
  // 检查sessionStorage中的标记
  const fromAssessmentSubmit = sessionStorage.getItem('fromAssessmentSubmit') === 'true';
  const assessmentSubmitInfo = sessionStorage.getItem('assessmentSubmitInfo');
  const assessmentAuthError = sessionStorage.getItem('assessmentAuthError');
  
  if (fromSubmit || fromAssessmentSubmit || assessmentSubmitInfo || assessmentAuthError) {
    console.log('检测到从测评页面跳转而来:', {
      fromSubmit,
      fromAssessmentSubmit,
      hasSubmitInfo: !!assessmentSubmitInfo,
      hasAuthError: !!assessmentAuthError
    });
    
    message.info('您需要重新登录以继续测评操作，您的测评数据已临时保存');
  }
};

onMounted(() => {
  checkFromAssessment();
});

const onFinish = async (values: LoginRequest) => {
  loading.value = true;
  try {
    console.log('开始登录请求:', values.username);
    const response = await authApi.login(values);
    authStore.setAuth(response.token, response.role, response.username);
    message.success('登录成功');
    
    // 检查是否有重定向路径
    const redirectPath = sessionStorage.getItem('redirectPath');
    console.log('检测到重定向路径:', redirectPath);
    
    // 检查是否有测评答案恢复
    const hasAssessmentAnswers = Object.keys(localStorage).some(key => 
      key.startsWith('assessment_answers_') || key.startsWith('assessment_submit_state_')
    );
    
    // 检查是否是从测评提交后跳转来的
    const fromAssessmentSubmit = sessionStorage.getItem('fromAssessmentSubmit') === 'true';
    const assessmentSubmitSuccess = sessionStorage.getItem('assessmentSubmitSuccess');
    
    if (hasAssessmentAnswers) {
      console.log('检测到保存的测评答案，准备恢复');
    }
    
    if (assessmentSubmitSuccess) {
      try {
        // 检查是否有提交成功的测评结果等待查看
        const submitInfo = JSON.parse(assessmentSubmitSuccess);
        if (submitInfo && submitInfo.redirectPath) {
          console.log('检测到测评提交成功信息，准备直接跳转到结果页:', submitInfo.redirectPath);
          message.info('登录成功，正在查看您的测评结果');
          
          // 清除状态
          sessionStorage.removeItem('assessmentSubmitSuccess');
          sessionStorage.removeItem('fromAssessmentSubmit');
          
          // 延迟跳转确保token已设置
          setTimeout(() => {
            router.push({
              path: submitInfo.redirectPath,
              query: { from: 'login-after-submit', t: new Date().getTime().toString() }
            });
          }, 500);
          return;
        }
      } catch (e) {
        console.error('解析测评提交状态出错:', e);
      }
    }

    if (redirectPath) {
      // 如果是测评相关页面，增加特殊处理
      if (redirectPath.includes('/student/assessment/') || redirectPath.includes('/student/assessment-result/')) {
        if (redirectPath.includes('result')) {
          console.log('正在重定向到测评结果页面:', redirectPath);
          message.info('重新登录成功，正在查看您的测评结果');
        } else {
          console.log('正在重定向到测评页面:', redirectPath);
          message.info('重新登录成功，可以继续您的测评');
        }
        
        // 清除测评相关状态
        sessionStorage.removeItem('fromAssessmentSubmit');
        sessionStorage.removeItem('assessmentAuthError');
      }
      
      // 清除存储的重定向路径
      sessionStorage.removeItem('redirectPath');
      
      // 使用延时确保token已经被正确设置
      setTimeout(() => {
        // 重定向到之前尝试访问的页面
        router.push(redirectPath);
      }, 500);
    } else {
      // 没有重定向路径时，根据用户角色跳转到对应的页面
      switch (response.role) {
        case 'ADMIN':
          router.push('/admin');
          break;
        case 'COUNSELOR':
          router.push('/counselor');
          break;
        case 'STUDENT':
          router.push('/student');
          break;
      }
    }
  } catch (error: any) {
    console.error('登录失败:', error);
    const errorMessage = error.response?.data?.message;
    if (errorMessage === 'Bad credentials') {
      message.error('用户名或密码错误');
    } else if (errorMessage === '账号已被禁用') {
      message.error('账号已被禁用，请联系管理员');
    } else {
      message.error(errorMessage || '登录失败');
    }
  } finally {
    loading.value = false;
  }
};

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo);
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 20px;
}

.login-content {
  width: 100%;
  max-width: 400px;
}

.site-title {
  text-align: center;
  font-size: 33px;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 600;
  margin-bottom: 40px;
}

.login-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

:deep(.ant-card-head) {
  border-bottom: none;
  padding: 24px 24px 0;
}

:deep(.ant-card-head-title) {
  font-size: 20px;
  text-align: center;
}

:deep(.ant-card-body) {
  padding: 24px;
}

:deep(.ant-form-item-label) {
  padding-bottom: 8px;
}

:deep(.ant-input), :deep(.ant-input-password) {
  height: 40px;
}

:deep(.ant-btn) {
  height: 40px;
}

@media (max-width: 576px) {
  .login-content {
    max-width: 100%;
  }

  .site-title {
    font-size: 28px;
    margin-bottom: 30px;
  }

  :deep(.ant-card-head) {
    padding: 16px 16px 0;
  }

  :deep(.ant-card-body) {
    padding: 16px;
  }
}
</style> 