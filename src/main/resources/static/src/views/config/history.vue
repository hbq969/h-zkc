<script lang="ts" setup>
import {onMounted, reactive, ref} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
import router from "@/router";

const isADMIN = ref(false)
const menuMap = reactive({})

onMounted(() => {
  axios({
    url: '/system/user',
    method: 'get'
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      isADMIN.value = (res.data.body.roleName == 'ADMIN')
      if (res.data.body.menus && res.data.body.menus.length > 0) {
        res.data.body.menus.forEach(item => {
          menuMap[item.url] = 'Y'
        })
      }
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
})

const formLabelWidth = ref('140px')
const data = reactive({
  actions: []
});

const historyData = ref<HistoryRecord>()
const historyFormRef = ref<FormInstance>()
const historyRules = reactive<FormRules>({
  // time: [{required: true, message: '请选择时间', trigger: 'blur'}]
})
const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const historyForm = reactive({
  time: [],
  user: '',
  operate: ''
})
const searchHistory = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/queryHistoryOperates/v1.0',
        method: 'post',
        params: page,
        data: historyForm
      }).then((res: any) => {
        if (res.data.code == 1) {
          historyData.value = res.data.body.list
          page.total = res.data.body.total
        } else {
          msg('查询失败', 'error')
        }
      })
    } else {
      console.log('error submit!', fields)
    }
  })
}

const queryActions = () => {
}

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

onMounted(() => {
  console.log('页面加载后')
  queryActions()
});

const debounce = (callback: (...args: any[]) => void, delay: number) => {
  let tid: any;
  return function (...args: any[]) {
    const ctx = self;
    tid && clearTimeout(tid);
    tid = setTimeout(() => {
      callback.apply(ctx, args);
    }, delay);
  };
};

const _ = (window as any).ResizeObserver;
(window as any).ResizeObserver = class ResizeObserver extends _ {
  constructor(callback: (...args: any[]) => void) {
    callback = debounce(callback, 20);
    super(callback);
  }
};

</script>

<template>
  <div class="container">
    <el-form :model="historyForm" :inline="true" ref="historyFormRef" :rules="historyRules" size="small" style="margin-top: 20px">
      <el-form-item label="时间：" prop="time">
        <el-date-picker v-model="historyForm.time" type="daterange" unlink-panels range-separator="至"
                        start-placeholder="开始时间" end-placeholder="结束时间" format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="用户：">
        <el-input v-model="historyForm.user" type="text"/>
      </el-form-item>
      <el-form-item label="操作：">
        <el-select v-model="historyForm.operate" class="m-2" placeholder="请选择" clearable>
          <el-option
              v-for="item in [{ label: '请选择', value: '' }, { label: '更新配置', value: '更新配置' }, { label: '删除属性', value: '删除属性' }, { label: '创建目录', value: '创建目录' }, { label: '创建配置', value: '创建配置' }, { label: '批量删除配置', value: '批量删除配置' }, { label: '导入配置', value: '导入配置' }]"
              :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchHistory(historyFormRef)">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="historyData" style="width: 100%" :border="true" table-layout="fixed" :stripe="true" size="small"
              :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column prop="user" label="用户" :show-overflow-tooltip="true" header-align="center" align="center"/>
      <el-table-column prop="operate" label="操作" :show-overflow-tooltip="true" header-align="center" align="center"/>
      <el-table-column prop="fmtOpTime" label="时间" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="page.pageSize" v-model:current-page="page.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="page.total"
                   @size-change="searchHistory(historyFormRef)" @current-change="searchHistory(historyFormRef)"
                   @prev-click="searchHistory(historyFormRef)" @next-click="searchHistory(historyFormRef)" :small="true"
                   :background="true" :page-sizes="[5, 10, 20, 50, 100]"/>
  </div>
</template>

<style scoped>
.container {
  flex-grow: 1;
  padding: 20px 3%;
  overflow: auto;
  width: 80%;

}
</style>