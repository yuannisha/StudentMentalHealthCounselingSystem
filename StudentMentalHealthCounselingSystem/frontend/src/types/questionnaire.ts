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

export interface Questionnaire {
  id: number;
  title: string;
  description: string;
  totalScore: number;
  passingScore: number;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  questions: Question[];
  createTime?: string;
  updateTime?: string;
}

export interface CreateQuestionnaireRequest {
  title: string;
  description: string;
  passingScore: number;
  questions: Question[];
}

export interface UpdateQuestionnaireRequest extends CreateQuestionnaireRequest {}

export interface QuestionnaireListRequest {
  keyword?: string;
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  statuses?: ('DRAFT' | 'PUBLISHED' | 'ARCHIVED')[];
  page?: number;
  size?: number;
}

export interface QuestionnaireListResponse {
  content: Questionnaire[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
} 