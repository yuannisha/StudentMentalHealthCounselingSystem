import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage } from 'element-plus';
import 'element-plus/es/components/message/style/css';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/student',
      name: 'student',
      component: () => import('@/views/student/StudentDashboard.vue'),
      meta: { requiresAuth: true, role: 'STUDENT' },
      children: [
        {
          path: '',
          redirect: { name: 'assessment-list' }
        },
        {
          path: 'assessments',
          name: 'assessment-list',
          component: () => import('@/views/student/AssessmentList.vue')
        },
        {
          path: 'history',
          name: 'assessment-history',
          component: () => import('@/views/student/AssessmentHistory.vue')
        },
        {
          path: 'assessment/:id',
          name: 'take-assessment',
          component: () => import('@/views/student/TakeAssessment.vue')
        },
        {
          path: 'assessment-result/:id',
          name: 'assessment-result',
          component: () => import('@/views/student/AssessmentResult.vue')
        },
        {
          path: 'appointments',
          name: 'student-appointments',
          component: () => import('@/views/student/AppointmentList.vue')
        },
        {
          path: 'counselors',
          name: 'counselor-list',
          component: () => import('@/views/student/CounselorList.vue')
        },
        {
          path: 'profile',
          name: 'student-profile',
          component: () => import('@/views/components/UserProfile.vue')
        }
      ]
    },
    {
      path: '/counselor',
      name: 'counselor',
      component: () => import('@/views/counselor/CounselorDashboard.vue'),
      meta: { requiresAuth: true, role: 'COUNSELOR' },
      children: [
        {
          path: '',
          redirect: { name: 'counselor-appointments' }
        },
        {
          path: 'appointments',
          name: 'counselor-appointments',
          component: () => import('@/views/counselor/AppointmentManagement.vue')
        },
        {
          path: 'student-assessments',
          name: 'student-assessment-view',
          component: () => import('@/views/counselor/StudentAssessmentView.vue'),
          meta: { requiresAuth: true, role: 'COUNSELOR' }
        },
        {
          path: 'profile',
          name: 'counselor-profile',
          component: () => import('@/views/components/UserProfile.vue')
        }
      ]
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          path: '',
          redirect: { name: 'user-management' }
        },
        {
          path: 'users',
          name: 'user-management',
          component: () => import('@/views/admin/UserManagement.vue')
        },
        {
          path: 'questionnaires',
          name: 'questionnaire-management',
          component: () => import('@/views/admin/QuestionnaireManagement.vue')
        },
        {
          path: 'profile',
          name: 'admin-profile',
          component: () => import('@/views/components/UserProfile.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/login'
    }
  ]
});

/**
 * 全局前置守卫
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  // 检查是否需要认证
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  // 获取当前用户认证状态
  const isAuthenticated = authStore.token !== null;
  // 获取当前用户角色
  const userRole = authStore.userRole;
  
  console.log(`路由导航: 从 ${from.path} 到 ${to.path}, 认证状态: ${isAuthenticated}, 角色: ${userRole}`);

  // 如果页面需要认证但用户未登录
  if (requiresAuth && !isAuthenticated) {
    console.log(`需要认证但未登录，重定向到登录页面, 目标页面: ${to.path}`);
    
    // 保存被拦截的路由，登录后重定向
    sessionStorage.setItem('redirectPath', to.fullPath);
    
    // 特殊处理测评页面的数据保存
    if (from.path.includes('/student/assessment/take/') && !from.path.includes('result')) {
      console.log('从测评页面重定向到登录页，尝试保存测评答案');
      const assessmentId = from.params.id;
      if (assessmentId) {
        // 发出警告消息
        console.warn('请重新登录以继续测评，您的答案已临时保存');
      }
    }
    
    // 给用户反馈
    if (to.path.includes('/student/assessment/')) {
      if (to.path.includes('result')) {
        ElMessage.warning('登录已过期，请重新登录以查看测评结果');
      } else {
        ElMessage.warning('登录已过期，请重新登录以继续测评，您的答案不会丢失');
      }
    } else {
      ElMessage.warning('请先登录');
    }
    
    // 重定向到登录页
    next('/login');
  } 
  // 如果页面有角色限制
  else if (requiresAuth && isAuthenticated && to.meta.role) {
    // 检查用户角色是否有权限访问
    const role = to.meta.role as string;
    // 确保userRole非空后再进行比较
    if (userRole && userRole === role) {
      console.log(`用户角色 ${userRole} 有权限访问 ${to.path}`);
      next();
    } else {
      console.log(`用户角色 ${userRole} 无权限访问 ${to.path}，重定向到主页`);
      ElMessage.error('您没有权限访问该页面');
      // 根据用户角色跳转到对应的首页
      if (userRole) {
        switch (userRole) {
          case 'ADMIN':
            next('/admin');
            break;
          case 'COUNSELOR':
            next('/counselor');
            break;
          case 'STUDENT':
            next('/student');
            break;
          default:
            next('/login');
        }
      } else {
        next('/login');
      }
    }
  }
  // 如果是从角色页面访问子页面但未指定子页面角色要求
  else if (requiresAuth && isAuthenticated && !to.meta.role && to.matched.length > 1) {
    // 检查父路由是否有角色要求
    const parentRoute = to.matched[0];
    if (parentRoute.meta.role && userRole !== parentRoute.meta.role) {
      console.log(`用户角色 ${userRole} 无权限访问 ${to.path}，重定向到主页`);
      ElMessage.error('您没有权限访问该页面');
      
      if (userRole) {
        switch (userRole) {
          case 'ADMIN':
            next('/admin');
            break;
          case 'COUNSELOR':
            next('/counselor');
            break;
          case 'STUDENT':
            next('/student');
            break;
          default:
            next('/login');
        }
      } else {
        next('/login');
      }
    } else {
      next();
    }
  } else {
    // 如果不需要认证或已经认证，直接通过
    next();
  }
});

export default router; 