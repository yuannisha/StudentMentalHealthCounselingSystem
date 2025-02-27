export type QuestionnaireStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE';

export interface QuestionOption {
  id?: number;
  optionText: string;
  score: number;
  sortOrder?: number;
}

export interface Question {
  id?: number;
  questionText: string;
  questionType: QuestionType;
  score: number;
  sortOrder?: number;
  options: QuestionOption[];
}

export interface QuestionnaireRequest {
  title: string;
  description?: string;
  passingScore?: number;
  questions: Question[];
}

export interface QuestionnaireResponse {
  id: number;
  title: string;
  description?: string;
  totalScore: number;
  passingScore?: number;
  status: QuestionnaireStatus;
  questions: Question[];
  createTime: string;
  updateTime: string;
}

export interface QuestionnaireListRequest {
  page: number;
  size: number;
  keyword?: string;
  statuses?: QuestionnaireStatus[];
}

export interface QuestionnaireListResponse {
  content: QuestionnaireResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// 答题相关类型定义
export interface AssessmentAnswer {
  questionId: number;
  selectedOptions: number[];
}

export interface AssessmentSubmitRequest {
  questionnaireId: number;
  answers: AssessmentAnswer[];
}

export interface AssessmentResultAnswer {
  questionId: number;
  selectedOptions: number[];
  correctOptions: number[];
  score: number;
}

export interface AssessmentResultResponse {
  id: number;
  totalScore: number;
  passed: boolean;
  questionnaire: QuestionnaireResponse;
  answers: AssessmentResultAnswer[];
  createTime: string;
}

export interface AssessmentHistoryResponse {
  content: AssessmentResultResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface QuestionnaireStatistics {
  questionnaireId: number;
  title: string;
  completedCount: number;
  distinctUsersCount: number;
  averageScore: number;
  passedCount: number;
  passRate: number;
  failedCount?: number;
  highestScore?: number;
  lowestScore?: number;
} 