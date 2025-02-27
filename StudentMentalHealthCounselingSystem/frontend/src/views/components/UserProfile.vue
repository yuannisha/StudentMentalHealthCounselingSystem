<template>
  <div class="user-profile">
    <a-card title="个人信息">
      <a-tabs v-model:activeKey="activeKey">
        <!-- 基本信息 -->
        <a-tab-pane key="basic" tab="基本信息">
          <a-form
            :model="profileForm"
            name="profile"
            @finish="handleUpdateProfile"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 16 }"
          >
            <a-form-item label="用户名">
              <span>{{ profileForm.username }}</span>
            </a-form-item>

            <a-form-item
              label="真实姓名"
              name="realName"
              :rules="[{ required: true, message: '请输入真实姓名!' }]"
            >
              <a-input v-model:value="profileForm.realName" />
            </a-form-item>

            <a-form-item
              label="手机号"
              name="phone"
              :rules="[{ required: true, message: '请输入手机号!' }]"
            >
              <a-input v-model:value="profileForm.phone" />
            </a-form-item>

            <a-form-item
              label="邮箱"
              name="email"
              :rules="[{ type: 'email', message: '请输入正确的邮箱格式!' }]"
            >
              <a-input v-model:value="profileForm.email" />
            </a-form-item>

            <template v-if="isStudent">
              <a-form-item label="大学" name="university">
                <a-input v-model:value="profileForm.university" />
              </a-form-item>

              <a-form-item label="学院" name="college">
                <a-input v-model:value="profileForm.college" />
              </a-form-item>

              <a-form-item label="专业" name="major">
                <a-input v-model:value="profileForm.major" />
              </a-form-item>

              <a-form-item label="学号" name="studentId">
                <a-input v-model:value="profileForm.studentId" />
              </a-form-item>
            </template>

            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-button type="primary" html-type="submit" :loading="loading">
                保存修改
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 修改密码 -->
        <a-tab-pane key="password" tab="修改密码">
          <a-form
            :model="passwordForm"
            name="password"
            @finish="handleChangePassword"
            :label-col="{ span: 4 }"
            :wrapper-col="{ span: 16 }"
          >
            <a-form-item
              label="旧密码"
              name="oldPassword"
              :rules="[{ required: true, message: '请输入旧密码!' }]"
            >
              <a-input-password v-model:value="passwordForm.oldPassword" />
            </a-form-item>

            <a-form-item
              label="新密码"
              name="newPassword"
              :rules="[{ required: true, message: '请输入新密码!' }]"
            >
              <a-input-password v-model:value="passwordForm.newPassword" />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
              <a-button type="primary" html-type="submit" :loading="loading">
                修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useAuthStore } from '@/stores/auth';
import { userApi } from '@/api/user';
import type { UpdateUserRequest } from '@/types/user';
import type { User } from '@/types/user';
import type { ChangePasswordRequest } from '@/types/user';

const authStore = useAuthStore();
const loading = ref(false);
const activeKey = ref('basic');

const profileForm = reactive<Partial<User>>({
  username: '',
  realName: '',
  phone: '',
  email: '',
  role: authStore.userRole!,
  university: '',
  college: '',
  major: '',
  studentId: ''
});

const passwordForm = reactive<ChangePasswordRequest>({
  oldPassword: '',
  newPassword: ''
});

const isStudent = computed(() => authStore.userRole === 'STUDENT');

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const data = await userApi.getCurrentUser();
    Object.assign(profileForm, data);
  } catch (error: any) {
    message.error('获取用户信息失败');
  }
};

// 更新用户信息
const handleUpdateProfile = async () => {
  loading.value = true;
  try {
    const request: UpdateUserRequest = {
      realName: profileForm.realName,
      phone: profileForm.phone,
      email: profileForm.email,
      university: profileForm.university,
      college: profileForm.college,
      major: profileForm.major,
      studentId: profileForm.studentId
    };
    
    await userApi.updateCurrentUser(request);
    message.success('更新成功');
  } catch (error: any) {
    message.error(error.response?.data?.message || '更新失败');
  } finally {
    loading.value = false;
  }
};

// 修改密码
const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.error('请填写完整的密码信息');
    return;
  }

  loading.value = true;
  try {
    await userApi.changePassword({ 
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });
    message.success('密码修改成功');
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
  } catch (error: any) {
    message.error(error.response?.data?.message || '修改失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchUserProfile();
});
</script>

<style scoped>
.user-profile {
  max-width: 800px;
  margin: 0 auto;
}
</style> 