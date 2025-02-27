import request from '@/utils/request';
import type { 
  AssessmentSubmitRequest, 
  AssessmentResultResponse,
  AssessmentHistoryResponse,
  QuestionnaireStatistics
} from '@/types/assessment';
import { message } from 'ant-design-vue';

// 包装请求方法，添加特殊的错误处理
const wrapRequest = async <T>(requestPromise: Promise<T>): Promise<T> => {
  try {
    return await requestPromise;
  } catch (error: any) {
    console.error('测评API错误:', error);
    
    // 特殊处理授权错误
    if (error.response && error.response.status === 401) {
      message.error('身份验证已过期，请重新登录后再提交');
    }
    
    throw error;
  }
};

export const assessmentApi = {
  /**
   * 提交答案
   */
  submitAnswers: (data: AssessmentSubmitRequest): Promise<AssessmentResultResponse> => {
    return wrapRequest(request.post('/api/assessment/answers', data));
  },

  /**
   * 获取测评结果
   */
  getResult: (id: number): Promise<AssessmentResultResponse> => {
    return wrapRequest(request.get(`/api/assessment/results/${id}`));
  },

  /**
   * 获取测评历史列表
   */
  getHistory: (params: any = {}): Promise<any> => {
    return wrapRequest(request.get('/api/assessment/results', { params }));
  },

  /**
   * 获取特定用户的测评历史（管理员或咨询师使用）
   */
  getUserHistory: (userId: number, params: any = {}): Promise<any> => {
    return wrapRequest(request.get(`/api/assessment/user/${userId}/results`, { params }));
  },

  /**
   * 获取问卷统计数据
   */
  getQuestionnaireStatistics(questionnaireId: number): Promise<QuestionnaireStatistics> {
    return request.get(`/api/assessment/questionnaires/${questionnaireId}/statistics`);
  }
}; 