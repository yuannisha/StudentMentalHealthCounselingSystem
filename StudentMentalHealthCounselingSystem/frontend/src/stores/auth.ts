import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { UserRole } from '@/types/user';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'));
  const userRole = ref<UserRole | null>(localStorage.getItem('userRole') as UserRole);
  const username = ref<string | null>(localStorage.getItem('username'));

  const isAuthenticated = computed(() => !!token.value);

  const defaultRoute = computed(() => {
    switch (userRole.value) {
      case 'ADMIN':
        return 'admin';
      case 'COUNSELOR':
        return 'counselor';
      case 'STUDENT':
        return 'student';
      default:
        return 'login';
    }
  });

  function setAuth(authToken: string, role: UserRole, name: string) {
    token.value = authToken;
    userRole.value = role;
    username.value = name;
    localStorage.setItem('token', authToken);
    localStorage.setItem('userRole', role);
    localStorage.setItem('username', name);
  }

  function clearAuth() {
    token.value = null;
    userRole.value = null;
    username.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    localStorage.removeItem('username');
  }

  async function logout() {
    try {
      // 可以在这里添加调用后端登出接口的逻辑
      clearAuth();
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    }
  }

  return {
    token,
    userRole,
    username,
    isAuthenticated,
    defaultRoute,
    setAuth,
    clearAuth,
    logout
  };
}); 