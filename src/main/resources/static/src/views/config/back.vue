<script lang="ts" setup>
import {
  Delete, Download,
  Edit, Upload, WarningFilled
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed} from 'vue'
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

interface Backup {
  id: string
  backTime: number
  fmtBackTime: string
  size: number
}

const bkDialogFormVisible = ref(false)
const bkPage = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const haForm = reactive({
  time: []
})
const backupData = ref<Backup>()
const haFormRef = ref<FormInstance>()
const haRules = reactive<FormRules>({
  // time: [{required: true, message: '请选择时间', trigger: 'blur'}]
})
const searchBackup = async (formEl: FormInstance | undefined) => {
  console.log('查询备份记录参数: ', haForm)
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/queryBackups/v1.0',
        method: 'post',
        params: bkPage,
        data: haForm
      }).then((res: any) => {
        if (res.data.code == 1) {
          backupData.value = res.data.body.list
          bkPage.total = res.data.body.total
        } else {
          msg('查询失败', 'error')
        }
      })
    } else {
      console.log('error submit!', fields)
    }
  })
}
const backup = () => {
  axios({
    url: '/backup/v1.0',
    method: 'post'
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('备份成功', 'success')
      searchBackup(haFormRef.value)
    } else {
      msg('备份失败', 'error')
    }
  })
}
const recovery = (scope: any) => {
  axios({
    url: '/recovery/v1.0',
    method: 'post',
    data: {id: scope.row.id}
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('恢复成功', 'success')
    } else {
      msg('恢复失败', 'error')
    }
  })
}

const deleteBackup = (scope: any) => {
  axios({
    url: '/deleteBackup/v1.0',
    method: 'post',
    data: {id: scope.row.id}
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('删除成功', 'success')
      searchBackup(haFormRef.value)
    } else {
      msg('删除失败', 'error')
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
  <div class="container" v-if="isADMIN || menuMap['backup']">
    <el-form :model="haForm" :inline="true" ref="haFormRef" :rules="haRules" size="small" style="margin-top: 20px">
      <el-form-item label="时间：" prop="time">
        <el-date-picker v-model="haForm.time" type="daterange" unlink-panels range-separator="至"
                        start-placeholder="开始时间" end-placeholder="结束时间" format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchBackup(haFormRef)">查询</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="success" @click="backup" v-if="isADMIN || menuMap['backup']">
          备份
          <el-icon class="el-icon--right">
            <Download/>
          </el-icon>
        </el-button>
      </el-form-item>
    </el-form>

    <el-table :data="backupData" style="width: 100%" :border="true" table-layout="fixed" :stripe="true" size="small"
              :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" label="操作" width="120" header-align="center" align="center">
        <template #default="scope">
          <el-popconfirm title="是否使用本快照进行恢复?" @confirm="recovery(scope)" :icon="WarningFilled"
                         confirm-button-type="success" cancel-button-type="info" icon-color="green">
            <template #reference>
              <el-button :icon="Upload" size="small" circle title="恢复"/>
            </template>
          </el-popconfirm>
          <el-popconfirm title="是否确认删除此快照数据?" @confirm="deleteBackup(scope)" :icon="WarningFilled"
                         confirm-button-type="danger" cancel-button-type="info" icon-color="red">
            <template #reference>
              <el-button :icon="Delete" size="small" circle title="删除" v-if="isADMIN || menuMap['backup']"/>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="快照名称" width="400" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtBackupTime" label="备份时间" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="size" label="记录数" :show-overflow-tooltip="true" header-align="center" align="center"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="bkPage.pageSize" v-model:current-page="bkPage.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="bkPage.total"
                   @size-change="searchBackup(haFormRef)" @current-change="searchBackup(haFormRef)"
                   @prev-click="searchBackup(haFormRef)" @next-click="searchBackup(haFormRef)" :small="true"
                   :background="true"
                   :page-sizes="[5, 10, 20, 50, 100]"/>
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