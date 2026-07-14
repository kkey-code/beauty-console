<template>
  <div class="beauty-dashboard">
    <div class="dashboard-hero">
      <div>
        <p>门店经营概览</p>
        <h1>今天先看预约、订单和库存风险</h1>
      </div>
      <el-button type="primary" icon="el-icon-refresh" @click="loadData">
        刷新数据
      </el-button>
    </div>

    <div v-loading="loading" class="metric-grid">
      <div v-for="item in metrics" :key="item.label" class="metric-card">
        <span :class="['metric-icon', item.tone]"><i :class="item.icon" /></span>
        <div>
          <p>{{ item.label }}</p>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div v-if="canView('serviceOrders')" class="panel">
        <div class="panel-title">
          <strong>待服务订单</strong>
          <el-button type="text" @click="$router.push('/service-orders')">
            查看全部
          </el-button>
        </div>
        <el-table :data="pendingOrders" size="small" empty-text="暂无待服务订单">
          <el-table-column prop="orderNo" label="订单号" min-width="130" />
          <el-table-column prop="customerName" label="客户" min-width="90" />
          <el-table-column prop="receivableAmount" label="应收" width="90">
            <template slot-scope="{ row }">
              ¥{{ money(row.receivableAmount) }}
            </template>
          </el-table-column>
          <el-table-column prop="orderStatusName" label="状态" width="90" />
        </el-table>
      </div>

      <div v-if="canView('appointments')" class="panel">
        <div class="panel-title">
          <strong>近期预约</strong>
          <el-button type="text" @click="$router.push('/appointments')">
            查看全部
          </el-button>
        </div>
        <el-table :data="appointments" size="small" empty-text="暂无预约">
          <el-table-column prop="appointmentNo" label="预约号" min-width="120" />
          <el-table-column prop="customerName" label="客户" min-width="90" />
          <el-table-column prop="appointmentTime" label="预约时间" min-width="150" />
          <el-table-column prop="statusName" label="状态" width="90" />
        </el-table>
      </div>
    </div>

    <div class="dashboard-grid">
      <div v-if="canView('inventorySkus')" class="panel">
        <div class="panel-title">
          <strong>库存预警</strong>
          <el-button type="text" @click="$router.push('/inventory-skus')">
            处理库存
          </el-button>
        </div>
        <el-table :data="lowStockItems" size="small" empty-text="暂无低库存耗材">
          <el-table-column prop="name" label="耗材" min-width="120" />
          <el-table-column prop="quantity" label="当前库存" width="100" />
          <el-table-column prop="safetyStock" label="安全库存" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
        </el-table>
      </div>

      <div class="panel quick-panel">
        <div class="panel-title">
          <strong>快捷入口</strong>
        </div>
        <div class="quick-grid">
          <button v-for="item in visibleQuickLinks" :key="item.path" @click="$router.push(item.path)">
            <i :class="item.icon" />
            <span>{{ item.label }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { listRecords } from '@/api/beauty'
import { UserModule } from '@/store/modules/user'
import { canUseResourceAction } from '@/utils/rolePermissions'

@Component({
  name: 'BeautyDashboard'
})
export default class extends Vue {
  private loading = false
  private pendingOrders: any[] = []
  private appointments: any[] = []
  private lowStockItems: any[] = []
  private metrics = [
    { label: '客户总数', value: 0, icon: 'el-icon-user', tone: 'gold' },
    { label: '预约总数', value: 0, icon: 'el-icon-date', tone: 'rose' },
    { label: '订单总数', value: 0, icon: 'el-icon-tickets', tone: 'blue' },
    { label: '库存耗材', value: 0, icon: 'el-icon-box', tone: 'green' }
  ]

  private quickLinks = [
    { label: '新增预约', path: '/appointments', icon: 'el-icon-date', resource: 'appointments', action: 'create' },
    { label: '订单管理', path: '/service-orders', icon: 'el-icon-tickets', resource: 'serviceOrders', action: 'view' },
    { label: '客户档案', path: '/customers', icon: 'el-icon-user', resource: 'customers', action: 'view' },
    { label: '库存耗材', path: '/inventory-skus', icon: 'el-icon-box', resource: 'inventorySkus', action: 'view' }
  ]

  get visibleQuickLinks() {
    return this.quickLinks.filter((item: any) => this.canViewOrAction(item.resource, item.action))
  }

  mounted() {
    this.loadData()
  }

  private async loadData() {
    this.loading = true
    try {
      const [customers, appointments, orders, inventory, pendingOrders, appointmentRows, inventoryRows] = await Promise.all([
        this.fetchIfAllowed('customers', 'customers', { page: 1, pageSize: 1 }),
        this.fetchIfAllowed('appointments', 'appointments', { page: 1, pageSize: 1 }),
        this.fetchIfAllowed('serviceOrders', 'service-orders', { page: 1, pageSize: 1 }),
        this.fetchIfAllowed('inventorySkus', 'inventory-skus', { page: 1, pageSize: 1 }),
        this.fetchIfAllowed('serviceOrders', 'service-orders', { page: 1, pageSize: 5, orderStatus: 0 }),
        this.fetchIfAllowed('appointments', 'appointments', { page: 1, pageSize: 5 }),
        this.fetchIfAllowed('inventorySkus', 'inventory-skus', { page: 1, pageSize: 5, lowStockOnly: true })
      ])

      this.metrics = [
        { ...this.metrics[0], value: this.totalOf(customers) },
        { ...this.metrics[1], value: this.totalOf(appointments) },
        { ...this.metrics[2], value: this.totalOf(orders) },
        { ...this.metrics[3], value: this.totalOf(inventory) }
      ]
      this.pendingOrders = this.recordsOf(pendingOrders)
      this.appointments = this.recordsOf(appointmentRows)
      this.lowStockItems = this.recordsOf(inventoryRows)
    } finally {
      this.loading = false
    }
  }

  private fetchIfAllowed(resourceKey: string, endpoint: string, params: any) {
    if (!this.canView(resourceKey)) {
      return Promise.resolve(null)
    }
    return listRecords(endpoint, params)
  }

  private canView(resourceKey: string) {
    return this.canViewOrAction(resourceKey, 'view')
  }

  private canViewOrAction(resourceKey: string, action: string) {
    return canUseResourceAction(resourceKey, action, UserModule.roles, UserModule.permissions)
  }

  private totalOf(response: any) {
    return Number((((response || {}).data || {}).data || {}).total || 0)
  }

  private recordsOf(response: any) {
    return ((((response || {}).data || {}).data || {}).records || []) as any[]
  }

  private money(value: any) {
    return Number(value || 0).toFixed(2)
  }
}
</script>

<style lang="scss" scoped>
.beauty-dashboard {
  padding: 24px;
}

.dashboard-hero {
  min-height: 148px;
  border-radius: 24px;
  padding: 28px 32px;
  background:
    linear-gradient(135deg, rgba(217, 151, 50, 0.96), rgba(225, 108, 122, 0.88)),
    linear-gradient(45deg, #fff4df, #ffe7ef);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  p {
    margin: 0 0 10px;
    font-size: 14px;
    opacity: 0.86;
  }

  h1 {
    margin: 0;
    font-size: 28px;
    line-height: 36px;
    letter-spacing: 0;
  }

  .el-button {
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.92);
    border: 0;
    color: #9b5a08;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.metric-card,
.panel {
  background: #ffffff;
  border: 1px solid #edf0f5;
  border-radius: 20px;
  box-shadow: 0 12px 32px rgba(31, 38, 53, 0.06);
}

.metric-card {
  min-height: 118px;
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;

  p {
    margin: 0 0 8px;
    color: #8a92a6;
    font-size: 13px;
  }

  strong {
    color: #20242f;
    font-size: 28px;
    line-height: 34px;
  }
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;

  &.gold { background: #fff3d8; color: #b06a00; }
  &.rose { background: #ffe9ed; color: #c94f60; }
  &.blue { background: #e8f0ff; color: #3a7bd5; }
  &.green { background: #e6f8ef; color: #1f9f62; }
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.panel {
  padding: 18px;
}

.panel-title {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;

  strong {
    font-size: 16px;
    color: #20242f;
  }
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;

  button {
    height: 74px;
    border: 1px solid #edf0f5;
    background: #f8fafc;
    border-radius: 18px;
    color: #303644;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    cursor: pointer;
    font-size: 14px;

    i {
      color: #d99732;
      font-size: 20px;
    }

    &:hover {
      background: #fff8ed;
      border-color: #f4d6a5;
    }
  }
}
</style>
