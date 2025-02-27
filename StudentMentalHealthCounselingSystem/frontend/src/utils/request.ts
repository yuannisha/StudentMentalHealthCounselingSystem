import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { message } from 'ant-design-vue';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  timeout: 10000,
  paramsSerializer: {
    // 确保数组参数被正确序列化
    indexes: null  // 使用 statuses=DRAFT&statuses=PUBLISHED 格式而不是 statuses[]=DRAFT
  }
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    // 添加请求标记，用于区分不同类型的请求
    if (config.url?.includes('/api/assessment/answers')) {
      config.headers['X-Request-Type'] = 'assessment-submit';
    } else if (config.url?.includes('/api/assessment/results')) {
      config.headers['X-Request-Type'] = 'assessment-result';
    }
    
    console.log('发送请求:', {
      url: config.url,
      method: config.method,
      params: config.params,
      data: config.data,
      headers: config.headers
    });
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    console.log('收到响应:', {
      url: response.config.url,
      status: response.status,
      data: response.data,
      headers: response.config.headers
    });
    
    // 判断是否为测评提交或结果获取请求
    const requestType = response.config.headers['X-Request-Type'];
    
    // 如果是测评相关请求，确认授权状态
    if (requestType && (requestType === 'assessment-submit' || requestType === 'assessment-result')) {
      const authStore = useAuthStore();
      if (!authStore.isAuthenticated) {
        console.warn('发现测评相关请求成功，但授权状态异常，尝试恢复...');
        // 不做任何处理，让业务逻辑继续执行
      }
      
      // 如果是提交测评的请求，记录提交成功状态
      if (requestType === 'assessment-submit' && response.data && response.data.id) {
        console.log('测评提交成功，记录结果ID:', response.data.id);
        // 记录测评提交成功的状态，避免在后续操作中被打断
        sessionStorage.setItem('lastSubmittedAssessment', JSON.stringify({
          id: response.data.id,
          timestamp: new Date().getTime(),
          success: true
        }));
      }
    }
    
    return response.data;
  },
  (error) => {
    console.error('响应错误:', {
      config: error.config,
      response: error.response,
      message: error.message
    });

    if (error.response) {
      // 检查是否是测评提交答案的请求
      const isAssessmentSubmit = error.config && error.config.url && 
                               error.config.url.includes('/api/assessment/answers');
      const isAssessmentResult = error.config && error.config.url &&
                               error.config.url.includes('/api/assessment/results');
      
      switch (error.response.status) {
        case 401:
          // 如果是测评相关请求，记录状态但延迟处理认证问题
          if (isAssessmentSubmit || isAssessmentResult) {
            console.warn('测评相关请求遇到授权问题，记录状态...');
            
            // 保存当前路径和操作信息
            const currentPath = router.currentRoute.value.fullPath;
            sessionStorage.setItem('assessmentAuthError', JSON.stringify({
              path: currentPath,
              type: isAssessmentSubmit ? 'submit' : 'result',
              timestamp: new Date().getTime()
            }));
            
            sessionStorage.setItem('redirectPath', currentPath);
            
            // 给用户提示但不立即跳转，让组件处理
            message.error('会话已过期，请重新登录后继续');
            
            // 清除认证状态但延迟跳转，让测评组件有机会保存状态
            const authStore = useAuthStore();
            authStore.clearAuth();
            
            // 返回特殊的错误，让组件处理
            error.isAssessmentAuthError = true;
            return Promise.reject(error);
          }
          
          // 其他情况下，未授权，清除用户信息并跳转到登录页
          const authStore = useAuthStore();
          authStore.clearAuth();
          // 使用 router.push 替代直接修改 location
          router.push('/login');
          break;
        case 403:
          message.error('没有权限访问该资源');
          break;
        case 404:
          message.error('请求的资源不存在');
          break;
        case 500:
          message.error('服务器错误');
          break;
        default:
          message.error(error.response.data?.message || '请求失败');
      }
    } else if (error.request) {
      message.error('网络错误，请检查您的网络连接');
    } else {
      message.error('请求配置错误');
    }
    return Promise.reject(error);
  }
);

export default request; 