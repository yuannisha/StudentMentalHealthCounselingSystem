<template>
  <div class="counselor-list">
    <a-card title="心理咨询师列表">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索咨询师姓名"
          style="width: 300px"
          @search="handleSearch"
        />
      </div>

      <!-- 咨询师列表 -->
      <a-list
        :loading="loading"
        :data-source="counselors"
        :pagination="pagination"
        item-layout="vertical"
      >
        <template #renderItem="{ item }">
          <a-list-item key="item.id">
            <a-card :bordered="false" style="width: 100%">
              <a-row>
                <a-col :span="18">
                  <h3>{{ item.realName }}</h3>
                  <a-descriptions :column="2">
                    <a-descriptions-item label="联系电话">
                      {{ item.phone }}
                    </a-descriptions-item>
                    <a-descriptions-item label="邮箱">
                      {{ item.email }}
                    </a-descriptions-item>
                  </a-descriptions>
                </a-col>
                <a-col :span="6" style="text-align: right;">
                  <a-button type="primary" @click="openAppointmentModal(item)">
                    预约咨询
                  </a-button>
                </a-col>
              </a-row>
            </a-card>
          </a-list-item>
        </template>
        <template #empty>
          <a-empty description="暂无咨询师" />
        </template>
      </a-list>
    </a-card>

    <!-- 预约表单模态框 -->
    <a-modal
      v-model:visible="modalVisible"
      title="预约咨询"
      @ok="handleAppointmentSubmit"
      :confirmLoading="submitting"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
      >
        <a-form-item label="咨询师" name="counselorName">
          <a-input v-model:value="formState.counselorName" disabled />
        </a-form-item>
        <a-form-item label="咨询主题" name="subject">
          <a-input v-model:value="formState.subject" placeholder="请输入咨询主题" />
        </a-form-item>
        <a-form-item label="问题描述" name="description">
          <a-textarea 
            v-model:value="formState.description" 
            placeholder="请描述您的问题"
            :rows="4" 
          />
        </a-form-item>
        <a-form-item label="预约时间" name="appointmentTime">
          <a-date-picker
            v-model:value="formState.appointmentTime"
            :show-time="{ format: 'HH:mm' }"
            format="YYYY-MM-DD HH:mm"
            :disabledDate="disabledDate"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import type { FormInstance, TablePaginationConfig } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { userApi } from '@/api/user';
import { appointmentApi } from '@/api/appointment';
import type { User } from '@/types/user';
import type { CreateAppointmentRequest } from '@/types/appointment';

const router = useRouter();
const loading = ref(false);
const submitting = ref(false);
const searchKeyword = ref('');
const counselors = ref<User[]>([]);
const modalVisible = ref(false);
const selectedCounselor = ref<User | null>(null);
const formRef = ref<FormInstance>();

// 表单状态
const formState = reactive({
  counselorId: 0,
  counselorName: '',
  subject: '',
  description: '',
  appointmentTime: null as Dayjs | null
});

// 表单验证规则
const rules = {
  subject: [{ required: true, message: '请输入咨询主题' }],
  appointmentTime: [{ required: true, message: '请选择预约时间' }]
};

// 分页配置
const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  onChange: (page: number, pageSize: number) => {
    pagination.current = page;
    pagination.pageSize = pageSize;
    fetchCounselors();
  }
});

// 获取咨询师列表
const fetchCounselors = async () => {
  loading.value = true;
  try {
    const params = {
      page: (pagination.current || 1) - 1,
      size: pagination.pageSize || 10,
      search: searchKeyword.value || undefined
    };
    
    const response = await userApi.getCounselors(params);
    counselors.value = response.content;
    pagination.total = response.totalElements;
  } catch (error: any) {
    message.error(error.response?.data?.message || '获取咨询师列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索咨询师
const handleSearch = () => {
  pagination.current = 1;
  fetchCounselors();
};

// 打开预约模态框
const openAppointmentModal = (counselor: User) => {
  selectedCounselor.value = counselor;
  formState.counselorId = counselor.id;
  formState.counselorName = counselor.realName;
  formState.subject = '';
  formState.description = '';
  formState.appointmentTime = null;
  modalVisible.value = true;
};

// 禁用过去的日期
const disabledDate = (current: Dayjs) => {
  return current && current < dayjs().startOf('day');
};

// 提交预约
const handleAppointmentSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    submitting.value = true;
    
    const appointmentData: CreateAppointmentRequest = {
      counselorId: formState.counselorId,
      subject: formState.subject,
      description: formState.description,
      appointmentTime: formState.appointmentTime!.format('YYYY-MM-DDTHH:mm:ss')
    };
    
    await appointmentApi.createAppointment(appointmentData);
    message.success('预约提交成功！');
    modalVisible.value = false;
    router.push('/student/appointments');
  } catch (error: any) {
    message.error(error.response?.data?.message || '预约提交失败');
  } finally {
    submitting.value = false;
  }
};

// 生命周期钩子
onMounted(() => {
  fetchCounselors();
});
</script>

<style scoped>
.counselor-list {
  padding: 24px;
}

.search-bar {
  margin-bottom: 16px;
}
</style> 