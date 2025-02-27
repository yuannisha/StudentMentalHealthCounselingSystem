<template>
  <a-layout class="dashboard">
    <a-layout-sider width="200" class="sider">
      <div class="logo">
        <h2>心理咨询系统</h2>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
      >
        <a-menu-item key="appointments">
          <template #icon>
            <CalendarOutlined />
          </template>
          <router-link to="/counselor/appointments">预约管理</router-link>
        </a-menu-item>
        
        <a-menu-item key="student-assessment">
          <template #icon>
            <SolutionOutlined />
          </template>
          <router-link to="/counselor/student-assessments">学生测评</router-link>
        </a-menu-item>
        
        <a-menu-item key="profile">
          <template #icon>
            <UserOutlined />
          </template>
          <router-link to="/counselor/profile">个人信息</router-link>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="header">
        <div class="user-info">
          <a-dropdown>
            <a class="ant-dropdown-link" @click.prevent>
              {{ authStore.username }}
              <DownOutlined />
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">
                  <router-link to="/counselor/profile">个人信息</router-link>
                </a-menu-item>
                <a-menu-item key="logout" @click="handleLogout">
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="content">
        <router-view></router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  DownOutlined,
  CalendarOutlined,
  SolutionOutlined
} from '@ant-design/icons-vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

// 根据当前路由路径设置选中的菜单项
const selectedKeys = ref<string[]>(['appointments']);

// 监听路由变化，更新选中的菜单项
watch(() => router.currentRoute.value.path, (path) => {
  const routePath = path.split('/')[2];
  if (routePath) {
    selectedKeys.value = [routePath];
  }
}, { immediate: true });

const handleLogout = async () => {
  try {
    await authStore.logout();
    router.push('/login');
  } catch (error) {
    console.error('退出登录失败:', error);
  }
};
</script>

<style scoped>
.dashboard {
  height: 100vh;
}

.sider {
  overflow: auto;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
}

.logo {
  height: 64px;
  padding: 16px;
  color: white;
  text-align: center;
  background: rgba(255, 255, 255, 0.1);
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 16px;
  line-height: 32px;
}

.header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.content {
  margin: 24px 16px;
  padding: 24px;
  background: #fff;
  min-height: 280px;
  margin-left: 200px;
  border-radius: 4px;
}

.user-info {
  cursor: pointer;
}

.ant-dropdown-link {
  color: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
}

:deep(.ant-menu) {
  flex: 1;
}

:deep(.ant-menu-item) {
  cursor: pointer;
}

:deep(.ant-menu-item a) {
  color: inherit;
  text-decoration: none;
}
</style> 