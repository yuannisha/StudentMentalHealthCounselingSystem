import type { User, UserProfileResponse, UpdateUserRequest, CreateUserRequest, UserListResponse, ChangePasswordRequest } from '../types/user';
import request from '../utils/request';

export const userApi = {
  /**
   * 获取当前用户信息
   */
  getCurrentUser(): Promise<UserProfileResponse> {
    return request.get('/api/users/profile');
  },

  /**
   * 更新当前用户信息
   */
  updateCurrentUser(data: UpdateUserRequest): Promise<UserProfileResponse> {
    return request.put('/api/users/profile', data);
  },

  /**
   * 修改密码
   */
  changePassword(data: ChangePasswordRequest): Promise<void> {
    return request.put('/api/users/password', data);
  },

  /**
   * 获取用户列表 (仅管理员可用)
   */
  getUsers(params: { page: number; size: number; keyword?: string }): Promise<UserListResponse> {
    return request.get('/api/admin/users', { params });
  },
  
  /**
   * 获取学生列表 (供咨询师使用)
   */
  getStudents(params: { page: number; size: number; keyword?: string }): Promise<UserListResponse> {
    return request.get('/api/counselor/students', { params });
  },

  /**
   * 获取咨询师列表
   */
  getCounselors(params: { page: number; size: number; search?: string }): Promise<UserListResponse> {
    return request.get('/api/users/counselors', { params });
  },

  /**
   * 创建用户
   */
  createUser(data: CreateUserRequest): Promise<User> {
    return request.post('/api/users', data);
  },

  /**
   * 更新用户
   */
  updateUser(id: number, data: UpdateUserRequest): Promise<User> {
    return request.put(`/api/users/${id}`, data);
  },

  /**
   * 删除用户
   */
  deleteUser(id: number): Promise<void> {
    return request.delete(`/api/users/${id}`);
  },

  /**
   * 启用/禁用用户
   */
  toggleUserStatus(id: number, enabled: boolean): Promise<void> {
    return request.put(`/api/users/${id}/status`, { enabled });
  }
}; 