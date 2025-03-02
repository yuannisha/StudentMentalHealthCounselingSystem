# 学生心理健康咨询系统

## 项目概述

学生心理健康咨询系统是一个面向高校的多角色心理健康服务平台，旨在为学生提供在线心理测评、心理咨询预约等功能，帮助学校更好地关注和服务学生的心理健康。

系统支持三种角色（学生、心理咨询师、系统管理员）的不同功能需求，提供直观友好的用户界面，完善的权限管理以及数据安全保障。

## 项目详细介绍

本系统是一个全面的心理健康服务平台，致力于解决高校学生心理健康问题，提供从心理测评到心理咨询的完整服务链条。系统主要解决以下问题：

1. **心理问题早期识别**：通过专业心理测评量表，帮助学生及早发现潜在的心理问题，如抑郁、焦虑、学习压力等。
2. **心理咨询便捷化**：打破传统心理咨询的时间和空间限制，学生可随时在线预约心理咨询师。
3. **心理健康数据分析**：为学校提供学生心理健康状况的整体分析，帮助制定有针对性的心理健康工作策略。
4. **信息保密与安全**：采用严格的权限控制和数据加密措施，保障学生心理健康数据的安全和隐私。

### 用户场景与流程

#### 学生使用流程

1. **注册登录**：学生使用学号和初始密码登录系统，首次登录需完善个人信息。
2. **参与心理测评**：浏览可用的心理测评问卷，选择问卷进行测评，填写答案后获取即时评估结果和建议。
3. **查看历史评估**：访问"我的测评"页面，查看历史测评记录和结果分析，了解自己的心理健康变化趋势。
4. **预约咨询**：在"咨询预约"页面，查看心理咨询师空闲时段，选择合适的咨询师和时间预约咨询。
5. **管理预约**：在"我的预约"页面，查看、修改或取消自己的预约记录，查看咨询反馈。
6. **个人信息管理**：在"个人中心"修改个人资料、联系方式和密码。

#### 心理咨询师使用流程

1. **管理预约请求**：查看学生发起的咨询预约请求，确认或调整预约时间。
2. **查看学生评估**：在"学生管理"页面，查看预约学生的基本信息和心理测评历史，做好咨询前的准备工作。
3. **进行咨询记录**：咨询完成后，记录咨询内容和建议，给出针对性的心理健康指导。
4. **咨询统计分析**：查看自己的咨询工作量、学生类型分布等统计数据，优化咨询工作。

#### 系统管理员使用流程

1. **用户管理**：添加、修改或停用系统用户账号，包括学生、咨询师和其他管理员。
2. **问卷管理**：创建新的心理测评问卷，设置问题和选项，发布或归档问卷。
3. **系统监控**：查看系统使用情况，如活跃用户数、测评次数、预约量等关键指标。
4. **数据分析与报表**：生成学生心理健康统计报表，为学校心理健康工作提供数据支持。

## 功能介绍

### 学生功能

- **心理测评**：参与各类心理健康测评问卷，获得即时评估结果
- **测评历史**：查看历史测评记录及结果分析
- **咨询预约**：在线预约心理咨询师进行一对一咨询
- **预约管理**：查看、取消自己的预约记录
- **个人信息**：维护个人资料，修改密码等

### 心理咨询师功能

- **预约管理**：查看、确认、完成或取消学生的咨询预约
- **学生评估查看**：查看指定学生的心理测评历史及结果
- **个人信息**：维护个人资料，修改密码等

### 系统管理员功能

- **用户管理**：对所有用户（学生、咨询师、管理员）进行增删改查
- **问卷管理**：创建、编辑、发布、归档心理测评问卷
- **问卷统计**：查看各问卷的参与情况、通过率等统计数据
- **系统监控**：监控系统运行状态

## 技术栈

### 前端技术

- **框架**：Vue 3 + TypeScript
- **UI组件库**：Ant Design Vue
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **构建工具**：Vite

### 后端技术

- **框架**：Spring Boot
- **安全认证**：Spring Security + JWT
- **依赖管理**：Maven
- **数据库**：MySQL 8
- **数据库迁移**：Flyway
- **ORM框架**：Spring Data JPA
- **API文档**：Springdoc-OpenAPI (Swagger)

### 系统环境

- **操作系统**：Windows 11/10
- **JDK版本**：JDK 21
- **Node.js**：v16+ (推荐v18+)
- **NPM版本**：10.9.0+

## 系统架构

