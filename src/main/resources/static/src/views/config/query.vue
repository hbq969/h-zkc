<script lang="ts" setup>
import {
  Delete,
  Edit, WarningFilled
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import {ElMessage, ElMessageBox} from 'element-plus'
import type {FormInstance, FormRules, TableInstance} from 'element-plus'
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

interface PropertyInfo {
  path: string
  name: string
  value: string
  strValue: string
}

const searchDialogFormVisible = ref(false)
const tableRef = ref<TableInstance>()
const searchData = ref<PropertyInfo[]>()
const searchForm = reactive({
  path: '',
  name: '',
  value: '',
})
const searchProperty = () => {
  console.log('点击树节点查询配置列表:', searchForm)
  axios({
    url: '/queryAllProperty/v1.0',
    method: 'post',
    data: searchForm
  }).then((res: any) => {
    if (res.data.code == 1) {
      searchData.value = res.data.body
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const propForm = reactive({
  zkPath: '',
  name: '',
  value: ''
})
const dialogFormVisible = ref(false)
const updateProperty = (scope: any) => {
  console.log('更新配置: %o', scope.row)
  propForm.zkPath = scope.row.path
  propForm.name = scope.row.name
  propForm.value = scope.row.strValue
  dialogFormVisible.value = true
}
const deleteProperty = (scope: any) => {
  console.log('删除配置: %o', scope)
  axios({
    url: '/deleteLeaves/v1.0',
    method: 'post',
    data: {
      zkPath: scope.row.path,
      leafNames: [scope.row.name]
    }
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('删除成功', 'success')
      searchProperty()
    } else {
      msg(res.data.msg, 'error')
    }
  })
}

const updateConfig = () => {
  axios({
    url: '/updateProperty/v1.0',
    method: 'post',
    data: propForm
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('更新成功', 'success')
      dialogFormVisible.value = false
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

const dialogFormVisible2=ref(false)
const batchUpdateForm=reactive({
  configKey: '',
  configValue:''
})
const batchData=reactive({
  list: []
})
const showBatchUpdateDialog = () => {
  let rows = tableRef.value?.getSelectionRows();
  if (!rows || rows.length == 0) {
    ElMessageBox.alert('请选择需要批量修改的配置', '标题', {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
    })
    return
  }
  batchData.list=rows
  dialogFormVisible2.value=true
  let row=rows[0]
  batchUpdateForm.configKey=row.name
  batchUpdateForm.configValue=row.strValue
}
const formRef2 = ref<FormInstance>();
const rules2 = reactive<FormRules>({
  configKey: [{required: true, message: '不能为空', trigger: 'blur'}],
  configValue: [{required: true, message: '不能为空', trigger: 'blur'}]
})

const batchUpdateConfig = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      batchData.list.forEach(item=>{
        item.value=batchUpdateForm.configValue
        item.zkPath=item.path
      })
      axios({
          url: '/updateProperty/batch/v1.0',
          method: 'post',
          data: batchData.list,
        }).then((res: any) => {
          if (res.data.code == 1) {
            msg('更新成功', 'success')
            dialogFormVisible2.value = false
            searchProperty()
          } else {
            msg(res.data.errorMessage, 'warning')
          }
        }).catch((err: Error) => {
          msg('请求异常', 'error')
        })
    }
  })
}

const batchDeleteConfig=()=>{
  let rows = tableRef.value?.getSelectionRows();
  if (!rows || rows.length == 0) {
    ElMessageBox.alert('请选择需要批量修改的配置', '标题', {
      confirmButtonText: 'OK',
      type: 'warning',
      showClose: false
    })
    return
  }
  rows.forEach(item=>{
    item.zkPath=item.path
    item.leafNames=[]
    item.leafNames.push(item.name)
  })
  axios({
      url: '/deleteLeaves/batch/v1.0',
      method: 'post',
      data: rows,
    }).then((res: any) => {
      if (res.data.code == 1) {
        msg('更新成功', 'success')
        searchProperty()
      } else {
        msg(res.data.errorMessage, 'warning')
      }
    }).catch((err: Error) => {
      msg('请求异常', 'error')
    })
}

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
    <el-form :model="searchForm" :inline="true" size="small" style="margin-top: 20px">
      <el-form-item label="目录：">
        <el-input v-model="searchForm.path" type="text"/>
      </el-form-item>
      <el-form-item label="属性名：">
        <el-input v-model="searchForm.name" type="text"/>
      </el-form-item>
      <el-form-item label="属性值：">
        <el-input v-model="searchForm.value" type="text"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchProperty">查询</el-button>
        <el-button type="warning" @click="showBatchUpdateDialog" v-if="isADMIN">批量修改</el-button>
        <el-popconfirm title="确认要删除这些配置吗?" confirm-button-type="danger" @confirm="batchDeleteConfig">
            <template #reference>
              <el-button type="danger" v-if="isADMIN">批量删除</el-button>
            </template>
        </el-popconfirm>
      </el-form-item>
    </el-form>

    <el-table ref="tableRef" :data="searchData" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small"
              :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column type="selection" header-align="center" align="center" v-if="isADMIN"/>
      <el-table-column fixed="left" label="操作" width="120" header-align="center" align="center" v-if="isADMIN">
        <template #default="scope">
          <el-button :icon="Edit" circle size="small" title="编辑" @click="updateProperty(scope)"/>
          <el-popconfirm title="你确定要删除本条配置吗?" @confirm="deleteProperty(scope)" :icon="WarningFilled"
                         confirm-button-type="danger" cancel-button-type="info" icon-color="red">
            <template #reference>
              <el-button :icon="Delete" circle size="small" title="删除"/>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="目录" :show-overflow-tooltip="true" header-align="center"/>
      <el-table-column prop="name" label="属性名" :show-overflow-tooltip="true" header-align="center"/>
      <el-table-column prop="strValue" label="属性值" :show-overflow-tooltip="true" header-align="center"/>
    </el-table>

    <el-dialog v-model="dialogFormVisible" title="编辑属性">
      <el-form :model="propForm" size="small">
        <el-form-item label="属性名：">
          <el-input v-model="propForm.name" :type="propForm.name.length>50?'textarea':'text'" rows="3" disabled/>
        </el-form-item>
        <el-form-item label="属性值：">
          <el-input v-model="propForm.value" type="textarea" :rows="7"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="updateConfig">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible2" title="批量修改配置" draggable>
      <el-form :model="batchUpdateForm" label-position="right" size="small" :inline="false" ref="formRef2"
               :rules="rules2"
               label-width="20%">
        <el-form-item label="属性名：" prop="configKey">
          <el-input v-model="batchUpdateForm.configKey" type="text" disabled/>
        </el-form-item>
        <el-form-item label="属性值：" prop="configValue">
          <el-input v-model="batchUpdateForm.configValue" type="text"/>
        </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogFormVisible2 = false">取消</el-button>
            <el-popconfirm title="确认新增系统注册信息?" confirm-button-type="warning" @confirm="batchUpdateConfig(formRef2)">
                <template #reference>
                  <el-button type="primary">保存</el-button>
                </template>
            </el-popconfirm>
          </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.container {
  flex-grow: 1;
  padding: 20px 3%;
  overflow: auto;
  width: 80%;

}
</style>s