<script lang="ts" setup>
import {Refresh, WarningFilled} from '@element-plus/icons-vue'
import {onMounted, reactive, ref} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
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

const refreshForm = reactive({
  name: '',
})
const appData = ref<MicroService[]>()
const searchApp = () => {
  axios({
    url: '/queryAppInfos/v1.0',
    method: 'post',
    data: refreshForm
  }).then((res: any) => {
    if (res.data.code == 1) {
      appData.value = res.data.body
    } else {
      msg(res.data.msg, 'error')
    }
  })
}
const refreshAppConfig = (scope: any) => {
  axios({
    url: '/refreshConfig/v1.0',
    method: 'post',
    data: {name: scope.row.name}
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('刷新成功', 'success')
    } else {
      msg(res.data.msg, 'error')
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
  <div class="container" v-if="isADMIN">
    <el-form :model="refreshForm" :inline="true" size="small" style="margin-top: 20px">
      <el-form-item label="应用名称：" :label-width="formLabelWidth">
        <el-input v-model="refreshForm.name" type="text"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchApp">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="appData" style="width: 100%" :border="true" table-layout="fixed" :stripe="true" size="small"
              :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" label="操作" width="120" header-align="center" align="center">
        <template #default="scope">
          <el-popconfirm title="确定刷新此应用的配置, 可能需要等待一段时间?" @confirm="refreshAppConfig(scope)"
                         :icon="WarningFilled"
                         confirm-button-type="danger" cancel-button-type="info" icon-color="red">
            <template #reference>
              <el-button :icon="Refresh" size="small" circle title="刷新"/>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="应用名称" :show-overflow-tooltip="true" header-align="center"/>
      <el-table-column prop="desc" label="应用描述" :show-overflow-tooltip="true" header-align="center"/>
      <el-table-column prop="instSize" label="实例数量(包括Down)" :show-overflow-tooltip="true" header-align="center"/>
    </el-table>

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