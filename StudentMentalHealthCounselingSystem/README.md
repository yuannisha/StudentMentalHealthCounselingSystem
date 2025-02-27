# 学生心理健康咨询系统

## 项目概述

学生心理健康咨询系统是一个面向高校的多角色心理健康服务平台，旨在为学生提供在线心理测评、心理咨询预约等功能，帮助学校更好地关注和服务学生的心理健康。

系统支持三种角色（学生、心理咨询师、系统管理员）的不同功能需求，提供直观友好的用户界面，完善的权限管理以及数据安全保障。

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
│   ├── assets/        # 资源文件(图片、样式等)
│   ├── components/    # 通用组件
│   ├── router/        # 路由配置
│   ├── stores/        # Pinia状态管理
│   ├── types/         # TypeScript类型定义
│   ├── utils/         # 工具函数
│   ├── views/         # 页面视图组件
│   │   ├── admin/     # 管理员页面
│   │   ├── counselor/ # 咨询师页面
│   │   ├── student/   # 学生页面
│   │   └── common/    # 公共页面
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
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── exception/       # 异常处理
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── security/        # 安全相关
│   │   │   ├── service/         # 服务层
│   │   │   │   └── impl/        # 服务实现
│   │   │   └── CounselingSystemApplication.java # 应用入口
│   │   └── resources/
│   │       ├── application.yml  # 应用配置
│   │       └── db/migration/    # Flyway数据库迁移脚本
│   └── test/                    # 测试代码
└── pom.xml                      # Maven配置
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
