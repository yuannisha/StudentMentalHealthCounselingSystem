<template>
  <div class="user-management">
    <a-card title="用户管理">
      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <a-space>
          <a-input
            v-model:value="searchQuery"
            placeholder="搜索用户名、姓名、邮箱或手机号"
            allow-clear
            @change="handleSearch"
            style="width: 300px"
          >
            <template #prefix>
              <search-outlined />
            </template>
          </a-input>
          <a-select
            v-model:value="selectedRole"
            placeholder="选择角色"
            style="width: 150px"
            allow-clear
            @change="handleSearch"
          >
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="COUNSELOR">心理咨询师</a-select-option>
            <a-select-option value="ADMIN">管理员</a-select-option>
          </a-select>
        </a-space>
      </div>

      <!-- 用户列表表格 -->
      <a-table
        :columns="columns"
        :data-source="users"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        row-key="id"
      >
        <!-- 角色列 -->
        <template #role="{ text }">
          <a-tag :color="getRoleColor(text)">{{ getRoleText(text) }}</a-tag>
        </template>

        <!-- 状态列 -->
        <template #enabled="{ text, record }">
          <a-switch
            :checked="text"
            :disabled="record.role === 'ADMIN'"
            @change="(checked: boolean) => handleToggleStatus(record.id, checked)"
          />
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" @click="handleViewDetails(record)">
              查看详情
            </a-button>
            <a-popconfirm
              v-if="record.role !== 'ADMIN'"
              title="确定要删除该用户吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 用户详情抽屉 -->
    <a-drawer
      v-model:visible="drawerVisible"
      title="用户详情"
      placement="right"
      width="600"
    >
      <template v-if="selectedUser">
        <a-descriptions :column="1">
          <a-descriptions-item label="用户名">
            {{ selectedUser.username }}
          </a-descriptions-item>
          <a-descriptions-item label="真实姓名">
            {{ selectedUser.realName }}
          </a-descriptions-item>
          <a-descriptions-item label="手机号">
            {{ selectedUser.phone }}
          </a-descriptions-item>
          <a-descriptions-item label="邮箱">
            {{ selectedUser.email }}
          </a-descriptions-item>
          <a-descriptions-item label="角色">
            <a-tag :color="getRoleColor(selectedUser.role)">
              {{ getRoleText(selectedUser.role) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="selectedUser.enabled ? 'success' : 'error'">
              {{ selectedUser.enabled ? '启用' : '禁用' }}
            </a-tag>
          </a-descriptions-item>
          <template v-if="selectedUser.role === 'STUDENT'">
            <a-descriptions-item label="大学">
              {{ selectedUser.university }}
            </a-descriptions-item>
            <a-descriptions-item label="学院">
              {{ selectedUser.college }}
            </a-descriptions-item>
            <a-descriptions-item label="专业">
              {{ selectedUser.major }}
            </a-descriptions-item>
            <a-descriptions-item label="学号">
              {{ selectedUser.studentId }}
            </a-descriptions-item>
          </template>
          <a-descriptions-item label="创建时间">
            {{ formatDate(selectedUser.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatDate(selectedUser.updatedAt) }}
          </a-descriptions-item>
        </a-descriptions>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { SearchOutlined } from '@ant-design/icons-vue';
import { adminApi } from '@/api/admin';
import type { UserProfile } from '@/types/user';
import type { TablePaginationConfig } from 'ant-design-vue';

// 表格列定义
const columns = [
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
  },
  {
    title: '真实姓名',
    dataIndex: 'realName',
    key: 'realName',
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    slots: { customRender: 'role' },
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
  },
  {
    title: '状态',
    dataIndex: 'enabled',
    key: 'enabled',
    slots: { customRender: 'enabled' },
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
  },
];

// 状态定义
const users = ref<UserProfile[]>([]);
const loading = ref(false);
const searchQuery = ref('');
const selectedRole = ref<string | undefined>();
const pagination = ref<TablePaginationConfig>({
  total: 0,
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

const drawerVisible = ref(false);
const selectedUser = ref<UserProfile | null>(null);

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  try {
    const { current, pageSize } = pagination.value;
    const response = await adminApi.getUsers({
      search: searchQuery.value,
      role: selectedRole.value as any,
      page: (current || 1) - 1,
      size: pageSize,
    });
    users.value = response.content;
    pagination.value.total = response.totalElements;
  } catch (error) {
    message.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

// 事件处理函数
const handleSearch = () => {
  pagination.value.current = 1;
  fetchUsers();
};

const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.value = pag;
  fetchUsers();
};

const handleToggleStatus = async (userId: number, enabled: boolean) => {
  try {
    await adminApi.toggleUserStatus(userId, enabled);
    message.success(`${enabled ? '启用' : '禁用'}用户成功`);
    fetchUsers();
  } catch (error: any) {
    message.error(error.response?.data?.message || `${enabled ? '启用' : '禁用'}用户失败`);
  }
};

const handleDelete = async (userId: number) => {
  try {
    await adminApi.deleteUser(userId);
    message.success('删除用户成功');
    fetchUsers();
  } catch (error: any) {
    message.error(error.response?.data?.message || '删除用户失败');
  }
};

const handleViewDetails = (user: UserProfile) => {
  selectedUser.value = user;
  drawerVisible.value = true;
};

// 工具函数
const getRoleColor = (role: string) => {
  const colors: Record<string, string> = {
    STUDENT: 'blue',
    COUNSELOR: 'green',
    ADMIN: 'red',
  };
  return colors[role] || 'default';
};

const getRoleText = (role: string) => {
  const texts: Record<string, string> = {
    STUDENT: '学生',
    COUNSELOR: '心理咨询师',
    ADMIN: '管理员',
  };
  return texts[role] || role;
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

// 生命周期钩子
onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-management {
  padding: 24px;
}

.search-bar {
  margin-bottom: 16px;
}
</style> 