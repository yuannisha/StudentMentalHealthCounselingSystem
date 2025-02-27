export type UserRole = 'ADMIN' | 'COUNSELOR' | 'STUDENT';

export interface User {
  id: number;
  username: string;
  realName: string;
  email: string;
  phone: string;
  role: UserRole;
  enabled: boolean;
  university?: string;
  college?: string;
  major?: string;
  studentId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserProfileResponse extends User {
  // 如果有额外的字段，可以在这里添加
}

export interface UpdateUserRequest {
  realName?: string;
  email?: string;
  phone?: string;
  university?: string;
  college?: string;
  major?: string;
  studentId?: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  realName: string;
  email: string;
  phone: string;
  role: UserRole;
  university?: string;
  college?: string;
  major?: string;
  studentId?: string;
}

export interface UserListResponse {
  content: User[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface UserProfile extends User {
  // 用于个人信息页面的用户信息
}