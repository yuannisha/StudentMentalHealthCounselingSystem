import request from '@/utils/request';
import type { 
  Questionnaire, 
  CreateQuestionnaireRequest, 
  QuestionnaireListRequest,
  QuestionnaireListResponse 
} from '@/types/questionnaire';

export const questionnaireApi = {
  /**
   * 创建问卷
   */
  createQuestionnaire(data: CreateQuestionnaireRequest): Promise<Questionnaire> {
    return request.post('/api/questionnaires', data);
  },

  /**
   * 更新问卷
   */
  updateQuestionnaire(id: number, data: CreateQuestionnaireRequest): Promise<Questionnaire> {
    return request.put(`/api/questionnaires/${id}`, data);
  },

  /**
   * 获取问卷详情
   */
  getQuestionnaire(id: number): Promise<Questionnaire> {
    return request.get(`/api/questionnaires/${id}`);
  },

  /**
   * 获取问卷列表
   */
  getQuestionnaires(params: QuestionnaireListRequest): Promise<QuestionnaireListResponse> {
    return request.get('/api/questionnaires', { params });
  },

  /**
   * 发布问卷
   */
  publishQuestionnaire(id: number): Promise<void> {
    return request.put(`/api/questionnaires/${id}/publish`);
  },

  /**
   * 归档问卷
   */
  archiveQuestionnaire(id: number): Promise<void> {
    return request.put(`/api/questionnaires/${id}/archive`);
  },

  /**
   * 删除问卷
   */
  deleteQuestionnaire(id: number): Promise<void> {
    return request.delete(`/api/questionnaires/${id}`);
  }
}; 