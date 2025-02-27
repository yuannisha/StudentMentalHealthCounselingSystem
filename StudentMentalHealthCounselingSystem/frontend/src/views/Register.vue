<template>
  <div class="register-container">
    <a-card title="用户注册" :bordered="false" class="register-card">
      <a-form
        :model="formState"
        name="register"
        @finish="onFinish"
        @finishFailed="onFinishFailed"
        autocomplete="off"
      >
        <a-form-item
          label="用户名"
          name="username"
          :rules="[{ required: true, message: '请输入用户名!' }]"
        >
          <a-input v-model:value="formState.username" />
        </a-form-item>

        <a-form-item
          label="密码"
          name="password"
          :rules="[{ required: true, message: '请输入密码!' }]"
        >
          <a-input-password v-model:value="formState.password" />
        </a-form-item>

        <a-form-item
          label="真实姓名"
          name="realName"
          :rules="[{ required: true, message: '请输入真实姓名!' }]"
        >
          <a-input v-model:value="formState.realName" />
        </a-form-item>

        <a-form-item
          label="手机号"
          name="phone"
          :rules="[{ required: true, message: '请输入手机号!' }]"
        >
          <a-input v-model:value="formState.phone" />
        </a-form-item>

        <a-form-item
          label="邮箱"
          name="email"
          :rules="[{ type: 'email', message: '请输入正确的邮箱格式!' }]"
        >
          <a-input v-model:value="formState.email" />
        </a-form-item>

        <a-form-item
          label="角色"
          name="role"
          :rules="[{ required: true, message: '请选择角色!' }]"
        >
          <a-select v-model:value="formState.role">
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="COUNSELOR">心理咨询师</a-select-option>
          </a-select>
        </a-form-item>

        <template v-if="formState.role === 'STUDENT'">
          <a-form-item label="大学" name="university">
            <a-input v-model:value="formState.university" />
          </a-form-item>

          <a-form-item label="学院" name="college">
            <a-input v-model:value="formState.college" />
          </a-form-item>

          <a-form-item label="专业" name="major">
            <a-input v-model:value="formState.major" />
          </a-form-item>

          <a-form-item label="学号" name="studentId">
            <a-input v-model:value="formState.studentId" />
          </a-form-item>
        </template>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="loading" block>
            注册
          </a-button>
        </a-form-item>

        <a-form-item>
          <a-button type="link" @click="$router.push('/login')" block>
            已有账号？立即登录
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { authApi } from '../api/auth';
import type { RegisterRequest } from '../api/auth';

const router = useRouter();
const authStore = useAuthStore();
const loading = ref(false);

const formState = reactive<RegisterRequest>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  role: 'STUDENT',
  university: '',
  college: '',
  major: '',
  studentId: ''
});

const onFinish = async (values: RegisterRequest) => {
  loading.value = true;
  try {
    const response = await authApi.register(values);
    authStore.setAuth(response.token, response.role, response.username);
    message.success('注册成功');
    
    // 返回登录页面
    router.push('/login');

  } catch (error: any) {
    message.error(error.response?.data?.message || '注册失败');
  } finally {
    loading.value = false;
  }
};

const onFinishFailed = (errorInfo: any) => {
  console.log('Failed:', errorInfo);
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.register-card {
  width: 100%;
  max-width: 500px;
  margin: 2rem 0;
}
</style> 