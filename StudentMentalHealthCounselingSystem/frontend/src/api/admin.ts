import request from '@/utils/request';
import { UserProfile } from '@/types/user';

export interface GetUsersParams {
  search?: string;
  role?: 'STUDENT' | 'COUNSELOR' | 'ADMIN';
  page?: number;
  size?: number;
  sort?: string[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const adminApi = {
  /**
   * 获取用户列表
   */
  async getUsers(params: GetUsersParams): Promise<PageResponse<UserProfile>> {
    return request.get('/api/admin/users', { params });
  },

  /**
   * 删除用户
   */
  async deleteUser(userId: number): Promise<void> {
    return request.delete(`/api/admin/users/${userId}`);
  },

  /**
   * 禁用/启用用户
   */
  async toggleUserStatus(userId: number, enabled: boolean): Promise<void> {
    return request.put(`/api/admin/users/${userId}/status`, null, {
      params: { enabled }
    });
  }
}; 