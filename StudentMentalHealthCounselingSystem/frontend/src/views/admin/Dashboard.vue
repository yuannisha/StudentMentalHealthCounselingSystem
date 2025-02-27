<template>
  <div class="admin-dashboard">
    <a-layout>
      <a-layout-header class="header">
        <div class="logo">心理健康咨询系统 - 管理员</div>
        <div class="user-info">
          <a-dropdown>
            <a class="ant-dropdown-link" @click.prevent>
              {{ authStore.username }}
              <down-outlined />
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout>
        <a-layout-sider width="200">
          <a-menu
            v-model:selectedKeys="selectedKeys"
            mode="inline"
            :style="{ height: '100%', borderRight: 0 }"
          >
            <a-menu-item key="users">
              <user-outlined />
              <span>用户管理</span>
            </a-menu-item>
          </a-menu>
        </a-layout-sider>
        <a-layout-content class="content">
          <router-view></router-view>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { UserOutlined, DownOutlined } from '@ant-design/icons-vue';

const router = useRouter();
const authStore = useAuthStore();
const selectedKeys = ref<string[]>(['users']);

const handleLogout = () => {
  authStore.clearAuth();
  router.push('/login');
};
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
}

.user-info {
  color: rgba(0, 0, 0, 0.65);
}

.content {
  padding: 24px;
  background: #fff;
  min-height: 280px;
  margin: 24px;
}
</style> 