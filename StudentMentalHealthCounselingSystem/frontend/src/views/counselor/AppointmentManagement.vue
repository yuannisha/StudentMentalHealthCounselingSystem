<template>
  <div class="appointment-management">
    <a-card title="咨询预约管理">
      <!-- 搜索和筛选区域 -->
      <div class="search-bar">
        <a-space>
          <a-range-picker
            v-model:value="dateRange"
            :show-time="{ format: 'HH:mm' }"
            format="YYYY-MM-DD HH:mm"
            :placeholder="['开始时间', '结束时间']"
            @change="handleDateRangeChange"
          />
          <a-select
            v-model:value="statuses"
            style="width: 120px"
            placeholder="预约状态"
            @change="handleSearch"
          >
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="PENDING">待确认</a-select-option>
            <a-select-option value="CONFIRMED">已确认</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="CANCELLED">已取消</a-select-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
        </a-space>
      </div>

      <!-- 预约列表 -->
      <a-table
        :columns="columns"
        :data-source="appointments"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <!-- 学生信息列 -->
        <template #student="{ text }">
          {{ text.realName }} ({{ text.username }})
        </template>

        <!-- 预约时间列 -->
        <template #appointmentTime="{ text }">
          {{ formatDateTime(text) }}
        </template>

        <!-- 状态列 -->
        <template #status="{ text }">
          <a-tag :color="getStatusColor(text)">
            {{ getStatusText(text) }}
          </a-tag>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-space>
            <a-button
              v-if="record.status === 'PENDING'"
              type="primary"
              size="small"
              @click="handleConfirm(record)"
            >
              确认
            </a-button>
            <a-button
              v-if="record.status === 'CONFIRMED'"
              type="primary"
              size="small"
              @click="handleComplete(record)"
            >
              完成
            </a-button>
            <a-button
              v-if="['PENDING', 'CONFIRMED'].includes(record.status)"
              danger
              size="small"
              @click="handleCancel(record)"
            >
              取消
            </a-button>
            <a-button
              size="small"
              @click="showDetails(record)"
            >
              详情
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 预约详情抽屉 -->
    <a-drawer
      v-model:visible="drawerVisible"
      title="预约详情"
      placement="right"
      width="400"
    >
      <a-descriptions v-if="selectedAppointment" :column="1">
        <a-descriptions-item label="学生姓名">
          {{ selectedAppointment.student.realName }}
        </a-descriptions-item>
        <a-descriptions-item label="学生学号">
          {{ selectedAppointment.student.studentId }}
        </a-descriptions-item>
        <a-descriptions-item label="预约时间">
          {{ formatDateTime(selectedAppointment.appointmentTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="预约状态">
          <a-tag :color="getStatusColor(selectedAppointment.status)">
            {{ getStatusText(selectedAppointment.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="咨询主题">
          {{ selectedAppointment.subject }}
        </a-descriptions-item>
        <a-descriptions-item label="问题描述">
          {{ selectedAppointment.description }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatDateTime(selectedAppointment.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ formatDateTime(selectedAppointment.updateTime) }}
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import { SearchOutlined } from '@ant-design/icons-vue';
import type { Dayjs } from 'dayjs';
import { appointmentApi } from '@/api/appointment';
import type { Appointment, AppointmentStatus } from '@/types/appointment';

// 状态
const loading = ref(false);
const appointments = ref<Appointment[]>([]);
const dateRange = ref<[Dayjs, Dayjs]>();
const statuses = ref<AppointmentStatus | ''>('');
const drawerVisible = ref(false);
const selectedAppointment = ref<Appointment | null>(null);

// 分页配置
const pagination = ref<TablePaginationConfig>({
  total: 0,
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条记录`
});

// 表格列定义
const columns = [
  {
    title: '学生',
    dataIndex: 'student',
    slots: { customRender: 'student' }
  },
  {
    title: '预约时间',
    dataIndex: 'appointmentTime',
    slots: { customRender: 'appointmentTime' }
  },
  {
    title: '咨询主题',
    dataIndex: 'subject'
  },
  {
    title: '状态',
    dataIndex: 'status',
    slots: { customRender: 'status' }
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' }
  }
];

// 获取预约列表
const fetchAppointments = async () => {
  loading.value = true;
  try {
    const { current = 1, pageSize = 10 } = pagination.value;
    const params: any = {
      page: current - 1,
      size: pageSize,
      startTime: dateRange.value?.[0]?.format('YYYY-MM-DDTHH:mm:ss'),
      endTime: dateRange.value?.[1]?.format('YYYY-MM-DDTHH:mm:ss')
    };
    
    // 如果选择了状态，则添加到params中
    if (statuses.value) {
      params.status = statuses.value;
    }
    
    const response = await appointmentApi.getAppointments(params);
    appointments.value = response.content;
    pagination.value.total = response.totalElements;
  } catch (error: any) {
    message.error(error.response?.data?.message || '获取预约列表失败');
  } finally {
    loading.value = false;
  }
};

// 处理日期范围变化
const handleDateRangeChange = () => {
  handleSearch();
};

// 处理搜索
const handleSearch = () => {
  pagination.value.current = 1;
  fetchAppointments();
};

// 处理表格变化
const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  fetchAppointments();
};

// 确认预约
const handleConfirm = async (record: Appointment) => {
  try {
    await appointmentApi.confirmAppointment(record.id);
    message.success('预约已确认');
    fetchAppointments();
  } catch (error: any) {
    message.error(error.response?.data?.message || '确认预约失败');
  }
};

// 完成预约
const handleComplete = async (record: Appointment) => {
  try {
    await appointmentApi.completeAppointment(record.id);
    message.success('预约已完成');
    fetchAppointments();
  } catch (error: any) {
    message.error(error.response?.data?.message || '完成预约失败');
  }
};

// 取消预约
const handleCancel = async (record: Appointment) => {
  try {
    await appointmentApi.cancelAppointment(record.id);
    message.success('预约已取消');
    fetchAppointments();
  } catch (error: any) {
    message.error(error.response?.data?.message || '取消预约失败');
  }
};

// 显示预约详情
const showDetails = (record: Appointment) => {
  selectedAppointment.value = record;
  drawerVisible.value = true;
};

// 获取状态颜色
const getStatusColor = (status: AppointmentStatus) => {
  const colors: Record<string, string> = {
    PENDING: 'orange',
    CONFIRMED: 'blue',
    COMPLETED: 'green',
    CANCELLED: 'red'
  };
  return colors[status] || 'default';
};

// 获取状态文本
const getStatusText = (status: AppointmentStatus) => {
  const texts: Record<string, string> = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  };
  return texts[status] || status;
};

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 生命周期钩子
onMounted(() => {
  fetchAppointments();
});
</script>

<style scoped>
.appointment-management {
  padding: 24px;
}

.search-bar {
  margin-bottom: 16px;
}

:deep(.ant-table-pagination) {
  margin: 16px 0;
}
</style> 