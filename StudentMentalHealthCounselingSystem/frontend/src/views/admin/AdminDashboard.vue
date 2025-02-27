<template>
  <a-layout class="dashboard">
    <a-layout-sider width="200" class="sider">
      <div class="logo">
        <h2>心理咨询系统</h2>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        theme="dark"
      >
        <a-menu-item key="users">
          <template #icon>
            <team-outlined />
          </template>
          <router-link to="/admin/users">用户管理</router-link>
        </a-menu-item>
        <a-menu-item key="questionnaires">
          <template #icon>
            <form-outlined />
          </template>
          <router-link to="/admin/questionnaires">问卷管理</router-link>
        </a-menu-item>
        <a-menu-item key="profile">
          <template #icon>
            <user-outlined />
          </template>
          <router-link to="/admin/profile">个人信息</router-link>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="header-right">
          <a-dropdown>
            <a class="user-dropdown" @click.prevent>
              {{ username }}
              <down-outlined />
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <logout-outlined />
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import {
  TeamOutlined,
  UserOutlined,
  LogoutOutlined,
  DownOutlined,
  FormOutlined,
} from '@ant-design/icons-vue';

const router = useRouter();
const authStore = useAuthStore();

const selectedKeys = ref<string[]>(['users']);
const username = computed(() => authStore.username);

const handleLogout = async () => {
  await authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
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
  line-height: 64px;
  text-align: center;
  color: white;
  font-size: 16px;
  overflow: hidden;
}

.header {
  background: #fff;
  padding: 0;
  position: fixed;
  width: calc(100% - 200px);
  z-index: 1;
}

.header-right {
  float: right;
  margin-right: 24px;
}

.user-dropdown {
  color: rgba(0, 0, 0, 0.85);
  cursor: pointer;
}

.content {
  margin: 64px 0 0 200px;
  overflow: initial;
  min-height: calc(100vh - 64px);
}
</style> 