本系统采用前后端分离架构：

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│    前端     │      │   后端API   │      │   数据库    │
│  Vue3 + TS  │ <--> │ Spring Boot │ <--> │   MySQL8    │
└─────────────┘      └─────────────┘      └─────────────┘
```

- 前端通过HTTP请求与后端API进行通信
- 后端通过JWT实现无状态的身份验证
- 使用角色权限模型控制不同用户的访问权限

## 项目结构

### 前端结构

```
frontend/
├── public/            # 静态资源
├── src/
│   ├── api/           # API请求模块
│   │   ├── appointment.ts    # 预约相关API
│   │   ├── assessment.ts     # 心理测评相关API
│   │   ├── auth.ts           # 认证相关API
│   │   ├── counselor.ts      # 咨询师相关API
│   │   ├── questionnaire.ts  # 问卷相关API
│   │   ├── student.ts        # 学生相关API
│   │   ├── user.ts           # 用户相关API
│   │   └── index.ts          # API统一导出
│   ├── assets/        # 资源文件(图片、样式等)
│   │   ├── images/          # 图片资源
│   │   ├── styles/          # 全局样式
│   │   └── icons/           # 图标资源
│   ├── components/    # 通用组件
│   │   ├── common/          # 通用组件
│   │   │   ├── AppHeader.vue         # 应用头部组件
│   │   │   ├── AppFooter.vue         # 应用底部组件
│   │   │   ├── AppSidebar.vue        # 侧边栏组件
│   │   │   ├── LoadingSpinner.vue    # 加载指示器
│   │   │   ├── Notification.vue      # 通知组件
│   │   │   └── ErrorMessage.vue      # 错误消息组件
│   │   ├── assessment/      # 测评相关组件
│   │   │   ├── QuestionnaireCard.vue # 问卷卡片
│   │   │   ├── QuestionItem.vue      # 问题项组件
│   │   │   ├── AnswerOption.vue      # 答案选项组件
│   │   │   ├── ResultChart.vue       # 结果图表组件
│   │   │   └── AssessmentHistory.vue # 测评历史组件
│   │   ├── appointment/     # 预约相关组件
│   │   │   ├── AppointmentForm.vue   # 预约表单
│   │   │   ├── AppointmentList.vue   # 预约列表
│   │   │   ├── CounselorSelector.vue # 咨询师选择器
│   │   │   └── TimeSlotPicker.vue    # 时间段选择器
│   │   └── admin/           # 管理相关组件
│   │       ├── UserManagement.vue    # 用户管理组件
│   │       ├── QuestionnaireEditor.vue # 问卷编辑器
│   │       └── SystemStats.vue       # 系统统计组件
│   ├── router/        # 路由配置
│   │   ├── index.ts          # 路由主文件
│   │   ├── adminRoutes.ts    # 管理员路由
│   │   ├── counselorRoutes.ts # 咨询师路由
│   │   ├── studentRoutes.ts  # 学生路由
│   │   └── publicRoutes.ts   # 公共路由
│   ├── stores/        # Pinia状态管理
│   │   ├── auth.ts           # 认证状态
│   │   ├── user.ts           # 用户状态
│   │   ├── assessment.ts     # 测评状态
│   │   ├── appointment.ts    # 预约状态
│   │   └── notification.ts   # 通知状态
│   ├── types/         # TypeScript类型定义
│   │   ├── user.ts           # 用户相关类型
│   │   ├── assessment.ts     # 测评相关类型
│   │   ├── appointment.ts    # 预约相关类型
│   │   └── common.ts         # 通用类型
│   ├── utils/         # 工具函数
│   │   ├── request.ts        # HTTP请求工具
│   │   ├── auth.ts           # 认证工具
│   │   ├── date.ts           # 日期处理工具
│   │   ├── validation.ts     # 表单验证工具
│   │   └── formatter.ts      # 数据格式化工具
│   ├── views/         # 页面视图组件
│   │   ├── admin/     # 管理员页面
│   │   │   ├── Dashboard.vue         # 管理员仪表盘
│   │   │   ├── UserManagement.vue    # 用户管理页面
│   │   │   ├── QuestionnaireManagement.vue # 问卷管理页面
│   │   │   ├── QuestionnaireEditor.vue # 问卷编辑页面
│   │   │   ├── QuestionnaireStats.vue # 问卷统计页面
│   │   │   ├── SystemMonitor.vue     # 系统监控页面
│   │   │   └── AdminProfile.vue      # 管理员个人资料页面
│   │   ├── counselor/ # 咨询师页面
│   │   │   ├── Dashboard.vue         # 咨询师仪表盘
│   │   │   ├── AppointmentManagement.vue # 预约管理页面
│   │   │   ├── AppointmentDetail.vue # 预约详情页面
│   │   │   ├── StudentAssessment.vue # 学生评估查看页面
│   │   │   ├── CounselingRecord.vue  # 咨询记录页面
│   │   │   └── CounselorProfile.vue  # 咨询师个人资料页面
│   │   ├── student/   # 学生页面
│   │   │   ├── Dashboard.vue         # 学生仪表盘
│   │   │   ├── QuestionnaireList.vue # 问卷列表页面
│   │   │   ├── TakeAssessment.vue    # 参与测评页面
│   │   │   ├── AssessmentResult.vue  # 测评结果页面
│   │   │   ├── AssessmentHistory.vue # 测评历史页面
│   │   │   ├── AppointmentBooking.vue # 预约咨询页面
│   │   │   ├── MyAppointments.vue    # 我的预约页面
│   │   │   └── StudentProfile.vue    # 学生个人资料页面
│   │   └── common/    # 公共页面
│   │       ├── Login.vue             # 登录页面
│   │       ├── Register.vue          # 注册页面
│   │       ├── ForgotPassword.vue    # 忘记密码页面
│   │       ├── ResetPassword.vue     # 重置密码页面
│   │       ├── NotFound.vue          # 404页面
│   │       ├── Unauthorized.vue      # 未授权页面
│   │       └── MainLayout.vue        # 主布局页面
│   ├── App.vue        # 根组件
│   └── main.ts        # 入口文件
├── .env               # 环境变量
├── index.html         # HTML模板
├── package.json       # 依赖管理
├── tsconfig.json      # TypeScript配置
└── vite.config.ts     # Vite配置
```

### 后端结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/mentalhealth/counseling/
│   │   │   ├── config/          # 配置类
│   │   │   │   ├── SecurityConfig.java    # 安全配置
│   │   │   │   ├── JwtConfig.java         # JWT配置
│   │   │   │   ├── WebConfig.java         # Web配置
│   │   │   │   ├── SwaggerConfig.java     # Swagger配置
│   │   │   │   └── AuditConfig.java       # 审计配置
│   │   │   ├── controller/      # 控制器
│   │   │   │   ├── AuthController.java    # 认证控制器
│   │   │   │   ├── UserController.java    # 用户控制器
│   │   │   │   ├── StudentController.java # 学生控制器
│   │   │   │   ├── CounselorController.java # 咨询师控制器
│   │   │   │   ├── AdminController.java   # 管理员控制器
│   │   │   │   ├── QuestionnaireController.java # 问卷控制器
│   │   │   │   ├── AssessmentController.java # 测评控制器
│   │   │   │   └── AppointmentController.java # 预约控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   │   ├── request/              # 请求DTO
│   │   │   │   │   ├── LoginRequest.java    # 登录请求
│   │   │   │   │   ├── RegisterRequest.java # 注册请求
│   │   │   │   │   ├── UserUpdateRequest.java # 用户更新请求
│   │   │   │   │   ├── QuestionnaireRequest.java # 问卷请求
│   │   │   │   │   └── AppointmentRequest.java # 预约请求
│   │   │   │   └── response/             # 响应DTO
│   │   │   │       ├── JwtResponse.java     # JWT响应
│   │   │   │       ├── UserResponse.java    # 用户响应
│   │   │   │       ├── QuestionnaireResponse.java # 问卷响应
│   │   │   │       ├── AssessmentResponse.java # 测评响应
│   │   │   │       └── AppointmentResponse.java # 预约响应
│   │   │   ├── entity/          # 实体类
│   │   │   │   ├── User.java              # 用户实体
│   │   │   │   ├── Role.java              # 角色实体
│   │   │   │   ├── Student.java           # 学生实体
│   │   │   │   ├── Counselor.java         # 咨询师实体
│   │   │   │   ├── Questionnaire.java     # 问卷实体
│   │   │   │   ├── Question.java          # 问题实体
│   │   │   │   ├── Option.java            # 选项实体
│   │   │   │   ├── AssessmentRecord.java  # 测评记录实体
│   │   │   │   ├── AnswerRecord.java      # 答题记录实体
│   │   │   │   └── Appointment.java       # 预约实体
│   │   │   ├── exception/       # 异常处理
│   │   │   │   ├── GlobalExceptionHandler.java # 全局异常处理器
│   │   │   │   ├── ResourceNotFoundException.java # 资源未找到异常
│   │   │   │   ├── BadRequestException.java # 错误请求异常
│   │   │   │   └── UnauthorizedException.java # 未授权异常
│   │   │   ├── repository/      # 数据访问层
│   │   │   │   ├── UserRepository.java    # 用户仓库
│   │   │   │   ├── RoleRepository.java    # 角色仓库
│   │   │   │   ├── StudentRepository.java # 学生仓库
│   │   │   │   ├── CounselorRepository.java # 咨询师仓库
│   │   │   │   ├── QuestionnaireRepository.java # 问卷仓库
│   │   │   │   ├── QuestionRepository.java # 问题仓库
│   │   │   │   ├── OptionRepository.java  # 选项仓库
│   │   │   │   ├── AssessmentRecordRepository.java # 测评记录仓库
│   │   │   │   ├── AnswerRecordRepository.java # 答题记录仓库
│   │   │   │   └── AppointmentRepository.java # 预约仓库
│   │   │   ├── security/        # 安全相关
│   │   │   │   ├── JwtTokenProvider.java  # JWT令牌提供者
│   │   │   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   │   │   ├── UserDetailsServiceImpl.java # 用户详情服务实现
│   │   │   │   └── CustomUserDetails.java # 自定义用户详情
│   │   │   ├── service/         # 服务层
│   │   │   │   ├── AuthService.java       # 认证服务接口
│   │   │   │   ├── UserService.java       # 用户服务接口
│   │   │   │   ├── StudentService.java    # 学生服务接口
│   │   │   │   ├── CounselorService.java  # 咨询师服务接口
│   │   │   │   ├── QuestionnaireService.java # 问卷服务接口
│   │   │   │   ├── AssessmentService.java # 测评服务接口
│   │   │   │   ├── AppointmentService.java # 预约服务接口
│   │   │   │   └── impl/        # 服务实现
│   │   │   │       ├── AuthServiceImpl.java # 认证服务实现
│   │   │   │       ├── UserServiceImpl.java # 用户服务实现
│   │   │   │       ├── StudentServiceImpl.java # 学生服务实现
│   │   │   │       ├── CounselorServiceImpl.java # 咨询师服务实现
│   │   │   │       ├── QuestionnaireServiceImpl.java # 问卷服务实现
│   │   │   │       ├── AssessmentServiceImpl.java # 测评服务实现
│   │   │   │       └── AppointmentServiceImpl.java # 预约服务实现
│   │   │   └── CounselingSystemApplication.java # 应用入口
│   │   └── resources/
│   │       ├── application.yml  # 应用配置
│   │       ├── application-dev.yml # 开发环境配置
│   │       ├── application-prod.yml # 生产环境配置
│   │       └── db/migration/    # Flyway数据库迁移脚本
│   │           ├── V1__create_users_table.sql # 创建用户表
│   │           ├── V2__create_roles_table.sql # 创建角色表
│   │           ├── V3__create_questionnaires_table.sql # 创建问卷表
│   │           ├── V4__create_questions_table.sql # 创建问题表
│   │           ├── V5__create_options_table.sql # 创建选项表
│   │           ├── V6__create_assessment_records_table.sql # 创建测评记录表
│   │           ├── V7__create_answer_records_table.sql # 创建答题记录表
│   │           └── V8__create_appointments_table.sql # 创建预约表
│   └── test/                    # 测试代码
│       ├── java/com/mentalhealth/counseling/
│       │   ├── controller/      # 控制器测试
│       │   ├── service/         # 服务测试
│       │   ├── repository/      # 仓库测试
│       │   └── security/        # 安全测试
│       └── resources/           # 测试资源
├── pom.xml                      # Maven配置
└── README.md                    # 项目说明
```

