<template>
  <div class="resource-page">
    <div class="page-heading">
      <div>
        <p>{{ config.group }}</p>
        <h1>{{ config.title }}</h1>
      </div>
      <div class="heading-actions">
        <el-button icon="el-icon-refresh" :loading="loading" :disabled="loading" @click="loadData">
          刷新
        </el-button>
        <el-button
          v-if="canCreate"
          type="primary"
          icon="el-icon-plus"
          @click="openCreate"
        >
          新增{{ config.shortTitle || config.title }}
        </el-button>
      </div>
    </div>

    <div class="panel filter-panel">
      <el-form :model="query" inline class="filter-form">
        <el-form-item v-for="field in config.searchFields" :key="field.prop" :label="field.label">
          <el-select
            v-if="field.type === 'select'"
            v-model="query[field.prop]"
            clearable
            :placeholder="field.placeholder || '请选择'"
          >
            <el-option
              v-for="option in field.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <el-date-picker
            v-else-if="field.type === 'datetime'"
            v-model="query[field.prop]"
            type="datetime"
            value-format="yyyy-MM-ddTHH:mm:ss"
            :placeholder="field.placeholder || '选择时间'"
          />
          <el-input
            v-else
            v-model="query[field.prop]"
            clearable
            :placeholder="field.placeholder || '请输入'"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" :loading="loading" @click="handleSearch">
            查询
          </el-button>
          <el-button icon="el-icon-refresh-left" @click="resetSearch">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="panel table-panel">
      <el-table
        v-loading="loading"
        :data="records"
        border
        stripe
        empty-text="暂无数据"
      >
        <el-table-column
          v-for="column in config.columns"
          :key="column.prop"
          :prop="column.prop"
          :label="column.label"
          :min-width="column.width || 120"
          show-overflow-tooltip
        >
          <template slot-scope="{ row }">
            <el-tag
              v-if="column.type === 'tag'"
              :type="tagType(row[column.prop], column)"
              effect="light"
            >
              {{ formatValue(row, column) }}
            </el-tag>
            <span v-else-if="column.type === 'money'" class="money">
              ¥{{ money(row[column.prop]) }}
            </span>
            <span v-else-if="column.type === 'date'">
              {{ formatDate(row[column.prop]) }}
            </span>
            <span v-else>
              {{ formatValue(row, column) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column v-if="hasActions" label="操作" fixed="right" :width="config.actionWidth || 230">
          <template slot-scope="{ row }">
            <el-button
              v-if="canEdit"
              type="text"
              size="mini"
              @click="openEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canStatus"
              type="text"
              size="mini"
              @click="toggleStatus(row)"
            >
              {{ Number(row.status) === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button
              v-for="action in permittedRowActions"
              :key="action.action"
              type="text"
              size="mini"
              @click="runRowAction(action, row)"
            >
              {{ action.label }}
            </el-button>
            <el-button
              v-if="canDelete"
              type="text"
              size="mini"
              class="danger-link"
              @click="removeRow(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="page"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          :disabled="loading"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="620px"
      custom-class="rounded-dialog"
    >
      <el-form ref="formRef" :model="form" label-width="100px" class="resource-form">
        <el-form-item
          v-for="field in visibleFormFields"
          :key="field.prop"
          :label="field.label"
          :prop="field.prop"
          :rules="field.required ? [{ required: true, message: `${field.label}不能为空`, trigger: 'blur' }] : []"
        >
          <el-select
            v-if="field.type === 'select'"
            v-model="form[field.prop]"
            clearable
            :disabled="isFieldDisabled(field)"
            :placeholder="field.placeholder || '请选择'"
          >
            <el-option
              v-for="option in field.options"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
          <el-date-picker
            v-else-if="field.type === 'date'"
            v-model="form[field.prop]"
            type="date"
            value-format="yyyy-MM-dd"
            :disabled="isFieldDisabled(field)"
            :placeholder="field.placeholder || '选择日期'"
          />
          <el-date-picker
            v-else-if="field.type === 'datetime'"
            v-model="form[field.prop]"
            type="datetime"
            value-format="yyyy-MM-ddTHH:mm:ss"
            :disabled="isFieldDisabled(field)"
            :placeholder="field.placeholder || '选择时间'"
          />
          <el-input
            v-else-if="field.type === 'textarea'"
            v-model="form[field.prop]"
            type="textarea"
            :rows="3"
            :disabled="isFieldDisabled(field)"
            :placeholder="field.placeholder || '请输入'"
          />
          <el-input
            v-else
            v-model="form[field.prop]"
            :type="field.type === 'number' ? 'number' : 'text'"
            :disabled="isFieldDisabled(field)"
            :placeholder="field.placeholder || '请输入'"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          保存
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="appointmentItemDialogTitle"
      :visible.sync="appointmentItemDialogVisible"
      width="820px"
      custom-class="rounded-dialog appointment-item-dialog"
    >
      <div v-loading="appointmentItemLoading" class="appointment-item-body">
        <div class="appointment-item-target">
          <div>
            <strong>{{ appointmentItemTarget.appointmentNo || `预约 #${appointmentItemTarget.id || '-'}` }}</strong>
            <span>{{ appointmentItemTarget.customerName || '-' }}</span>
          </div>
          <p>至少添加一个服务项目后，预约才能转为订单。</p>
        </div>

        <el-form label-width="90px" class="appointment-item-form">
          <el-row :gutter="14">
            <el-col :span="10">
              <el-form-item label="服务项目" required>
                <el-select
                  v-model="appointmentItemForm.serviceProjectId"
                  filterable
                  placeholder="按名称选择项目"
                  @change="handleAppointmentProjectChange"
                >
                  <el-option
                    v-for="project in serviceProjectOptions"
                    :key="project.id"
                    :label="`${project.name}（¥${money(project.price)}）`"
                    :value="project.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="成交价" required>
                <el-input v-model="appointmentItemForm.price" type="number" min="0" />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item label="时长(分)">
                <el-input v-model="appointmentItemForm.durationMinutes" type="number" min="0" />
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label-width="0">
                <el-button
                  type="primary"
                  :loading="appointmentItemSubmitting"
                  @click="submitAppointmentItem"
                >
                  {{ appointmentItemEditingId ? '保存' : '添加' }}
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <el-table :data="appointmentItems" border empty-text="还没有项目明细，请在上方添加">
          <el-table-column prop="serviceName" label="服务项目" min-width="180" />
          <el-table-column label="成交价" width="110">
            <template slot-scope="{ row }">
              ¥{{ money(row.price) }}
            </template>
          </el-table-column>
          <el-table-column prop="durationMinutes" label="时长(分钟)" width="110" />
          <el-table-column label="操作" width="130">
            <template slot-scope="{ row }">
              <el-button type="text" size="mini" @click="editAppointmentItem(row)">
                编辑
              </el-button>
              <el-button type="text" size="mini" class="danger-link" @click="removeAppointmentItem(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="appointmentItemDialogVisible = false">
          关闭
        </el-button>
        <el-button
          type="primary"
          :disabled="appointmentItems.length === 0"
          @click="convertAppointmentToOrder"
        >
          转为订单
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="permissionDialogTitle"
      :visible.sync="permissionDialogVisible"
      width="760px"
      custom-class="rounded-dialog permission-dialog"
    >
      <div v-loading="permissionLoading" class="permission-body">
        <div class="permission-target">
          <strong>{{ permissionTarget.username || '-' }}</strong>
          <span>{{ permissionTarget.roleName || permissionTarget.roleCode || '-' }}</span>
        </div>
        <el-checkbox-group
          v-model="permissionCodes"
          class="permission-checks"
          @change="handlePermissionCodesChange"
        >
          <div
            v-for="group in permissionGroups"
            :key="group.name"
            class="permission-group"
          >
            <h4>{{ group.name }}</h4>
            <div class="permission-options">
              <el-checkbox
                v-for="permission in group.items"
                :key="permission.permissionCode"
                :label="permission.permissionCode"
                border
              >
                {{ permission.permissionName || permission.permissionCode }}
              </el-checkbox>
            </div>
          </div>
        </el-checkbox-group>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="restoreRoleDefaultPermissions">
          恢复角色默认
        </el-button>
        <el-button @click="clearPermissionCodes">
          清空权限
        </el-button>
        <el-button @click="permissionDialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" :loading="permissionSubmitting" @click="submitPermissionForm">
          保存权限
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import axios from 'axios'
import { Component, Vue, Watch } from 'vue-property-decorator'
import {
  createOrderFromAppointment,
  createRecord,
  createServiceOrder,
  deleteRecord,
  getOrderIdempotencyToken,
  getUserPermissions,
  listPermissions,
  listRecords,
  patchAction,
  updateRecord,
  updateStatus,
  updateUserPermissions
} from '@/api/beauty'
import { UserModule } from '@/store/modules/user'
import { canUseResourceAction } from '@/utils/rolePermissions'

const enabledOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const levelOptions = [
  { label: '普通', value: 0 },
  { label: '银卡', value: 1 },
  { label: '金卡', value: 2 },
  { label: 'VIP', value: 3 }
]

const appointmentStatusOptions = [
  { label: '待确认', value: 0 },
  { label: '已确认', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 }
]

const orderStatusOptions = [
  { label: '待服务', value: 0 },
  { label: '已完成', value: 1 },
  { label: '已取消', value: 2 }
]

const payStatusOptions = [
  { label: '未支付', value: 0 },
  { label: '部分支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '已退款', value: 3 }
]

const debtStatusOptions = [
  { label: '无欠款', value: 0 },
  { label: '分期中', value: 1 },
  { label: '已结清', value: 2 }
]

const roleOptions = [
  { label: '超级管理员', value: 1 },
  { label: '店长', value: 2 },
  { label: '普通员工', value: 3 },
  { label: '库存管理员', value: 4 },
  { label: '财务/收银', value: 5 },
  { label: '只读', value: 6 }
]

const orderTypeOptions = [
  { label: '服务订单', value: 'service' },
  { label: '次卡', value: 'time_card' },
  { label: '护理卡', value: 'care_card' },
  { label: '会员卡', value: 'member_card' },
  { label: '疗程卡', value: 'course_card' }
]

const paymentMethodOptions = [
  { label: '微信', value: 'wechat' },
  { label: '支付宝', value: 'alipay' },
  { label: '现金', value: 'cash' },
  { label: '次卡', value: 'time_card' }
]

const paymentRecordStatusOptions = [
  { label: '未确认', value: 0 },
  { label: '成功', value: 1 },
  { label: '退款', value: 2 },
  { label: '作废', value: 3 }
]

const changeTypeOptions = [
  { label: '入库', value: 'stock_in' },
  { label: '出库', value: 'stock_out' },
  { label: '盘点', value: 'check' },
  { label: '损耗', value: 'loss' },
  { label: '退回', value: 'return' }
]

const optionMap = (options: any[]) => {
  const map: any = {}
  options.forEach((item: any) => {
    map[item.value] = item.label
  })
  return map
}

const yesNoTag = { 0: 'info', 1: 'success' }
const statusTag = { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }

const resourceConfigs: any = {
  customers: {
    title: '客户档案',
    shortTitle: '客户',
    group: '客户中心',
    endpoint: 'customers',
    creatable: true,
    editable: true,
    deletable: true,
    searchFields: [
      { prop: 'name', label: '客户姓名' },
      { prop: 'phone', label: '手机号' },
      { prop: 'level', label: '客户等级', type: 'select', options: levelOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '客户姓名' },
      { prop: 'phone', label: '手机号' },
      { prop: 'gender', label: '性别', type: 'tag', map: optionMap(genderOptions), tagMap: { 1: 'success', 2: 'danger' } },
      { prop: 'level', label: '等级', type: 'tag', map: optionMap(levelOptions), tagMap: statusTag },
      { prop: 'source', label: '来源' },
      { prop: 'createTime', label: '创建时间', type: 'date', width: 160 }
    ],
    formFields: [
      { prop: 'name', label: '客户姓名', required: true },
      { prop: 'phone', label: '手机号' },
      { prop: 'gender', label: '性别', type: 'select', options: genderOptions },
      { prop: 'birthday', label: '生日', type: 'date' },
      { prop: 'level', label: '客户等级', type: 'select', options: levelOptions, default: 0 },
      { prop: 'source', label: '来源' },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  staffMembers: {
    title: '员工管理',
    shortTitle: '员工',
    group: '人员与权限',
    endpoint: 'staff-members',
    creatable: true,
    editable: true,
    deletable: true,
    statusAction: true,
    statusMode: 'query',
    searchFields: [
      { prop: 'name', label: '员工姓名' },
      { prop: 'phone', label: '手机号' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '员工姓名' },
      { prop: 'phone', label: '手机号' },
      { prop: 'gender', label: '性别', type: 'tag', map: optionMap(genderOptions), tagMap: { 1: 'success', 2: 'danger' } },
      { prop: 'position', label: '岗位' },
      { prop: 'status', label: '状态', type: 'tag', map: optionMap(enabledOptions), tagMap: yesNoTag }
    ],
    formFields: [
      { prop: 'name', label: '员工姓名', required: true },
      { prop: 'phone', label: '手机号' },
      { prop: 'gender', label: '性别', type: 'select', options: genderOptions },
      { prop: 'position', label: '岗位' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions, default: 1, required: true },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  serviceProjects: {
    title: '服务项目',
    shortTitle: '项目',
    group: '服务配置',
    endpoint: 'service-projects',
    creatable: true,
    editable: true,
    deletable: true,
    statusAction: true,
    statusMode: 'query',
    searchFields: [
      { prop: 'name', label: '项目名称' },
      { prop: 'category', label: '分类' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '项目名称' },
      { prop: 'category', label: '分类' },
      { prop: 'price', label: '价格', type: 'money', width: 100 },
      { prop: 'durationMinutes', label: '时长(分钟)', width: 120 },
      { prop: 'status', label: '状态', type: 'tag', map: optionMap(enabledOptions), tagMap: yesNoTag }
    ],
    formFields: [
      { prop: 'name', label: '项目名称', required: true },
      { prop: 'category', label: '分类' },
      { prop: 'price', label: '价格', type: 'number', required: true },
      { prop: 'durationMinutes', label: '时长', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions, default: 1, required: true },
      { prop: 'description', label: '说明', type: 'textarea' }
    ]
  },
  inventorySkus: {
    title: '库存耗材',
    shortTitle: '耗材',
    group: '库存中心',
    endpoint: 'inventory-skus',
    creatable: true,
    editable: true,
    deletable: true,
    statusAction: true,
    statusMode: 'query',
    searchFields: [
      { prop: 'name', label: '耗材名称' },
      { prop: 'category', label: '分类' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'name', label: '耗材名称' },
      { prop: 'category', label: '分类' },
      { prop: 'quantity', label: '当前库存' },
      { prop: 'safetyStock', label: '安全库存' },
      { prop: 'unit', label: '单位', width: 80 },
      { prop: 'costPrice', label: '成本价', type: 'money', width: 100 },
      { prop: 'status', label: '状态', type: 'tag', map: optionMap(enabledOptions), tagMap: yesNoTag }
    ],
    formFields: [
      { prop: 'name', label: '耗材名称', required: true },
      { prop: 'category', label: '分类' },
      { prop: 'unit', label: '单位' },
      { prop: 'quantity', label: '当前库存', type: 'number', required: true },
      { prop: 'safetyStock', label: '安全库存', type: 'number' },
      { prop: 'costPrice', label: '成本价', type: 'number' },
      { prop: 'supplier', label: '供应商' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions, default: 1, required: true },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  serviceProjectInventories: {
    title: '项目耗材',
    shortTitle: '耗材配置',
    group: '服务配置',
    endpoint: 'service-project-inventories',
    creatable: true,
    editable: true,
    deletable: true,
    searchFields: [
      { prop: 'serviceProjectId', label: '项目ID' },
      { prop: 'inventoryId', label: '耗材ID' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'serviceProjectName', label: '服务项目' },
      { prop: 'inventoryName', label: '耗材' },
      { prop: 'consumeQuantity', label: '单次消耗' },
      { prop: 'inventoryUnit', label: '单位', width: 80 },
      { prop: 'status', label: '状态', type: 'tag', map: optionMap(enabledOptions), tagMap: yesNoTag }
    ],
    formFields: [
      { prop: 'serviceProjectId', label: '项目ID', type: 'number', required: true },
      { prop: 'inventoryId', label: '耗材ID', type: 'number', required: true },
      { prop: 'consumeQuantity', label: '单次消耗', type: 'number', required: true },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions, default: 1, required: true },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  appointments: {
    title: '预约管理',
    shortTitle: '预约',
    group: '预约与服务',
    endpoint: 'appointments',
    creatable: true,
    editable: true,
    deletable: true,
    actionWidth: 285,
    rowActions: [
      { label: '项目明细', action: 'items', permissionAction: 'edit' },
      { label: '确认', action: 'confirm', message: '确认该预约？' },
      { label: '取消', action: 'cancel', message: '取消该预约？' },
      { label: '转订单', action: 'toOrder', message: '根据该预约生成服务订单？' }
    ],
    searchFields: [
      { prop: 'customerName', label: '客户姓名' },
      { prop: 'status', label: '状态', type: 'select', options: appointmentStatusOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'appointmentNo', label: '预约号' },
      { prop: 'customerName', label: '客户' },
      { prop: 'staffName', label: '主服务员工' },
      { prop: 'appointmentTime', label: '预约时间', type: 'date', width: 160 },
      { prop: 'status', label: '状态', type: 'tag', map: optionMap(appointmentStatusOptions), tagMap: statusTag }
    ],
    formFields: [
      { prop: 'appointmentNo', label: '预约号' },
      { prop: 'customerId', label: '客户ID', type: 'number', required: true },
      { prop: 'staffId', label: '主员工ID', type: 'number' },
      { prop: 'appointmentTime', label: '预约时间', type: 'datetime', required: true },
      { prop: 'status', label: '状态', type: 'select', options: appointmentStatusOptions, default: 0, required: true },
      { prop: 'totalDurationMinutes', label: '预计时长', type: 'number' },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  serviceOrders: {
    title: '订单管理',
    group: '订单与收款',
    endpoint: 'service-orders',
    creatable: false,
    editable: false,
    deletable: true,
    rowActions: [
      { label: '完成', action: 'finish', message: '完成订单后会自动扣减耗材库存，确认继续？' },
      { label: '取消', action: 'cancel', message: '取消已完成订单会按原出库流水回滚库存，确认继续？' }
    ],
    searchFields: [
      { prop: 'orderNo', label: '订单号' },
      { prop: 'customerName', label: '客户姓名' },
      { prop: 'orderStatus', label: '订单状态', type: 'select', options: orderStatusOptions },
      { prop: 'payStatus', label: '支付状态', type: 'select', options: payStatusOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'orderNo', label: '订单号', width: 150 },
      { prop: 'customerName', label: '客户' },
      { prop: 'orderType', label: '类型', type: 'tag', map: optionMap(orderTypeOptions) },
      { prop: 'receivableAmount', label: '应收', type: 'money', width: 100 },
      { prop: 'paidAmount', label: '已收', type: 'money', width: 100 },
      { prop: 'debtAmount', label: '欠款', type: 'money', width: 100 },
      { prop: 'payStatus', label: '支付', type: 'tag', map: optionMap(payStatusOptions), tagMap: statusTag },
      { prop: 'debtStatus', label: '欠款', type: 'tag', map: optionMap(debtStatusOptions), tagMap: statusTag },
      { prop: 'orderStatus', label: '状态', type: 'tag', map: optionMap(orderStatusOptions), tagMap: statusTag }
    ],
    formFields: []
  },
  paymentRecords: {
    title: '收款记录',
    shortTitle: '收款',
    group: '订单与收款',
    endpoint: 'payment-records',
    creatable: true,
    editable: false,
    deletable: false,
    rowActions: [
      { label: '作废', action: 'void', message: '作废后会同步回滚订单收款金额，确认继续？' }
    ],
    searchFields: [
      { prop: 'orderId', label: '订单ID' },
      { prop: 'paymentMethod', label: '支付方式', type: 'select', options: paymentMethodOptions },
      { prop: 'payStatus', label: '状态', type: 'select', options: paymentRecordStatusOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'orderId', label: '订单ID' },
      { prop: 'paymentNo', label: '流水号', width: 150 },
      { prop: 'paymentMethod', label: '支付方式', type: 'tag', map: optionMap(paymentMethodOptions) },
      { prop: 'payAmount', label: '金额', type: 'money', width: 100 },
      { prop: 'payStatus', label: '状态', type: 'tag', map: optionMap(paymentRecordStatusOptions), tagMap: statusTag },
      { prop: 'payTime', label: '收款时间', type: 'date', width: 160 }
    ],
    formFields: [
      { prop: 'orderId', label: '订单ID', type: 'number', required: true },
      { prop: 'paymentNo', label: '流水号' },
      { prop: 'paymentMethod', label: '支付方式', type: 'select', options: paymentMethodOptions, default: 'wechat', required: true },
      { prop: 'payAmount', label: '收款金额', type: 'number', required: true },
      { prop: 'payStatus', label: '状态', type: 'select', options: paymentRecordStatusOptions, default: 1, required: true },
      { prop: 'payTime', label: '收款时间', type: 'datetime' },
      { prop: 'operatorId', label: '操作人ID', type: 'number' },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  inventoryStockLogs: {
    title: '库存流水',
    shortTitle: '库存流水',
    group: '库存中心',
    endpoint: 'inventory-stock-logs',
    creatable: true,
    editable: false,
    deletable: false,
    searchFields: [
      { prop: 'inventoryId', label: '耗材ID' },
      { prop: 'changeType', label: '类型', type: 'select', options: changeTypeOptions },
      { prop: 'relatedOrderId', label: '订单ID' }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'inventoryName', label: '耗材' },
      { prop: 'changeType', label: '类型', type: 'tag', map: optionMap(changeTypeOptions), tagMap: { stock_in: 'success', stock_out: 'warning', check: 'info', loss: 'danger', return: 'success' } },
      { prop: 'changeQuantity', label: '变动数量' },
      { prop: 'beforeQuantity', label: '变动前' },
      { prop: 'afterQuantity', label: '变动后' },
      { prop: 'relatedOrderId', label: '订单ID' },
      { prop: 'createTime', label: '时间', type: 'date', width: 160 }
    ],
    formFields: [
      { prop: 'inventoryId', label: '耗材ID', type: 'number', required: true },
      { prop: 'changeType', label: '变动类型', type: 'select', options: changeTypeOptions, default: 'stock_in', required: true },
      { prop: 'changeQuantity', label: '变动数量', type: 'number', required: true },
      { prop: 'relatedOrderId', label: '订单ID', type: 'number' },
      { prop: 'operatorId', label: '操作人ID', type: 'number' },
      { prop: 'remark', label: '备注', type: 'textarea' }
    ]
  },
  users: {
    title: '账号权限',
    shortTitle: '账号',
    group: '人员与权限',
    endpoint: 'users',
    creatable: true,
    editable: false,
    deletable: true,
    statusAction: true,
    statusMode: 'body',
    rowActions: [
      { label: '权限', action: 'permissions' }
    ],
    searchFields: [
      { prop: 'username', label: '账号' },
      { prop: 'roleId', label: '角色', type: 'select', options: roleOptions },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions }
    ],
    columns: [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'username', label: '账号' },
      { prop: 'roleName', label: '角色' },
      { prop: 'staffName', label: '关联员工' },
      { prop: 'status', label: '状态', type: 'tag', map: optionMap(enabledOptions), tagMap: yesNoTag },
      { prop: 'lastLoginTime', label: '最后登录', type: 'date', width: 160 }
    ],
    formFields: [
      { prop: 'username', label: '账号', required: true },
      { prop: 'passwordHash', label: '密码', required: true },
      { prop: 'roleId', label: '角色', type: 'select', options: roleOptions, default: 3, required: true },
      { prop: 'staffId', label: '员工ID', type: 'number' },
      { prop: 'status', label: '状态', type: 'select', options: enabledOptions, default: 1, required: true }
    ]
  }
}

@Component({
  name: 'BeautyResource'
})
export default class extends Vue {
  private query: any = {}
  private form: any = {}
  private records: any[] = []
  private loading = false
  private submitting = false
  private orderIdempotencyToken = ''
  private dialogVisible = false
  private mode = 'create'
  private page = 1
  private pageSize = 10
  private total = 0
  private permissionDialogVisible = false
  private permissionLoading = false
  private permissionSubmitting = false
  private permissionTarget: any = {}
  private permissionCodes: string[] = []
  private permissionRoleDefaultCodes: string[] = []
  private permissionRestoreDefault = false
  private permissionGroups: any[] = []
  private appointmentItemDialogVisible = false
  private appointmentItemLoading = false
  private appointmentItemSubmitting = false
  private appointmentItemTarget: any = {}
  private appointmentItems: any[] = []
  private appointmentItemForm: any = {}
  private appointmentItemEditingId: number | string = ''
  private serviceProjectOptions: any[] = []
  private listRequestSource: any = null
  private listRequestSequence = 0

  get resourceKey() {
    const meta = this.$route.meta || {}
    return (meta as any).resource || 'customers'
  }

  get config() {
    return resourceConfigs[this.resourceKey] || resourceConfigs.customers
  }

  get visibleFormFields() {
    const formFields = this.config.formFields || []
    return formFields.filter((field: any) => this.shouldShowField(field))
  }

  get canCreate() {
    return this.config.creatable && this.canUseAction('create')
  }

  get canEdit() {
    return this.config.editable && this.canUseAction('edit')
  }

  get canDelete() {
    return this.config.deletable && this.canUseAction('delete')
  }

  get canStatus() {
    return this.config.statusAction && this.canUseAction('status')
  }

  get permittedRowActions() {
    const rowActions = this.config.rowActions || []
    return rowActions.filter((action: any) => this.canUseAction(action.permissionAction || action.action))
  }

  get hasActions() {
    return this.canEdit || this.canDelete || this.canStatus || this.permittedRowActions.length > 0
  }

  get dialogTitle() {
    return `${this.mode === 'edit' ? '编辑' : '新增'}${this.config.shortTitle || this.config.title}`
  }

  get permissionDialogTitle() {
    return `权限分配 - ${this.permissionTarget.username || ''}`
  }

  get appointmentItemDialogTitle() {
    return `预约项目明细 - ${this.appointmentItemTarget.customerName || ''}`
  }

  mounted() {
    this.resetState()
    this.loadData()
  }

  beforeDestroy() {
    this.listRequestSequence += 1
    this.cancelListRequest('页面已离开')
  }

  @Watch('$route')
  private onRouteChange() {
    this.cancelListRequest('已切换页面')
    this.resetState()
    this.loadData()
  }

  private resetState() {
    this.page = 1
    this.query = this.defaultModel(this.config.searchFields || [])
    this.form = this.defaultModel(this.config.formFields || [])
  }

  private defaultModel(fields: any[]) {
    const model: any = {}
    fields.forEach((field: any) => {
      model[field.prop] = field.default !== undefined ? field.default : ''
    })
    return model
  }

  private async loadData() {
    this.cancelListRequest('已发起更新的查询')
    const requestSequence = ++this.listRequestSequence
    const requestSource = axios.CancelToken.source()
    this.listRequestSource = requestSource
    this.loading = true
    try {
      const params = this.cleanParams({
        ...this.query,
        page: this.page,
        pageSize: this.pageSize
      })
      const { data } = await listRecords(this.config.endpoint, params, requestSource.token)
      if (requestSequence !== this.listRequestSequence) {
        return
      }
      if (Number(data.code) === 200) {
        const payload = data.data || {}
        this.records = payload.records || []
        this.total = Number(payload.total || 0)
      }
    } catch (error) {
      if (!axios.isCancel(error)) {
        throw error
      }
    } finally {
      if (requestSequence === this.listRequestSequence) {
        this.loading = false
        this.listRequestSource = null
      }
    }
  }

  private cancelListRequest(reason: string) {
    if (this.listRequestSource) {
      this.listRequestSource.cancel(reason)
      this.listRequestSource = null
    }
  }

  private cleanParams(source: any) {
    const params: any = {}
    Object.keys(source).forEach((key: string) => {
      const value = source[key]
      if (value !== '' && value !== null && value !== undefined) {
        params[key] = value
      }
    })
    return params
  }

  private handleSearch() {
    this.page = 1
    this.loadData()
  }

  private resetSearch() {
    this.query = this.defaultModel(this.config.searchFields || [])
    this.handleSearch()
  }

  private handleSizeChange(size: number) {
    this.pageSize = size
    this.page = 1
    this.loadData()
  }

  private handleCurrentChange(page: number) {
    this.page = page
    this.loadData()
  }

  private canUseAction(action: string) {
    return canUseResourceAction(this.resourceKey, action, UserModule.roles, UserModule.permissions)
  }

  private openCreate() {
    this.mode = 'create'
    this.orderIdempotencyToken = ''
    this.form = this.defaultModel(this.config.formFields || [])
    this.dialogVisible = true
  }

  private openEdit(row: any) {
    this.mode = 'edit'
    this.orderIdempotencyToken = ''
    const form = this.defaultModel(this.config.formFields || [])
    const formFields = this.config.formFields || []
    formFields.forEach((field: any) => {
      form[field.prop] = row[field.prop] !== undefined && row[field.prop] !== null ? row[field.prop] : form[field.prop]
    })
    form.id = row.id
    this.form = form
    this.dialogVisible = true
  }

  private submitForm() {
    const formRef = this.$refs.formRef as any
    formRef.validate(async (valid: boolean) => {
      if (!valid) {
        return false
      }
      this.submitting = true
      try {
        const payload = this.buildPayload()
        if (this.mode === 'create' && this.config.endpoint === 'service-orders' && !this.orderIdempotencyToken) {
          const tokenResponse = await getOrderIdempotencyToken()
          this.orderIdempotencyToken = tokenResponse.data.data
        }
        const response = this.mode === 'edit'
          ? await updateRecord(this.config.endpoint, this.form.id, payload)
          : this.config.endpoint === 'service-orders'
            ? await createServiceOrder(payload, this.orderIdempotencyToken)
            : await createRecord(this.config.endpoint, payload)
        if (Number(response.data.code) === 200) {
          this.orderIdempotencyToken = ''
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.loadData()
        }
      } finally {
        this.submitting = false
      }
    })
  }

  private buildPayload() {
    const payload: any = {}
    const formFields = this.config.formFields || []
    formFields.forEach((field: any) => {
      if (!this.shouldShowField(field)) {
        return
      }
      const value = this.form[field.prop]
      if (value !== '' && value !== undefined && value !== null) {
        payload[field.prop] = value
      }
    })
    return payload
  }

  private shouldShowField(field: any) {
    return !(this.mode === 'edit' && field.createOnly)
  }

  private isFieldDisabled(field: any) {
    return field.disabled || (this.mode === 'edit' && field.editDisabled)
  }

  private async toggleStatus(row: any) {
    const nextStatus = Number(row.status) === 1 ? 0 : 1
    await this.$confirm(`确认${nextStatus === 1 ? '启用' : '停用'}该记录？`, '提示', { type: 'warning' })
    const response = await updateStatus(this.config.endpoint, row.id, nextStatus, this.config.statusMode || 'query')
    if (Number(response.data.code) === 200) {
      this.$message.success('状态已更新')
      this.loadData()
    }
  }

  private async runRowAction(action: any, row: any) {
    if (action.action === 'permissions') {
      this.openPermissionDialog(row)
      return
    }
    if (action.action === 'items') {
      await this.openAppointmentItemDialog(row)
      return
    }
    if (action.action === 'toOrder') {
      const itemResponse = await listRecords('appointment-items', { appointmentId: row.id })
      const items = itemResponse.data.data || []
      if (items.length === 0) {
        this.$message.warning('请先添加预约项目明细')
        await this.openAppointmentItemDialog(row)
        return
      }
    }
    await this.$confirm(action.message || '确认执行该操作？', '提示', { type: 'warning' })
    const response = action.action === 'toOrder'
      ? await createOrderFromAppointment(row.id)
      : await patchAction(this.config.endpoint, row.id, action.action)
    if (Number(response.data.code) === 200) {
      this.$message.success('操作成功')
      this.loadData()
    }
  }

  private emptyAppointmentItemForm() {
    return {
      serviceProjectId: '',
      price: '',
      durationMinutes: ''
    }
  }

  private async openAppointmentItemDialog(row: any) {
    this.appointmentItemTarget = row
    this.appointmentItemForm = this.emptyAppointmentItemForm()
    this.appointmentItemEditingId = ''
    this.appointmentItems = []
    this.serviceProjectOptions = []
    this.appointmentItemDialogVisible = true
    this.appointmentItemLoading = true
    try {
      const [itemResponse, projectResponse] = await Promise.all([
        listRecords('appointment-items', { appointmentId: row.id }),
        listRecords('service-projects', { page: 1, pageSize: 100, status: 1 })
      ])
      this.appointmentItems = itemResponse.data.data || []
      const projectPayload = projectResponse.data.data || {}
      this.serviceProjectOptions = projectPayload.records || []
    } finally {
      this.appointmentItemLoading = false
    }
  }

  private handleAppointmentProjectChange(projectId: number | string) {
    const project = this.serviceProjectOptions.find((item: any) => String(item.id) === String(projectId))
    if (!project) {
      return
    }
    this.appointmentItemForm.price = project.price
    this.appointmentItemForm.durationMinutes = project.durationMinutes
  }

  private async submitAppointmentItem() {
    const projectId = this.appointmentItemForm.serviceProjectId
    const project = this.serviceProjectOptions.find((item: any) => String(item.id) === String(projectId))
    if (!project) {
      this.$message.warning('请选择服务项目')
      return
    }
    if (this.appointmentItemForm.price === '' || Number(this.appointmentItemForm.price) < 0) {
      this.$message.warning('请输入正确的成交价')
      return
    }

    const payload: any = {
      appointmentId: this.appointmentItemTarget.id,
      serviceProjectId: project.id,
      serviceName: project.name,
      price: Number(this.appointmentItemForm.price),
      durationMinutes: Number(this.appointmentItemForm.durationMinutes || 0),
      sortNo: this.appointmentItemEditingId
        ? Number((this.appointmentItems.find((item: any) => String(item.id) === String(this.appointmentItemEditingId)) || {}).sortNo || 1)
        : this.appointmentItems.length + 1
    }
    if (this.appointmentItemTarget.staffId) {
      payload.staffId = this.appointmentItemTarget.staffId
    }

    this.appointmentItemSubmitting = true
    try {
      const response = this.appointmentItemEditingId
        ? await updateRecord('appointment-items', this.appointmentItemEditingId, payload)
        : await createRecord('appointment-items', payload)
      if (Number(response.data.code) === 200) {
        this.$message.success(this.appointmentItemEditingId ? '项目明细已更新' : '项目明细已添加')
        this.appointmentItemEditingId = ''
        this.appointmentItemForm = this.emptyAppointmentItemForm()
        await this.loadAppointmentItems()
      }
    } finally {
      this.appointmentItemSubmitting = false
    }
  }

  private editAppointmentItem(row: any) {
    this.appointmentItemEditingId = row.id
    this.appointmentItemForm = {
      serviceProjectId: row.serviceProjectId,
      price: row.price,
      durationMinutes: row.durationMinutes
    }
  }

  private async removeAppointmentItem(row: any) {
    await this.$confirm(`确认删除项目“${row.serviceName || row.serviceProjectId}”？`, '提示', { type: 'warning' })
    const response = await deleteRecord('appointment-items', row.id)
    if (Number(response.data.code) === 200) {
      this.$message.success('项目明细已删除')
      if (String(this.appointmentItemEditingId) === String(row.id)) {
        this.appointmentItemEditingId = ''
        this.appointmentItemForm = this.emptyAppointmentItemForm()
      }
      await this.loadAppointmentItems()
    }
  }

  private async loadAppointmentItems() {
    const response = await listRecords('appointment-items', { appointmentId: this.appointmentItemTarget.id })
    this.appointmentItems = response.data.data || []
  }

  private async convertAppointmentToOrder() {
    if (this.appointmentItems.length === 0) {
      this.$message.warning('请至少添加一个服务项目')
      return
    }
    await this.$confirm('确认根据当前预约项目生成服务订单？', '提示', { type: 'warning' })
    const response = await createOrderFromAppointment(this.appointmentItemTarget.id)
    if (Number(response.data.code) === 200) {
      this.$message.success('订单生成成功')
      this.appointmentItemDialogVisible = false
      this.loadData()
    }
  }

  private async openPermissionDialog(row: any) {
    this.permissionTarget = row
    this.permissionDialogVisible = true
    this.permissionLoading = true
    this.permissionCodes = []
    this.permissionRoleDefaultCodes = []
    this.permissionRestoreDefault = false
    this.permissionGroups = []
    try {
      const [permissionResponse, userPermissionResponse] = await Promise.all([
        listPermissions(),
        getUserPermissions(row.id)
      ])
      const userPayload = userPermissionResponse.data.data || {}
      const allPermissions = (userPayload.allPermissions && userPayload.allPermissions.length)
        ? userPayload.allPermissions
        : ((permissionResponse.data || {}).data || [])
      this.permissionGroups = this.groupPermissions(allPermissions)
      this.permissionCodes = [...(userPayload.permissionCodes || [])]
      this.permissionRoleDefaultCodes = [...(userPayload.rolePermissionCodes || [])]
      this.permissionRestoreDefault = !Boolean(userPayload.customized)
    } finally {
      this.permissionLoading = false
    }
  }

  private restoreRoleDefaultPermissions() {
    this.permissionCodes = [...this.permissionRoleDefaultCodes]
    this.$nextTick(() => {
      this.permissionRestoreDefault = true
    })
  }

  private clearPermissionCodes() {
    this.permissionCodes = []
    this.permissionRestoreDefault = false
  }

  private handlePermissionCodesChange() {
    this.permissionRestoreDefault = false
  }

  private groupPermissions(permissions: any[]) {
    const groups: any = {}
    ;(permissions || []).forEach((permission: any) => {
      const groupName = permission.permissionGroup || '其他'
      if (!groups[groupName]) {
        groups[groupName] = []
      }
      groups[groupName].push(permission)
    })
    return Object.keys(groups).map((name: string) => ({
      name,
      items: groups[name]
    }))
  }

  private async submitPermissionForm() {
    if (!this.permissionTarget.id) {
      return
    }
    this.permissionSubmitting = true
    try {
      const response = await updateUserPermissions(
        this.permissionTarget.id,
        this.permissionRestoreDefault ? [] : this.permissionCodes,
        this.permissionRestoreDefault
      )
      if (Number(response.data.code) === 200) {
        this.$message.success('权限已保存')
        this.permissionDialogVisible = false
      }
    } finally {
      this.permissionSubmitting = false
    }
  }

  private async removeRow(row: any) {
    await this.$confirm('确认删除该记录？', '提示', { type: 'warning' })
    const response = await deleteRecord(this.config.endpoint, row.id)
    if (Number(response.data.code) === 200) {
      this.$message.success('删除成功')
      if (this.records.length === 1 && this.page > 1) {
        this.page -= 1
      }
      this.loadData()
    }
  }

  private formatValue(row: any, column: any) {
    const value = row[column.prop]
    if (value === '' || value === null || value === undefined) {
      return '-'
    }
    if (column.map) {
      return column.map[value] || value
    }
    return value
  }

  private formatDate(value: any) {
    if (!value) {
      return '-'
    }
    return String(value).replace('T', ' ').slice(0, 19)
  }

  private tagType(value: any, column: any) {
    if (column.tagMap && column.tagMap[value]) {
      return column.tagMap[value]
    }
    return 'info'
  }

  private money(value: any) {
    return Number(value || 0).toFixed(2)
  }
}
</script>

<style lang="scss" scoped>
.resource-page {
  padding: 24px;
}

.page-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 18px;

  p {
    margin: 0 0 8px;
    color: #d99732;
    font-size: 13px;
    font-weight: 600;
  }

  h1 {
    margin: 0;
    color: #20242f;
    font-size: 26px;
    line-height: 34px;
    letter-spacing: 0;
  }
}

.heading-actions {
  display: flex;
  gap: 10px;
}

.panel {
  background: #ffffff;
  border: 1px solid #edf0f5;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(31, 38, 53, 0.06);
}

.filter-panel {
  padding: 18px 18px 2px;
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0 10px;

  .el-form-item {
    margin-right: 0;
  }
}

.table-panel {
  padding: 18px;
}

.money {
  color: #20242f;
  font-weight: 600;
}

.danger-link {
  color: #e16c7a;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.resource-form {
  .el-select,
  .el-date-editor,
  .el-input {
    width: 100%;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.permission-body {
  min-height: 260px;
}

.appointment-item-body {
  min-height: 260px;
}

.appointment-item-target {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  margin-bottom: 18px;
  border: 1px solid #edf0f5;
  border-radius: 16px;
  background: #fbfcfe;

  div {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  strong {
    color: #20242f;
  }

  span,
  p {
    color: #7b8496;
    font-size: 13px;
  }

  p {
    margin: 0;
  }
}

.appointment-item-form {
  .el-select,
  .el-input {
    width: 100%;
  }
}

.permission-target {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #edf0f5;
  border-radius: 16px;
  margin-bottom: 16px;
  background: #fbfcfe;

  strong {
    color: #20242f;
    font-size: 15px;
  }

  span {
    color: #7b8496;
    font-size: 13px;
  }
}

.permission-checks {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.permission-group {
  border: 1px solid #edf0f5;
  border-radius: 18px;
  padding: 14px;

  h4 {
    margin: 0 0 12px;
    color: #20242f;
    font-size: 14px;
  }
}

.permission-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .el-checkbox.is-bordered {
    margin: 0;
    border-radius: 12px;
  }
}
</style>

<style lang="scss">
.resource-page,
.beauty-dashboard {
  .el-button {
    border-radius: 12px;
  }

  .el-button--primary {
    box-shadow: 0 10px 20px rgba(217, 151, 50, 0.18);
  }

  .el-input__inner,
  .el-textarea__inner {
    border-radius: 12px;
  }

  .el-table {
    border-radius: 16px;
    overflow: hidden;
  }

  .el-table th {
    background: #f8fafc;
    color: #4a5263;
    font-weight: 600;
  }

  .el-tag {
    border-radius: 999px;
  }
}

.rounded-dialog {
  border-radius: 20px;
  overflow: hidden;

  .el-dialog__header {
    padding: 22px 24px 12px;
    background: #ffffff;
  }

  .el-dialog__body {
    padding: 18px 26px;
  }

  .el-dialog__footer {
    padding: 14px 26px 24px;
  }
}
</style>
