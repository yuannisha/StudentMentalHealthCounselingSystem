import request from '@/utils/request';
import type {
  Appointment,
  AppointmentListRequest,
  AppointmentListResponse,
  CreateAppointmentRequest
} from '@/types/appointment';

export const appointmentApi = {
  /**
   * 获取预约列表
   */
  getAppointments(params: AppointmentListRequest): Promise<AppointmentListResponse> {
    const role = localStorage.getItem('userRole');
    const endpoint = role === 'COUNSELOR' ? '/api/appointments/counselor' : '/api/appointments/student';
    return request.get(endpoint, { params });
  },

  /**
   * 创建预约
   */
  createAppointment(data: CreateAppointmentRequest): Promise<Appointment> {
    return request.post('/api/appointments', data);
  },

  /**
   * 确认预约
   */
  confirmAppointment(id: number): Promise<void> {
    return request.put(`/api/appointments/${id}/confirm`);
  },

  /**
   * 完成预约
   */
  completeAppointment(id: number): Promise<void> {
    return request.put(`/api/appointments/${id}/complete`);
  },

  /**
   * 取消预约
   */
  cancelAppointment(id: number): Promise<void> {
    return request.put(`/api/appointments/${id}/cancel`);
  }
}; 