## 模块说明

### 1. 用户认证模块

- 用户注册、登录功能
- JWT Token生成与验证
- 角色权限控制

### 2. 用户管理模块

- 用户信息CRUD操作
- 不同角色用户的管理功能
- 用户状态控制(启用/禁用)

### 3. 心理测评模块

- 问卷管理(创建、编辑、发布、归档)
- 测评问题和选项管理
- 测评结果计算与展示
- 测评历史记录管理

### 4. 咨询预约模块

- 咨询预约申请
- 预约状态管理(待确认、已确认、已完成、已取消)
- 预约列表查看和筛选

### 5. 个人信息模块

- 个人信息维护
- 密码修改

## 核心功能流程

### 心理测评流程

1. 系统管理员创建并发布心理测评问卷
2. 学生登录系统，查看可用的测评问卷
3. 学生选择问卷，进入测评页面
4. 学生回答问卷中的问题，提交答案
5. 系统根据答案计算测评结果并展示
6. 学生可以查看历史测评记录和结果

### 咨询预约流程

1. 学生在系统中查看可预约的心理咨询师
2. 学生选择咨询师和可用时间段，提交预约申请
3. 心理咨询师接收到预约通知，确认或拒绝预约
4. 预约确认后，双方按约定时间进行咨询
5. 咨询完成后，咨询师记录咨询内容和建议
6. 学生可以查看历史咨询记录和咨询反馈

