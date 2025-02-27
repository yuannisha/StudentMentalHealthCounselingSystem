<template>
  <div class="appointment-list">
    <a-card title="我的预约">
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
            搜索
          </a-button>
          <a-button type="primary" @click="$router.push('/student/counselors')">
            新增预约
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
        <!-- 咨询师信息列 -->
        <template #counselor="{ text }">
          {{ text.realName }}
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
            <a @click="showAppointmentDetail(record)">查看详情</a>
            <a 
              v-if="record.status === 'PENDING' || record.status === 'CONFIRMED'" 
              @click="cancelAppointment(record.id)"
              class="cancel-link"
            >
              取消预约
            </a>
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
        <a-descriptions-item label="咨询师">
          {{ selectedAppointment.counselor.realName }}
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
import { ref, onMounted, h } from 'vue';
import { message, Modal } from 'ant-design-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
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
    title: '咨询师',
    dataIndex: 'counselor',
    slots: { customRender: 'counselor' }
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

// 显示预约详情
const showAppointmentDetail = (appointment: Appointment) => {
  selectedAppointment.value = appointment;
  drawerVisible.value = true;
};

// 取消预约
const cancelAppointment = (id: number) => {
  Modal.confirm({
    title: '确认取消预约？',
    icon: () => h(ExclamationCircleOutlined),
    content: '取消后将无法恢复，确定要取消吗？',
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        await appointmentApi.cancelAppointment(id);
        message.success('预约已取消');
        fetchAppointments();
      } catch (error: any) {
        message.error(error.response?.data?.message || '取消预约失败');
      }
    }
  });
};

// 获取状态颜色
const getStatusColor = (status: AppointmentStatus) => {
  const statusColors = {
    PENDING: 'orange',
    CONFIRMED: 'blue',
    COMPLETED: 'green',
    CANCELLED: 'red'
  };
  return statusColors[status] || 'default';
};

// 获取状态文本
const getStatusText = (status: AppointmentStatus) => {
  const statusTexts = {
    PENDING: '待确认',
    CONFIRMED: '已确认',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  };
  return statusTexts[status] || '未知状态';
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
.appointment-list {
  padding: 24px;
}

.search-bar {
  margin-bottom: 16px;
}

.cancel-link {
  color: #ff4d4f;
}

.cancel-link:hover {
  color: #ff7875;
}
</style> 