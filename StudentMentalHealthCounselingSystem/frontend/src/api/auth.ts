import axios from 'axios';
import type { UserRole } from '../types/user';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  realName: string;
  phone: string;
  email?: string;
  role: UserRole;
  university?: string;
  college?: string;
  major?: string;
  studentId?: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  role: UserRole;
}

export const authApi = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/register', data);
    return response.data;
  }
}; 