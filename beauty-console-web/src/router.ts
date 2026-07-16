import Vue from 'vue'
import Router from 'vue-router'
import Layout from '@/layout/index.vue'
import { ALL_ROLES } from '@/utils/rolePermissions'

Vue.use(Router)

const Resource = () => import(/* webpackChunkName: "beauty-resource" */ '@/views/beauty/resource.vue')

const router = new Router({
  scrollBehavior: (to, from, savedPosition) => savedPosition || { x: 0, y: 0 },
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/login',
      component: () => import(/* webpackChunkName: "login" */ '@/views/login/index.vue'),
      meta: { title: '美容门店后台', hidden: true, notNeedAuth: true }
    },
    {
      path: '/404',
      component: () => import(/* webpackChunkName: "404" */ '@/views/404.vue'),
      meta: { title: '页面不存在', hidden: true, notNeedAuth: true }
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          component: () => import(/* webpackChunkName: "beauty-dashboard" */ '@/views/beauty/dashboard.vue'),
          name: 'Dashboard',
          meta: { title: '工作台', icon: 'dashboard', affix: true, roles: ALL_ROLES, permission: 'dashboard:view' }
        },
        {
          path: 'appointments',
          component: Resource,
          name: 'Appointments',
          meta: { title: '预约管理', icon: 'icon-order', resource: 'appointments', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'READONLY'], permission: 'appointments:view' }
        },
        {
          path: 'service-orders',
          component: Resource,
          name: 'ServiceOrders',
          meta: { title: '订单管理', icon: 'icon-order', resource: 'serviceOrders', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'FINANCE', 'READONLY'], permission: 'serviceOrders:view' }
        },
        {
          path: 'customers',
          component: Resource,
          name: 'Customers',
          meta: { title: '客户档案', icon: 'icon-user', resource: 'customers', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'FINANCE', 'READONLY'], permission: 'customers:view' }
        },
        {
          path: 'service-projects',
          component: Resource,
          name: 'ServiceProjects',
          meta: { title: '服务项目', icon: 'icon-dish', resource: 'serviceProjects', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'READONLY'], permission: 'serviceProjects:view' }
        },
        {
          path: 'inventory-skus',
          component: Resource,
          name: 'InventorySkus',
          meta: { title: '库存耗材', icon: 'icon-category', resource: 'inventorySkus', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN', 'READONLY'], permission: 'inventorySkus:view' }
        },
        {
          path: 'service-project-inventories',
          component: Resource,
          name: 'ServiceProjectInventories',
          meta: { title: '项目耗材', icon: 'icon-combo', resource: 'serviceProjectInventories', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN', 'READONLY'], permission: 'serviceProjectInventories:view' }
        },
        {
          path: 'inventory-stock-logs',
          component: Resource,
          name: 'InventoryStockLogs',
          meta: { title: '库存流水', icon: 'icon-statistics', resource: 'inventoryStockLogs', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN', 'READONLY'], permission: 'inventoryStockLogs:view' }
        },
        {
          path: 'payment-records',
          component: Resource,
          name: 'PaymentRecords',
          meta: { title: '收款记录', icon: 'icon-order', resource: 'paymentRecords', roles: ['SUPER_ADMIN', 'STORE_MANAGER', 'FINANCE', 'READONLY'], permission: 'paymentRecords:view' }
        },
        {
          path: 'staff-members',
          component: Resource,
          name: 'StaffMembers',
          meta: { title: '员工管理', icon: 'icon-employee', resource: 'staffMembers', roles: ['SUPER_ADMIN', 'STORE_MANAGER'], permission: 'staffMembers:view' }
        },
        {
          path: 'users',
          component: Resource,
          name: 'Users',
          meta: { title: '员工账号与权限', icon: 'icon-employee', resource: 'users', roles: ['SUPER_ADMIN', 'STORE_MANAGER'], permission: 'users:view' }
        }
      ]
    },
    {
      path: '*',
      redirect: '/404',
      meta: { hidden: true }
    }
  ]
})

export default router