## 数据库设计

系统使用MySQL 8作为数据库，主要包含以下核心表：

- `users`: 用户信息表
- `assessment_questionnaires`: 心理测评问卷表
- `assessment_questions`: 问卷题目表
- `question_options`: 题目选项表
- `assessment_records`: 测评记录表
- `answer_records`: 答题记录表
- `appointments`: 咨询预约表

## 数据库迁移

项目使用Flyway进行数据库版本控制，迁移脚本位于 `backend/src/main/resources/db/migration/` 目录下，按照以下命名规则：

```
V{version}__{description}.sql
```

例如：`V1__create_users_table.sql`

## 启动指南

### 环境准备

1. 安装JDK 21
2. 安装Node.js (v16+)和npm
3. 安装MySQL 8
4. 创建数据库 `mental_health_counseling`

### 使用一键启动脚本

1. 双击项目根目录下的 `启动项目.bat` 文件
2. 该脚本会自动启动前端和后端服务

### 手动启动

#### 后端启动

```bash
cd backend
mvn spring-boot:run
```

#### 前端启动

```bash
cd frontend
npm install
npm run dev
```

### 访问地址

- 前端：http://localhost:5173
- 后端API：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html

## 部署指南

### 生产环境部署

#### 后端部署

1. 编译打包：
   ```bash
   cd backend
   mvn clean package -Pprod
   ```

