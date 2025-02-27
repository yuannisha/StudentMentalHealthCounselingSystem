import type { User } from './user';

export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface Appointment {
  id: number;
  student: User;
  counselor: User;
  subject: string;
  description: string;
  appointmentTime: string;
  status: AppointmentStatus;
  createTime: string;
  updateTime: string;
}

export interface AppointmentListRequest {
  page: number;
  size: number;
  status?: AppointmentStatus;
  statuses?: AppointmentStatus[];
  startTime?: string;
  endTime?: string;
}

export interface AppointmentListResponse {
  content: Appointment[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface CreateAppointmentRequest {
  counselorId: number;
  subject: string;
  description: string;
  appointmentTime: string;
} 