2. 将生成的JAR文件部署到服务器：
   ```bash
   java -jar counseling-system.jar --spring.profiles.active=prod
   ```

#### 前端部署

1. 构建前端静态文件：
   ```bash
   cd frontend
   npm run build
   ```

2. 将生成的`dist`目录下的文件部署到Web服务器(如Nginx)

### Docker部署

项目提供了Docker支持，可以使用Docker Compose进行一键部署：

```bash
docker-compose up -d
```

## 开发指南

### 前端开发

1. 添加新页面：
   - 在`src/views/`对应角色目录下创建Vue组件
   - 在`src/router/index.ts`中添加路由配置
   - 确保设置正确的权限控制 `meta: { requiresAuth: true, role: '角色名' }`

2. API调用：
   - 在`src/api/`下创建或修改对应模块的API调用方法
   - 使用`request`工具进行HTTP请求

### 后端开发

1. 添加新功能：
   - 创建实体类(Entity)
   - 添加数据访问层(Repository)
   - 实现服务层(Service)
   - 创建控制器(Controller)
   - 添加必要的DTO类

2. 权限控制：
   - 使用`@PreAuthorize`注解控制方法访问权限
   - 例如：`@PreAuthorize("hasRole('COUNSELOR')")`

## 系统亮点

1. **多角色权限控制**：基于Spring Security和JWT的精细化权限控制，确保不同角色用户只能访问自己权限范围内的功能。

2. **专业心理测评**：集成多种专业心理测评量表，如抑郁症筛查量表(PHQ-9)、广泛性焦虑量表(GAD-7)等，提供科学的心理健康评估。

3. **灵活的问卷设计**：支持多种题型（单选、多选、量表、文本）的问卷设计，满足不同心理测评需求。

4. **数据可视化**：直观展示测评结果和统计数据，帮助用户更好地理解心理健康状况。

5. **高度隐私保护**：采用数据加密、访问控制等多重安全措施，保护用户敏感的心理健康数据。

6. **响应式设计**：前端采用响应式设计，适配不同设备，提供良好的用户体验。

7. **完整的日志审计**：记录系统关键操作，便于问题追踪和安全审计。

## 后续规划

1. **智能推荐系统**：基于学生的心理测评结果，智能推荐相关的心理健康资源和服务。

2. **在线交流功能**：增加学生与咨询师的实时在线交流功能，提高咨询效率。

3. **移动端应用**：开发移动端应用，方便学生随时随地获取心理健康服务。

4. **AI辅助分析**：引入人工智能技术，辅助分析学生的心理健康状况，提供更精准的心理健康建议。

5. **知识库建设**：建立心理健康知识库，为学生提供自助学习和自我调节的资源。
