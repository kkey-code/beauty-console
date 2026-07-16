import { Route, RouteConfig } from 'vue-router'

export const ALL_ROLES = ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'INVENTORY_ADMIN', 'FINANCE', 'READONLY']
const ADMIN_ROLES = ['SUPER_ADMIN', 'STORE_MANAGER']

const ROLE_ALIASES: any = {
  ADMIN: 'SUPER_ADMIN',
  SUPER_ADMIN: 'SUPER_ADMIN',
  MANAGER: 'STORE_MANAGER',
  STORE_MANAGER: 'STORE_MANAGER',
  STAFF: 'STAFF',
  INVENTORY: 'INVENTORY_ADMIN',
  INVENTORY_ADMIN: 'INVENTORY_ADMIN',
  FINANCE: 'FINANCE',
  READONLY: 'READONLY'
}

const normalizeRole = (role: any) => {
  const value = String(role || '')
  return ROLE_ALIASES[value] || ROLE_ALIASES[value.toUpperCase()] || value.toUpperCase()
}

const normalizePermissions = (permissions: string[] = []) =>
  (permissions || []).filter(Boolean).map((item: string) => String(item))

export const hasPermission = (permissions: string[] = [], permissionCode: string) =>
  normalizePermissions(permissions).includes(permissionCode)

export const currentRole = (roles: string[] = []) => normalizeRole(roles[0]) || 'READONLY'

export const hasAnyRole = (roles: string[] = [], allowedRoles: string[] = ALL_ROLES) => {
  const role = currentRole(roles)
  return allowedRoles.map(normalizeRole).includes(role)
}

export const canVisitRoute = (route: Route | RouteConfig, roles: string[], permissions: string[] = []) => {
  const meta: any = route.meta || {}
  const permissionCodes = normalizePermissions(permissions)
  const roleAllowed = !meta.roles || hasAnyRole(roles, meta.roles)
  const permissionAllowed = !meta.permission || permissionCodes.includes(meta.permission)
  return roleAllowed && permissionAllowed
}

export const filterRoutesByRole = (routes: RouteConfig[], roles: string[], permissions: string[] = []) =>
  routes
    .filter((route: RouteConfig) => canVisitRoute(route, roles, permissions))
    .map((route: RouteConfig) => {
      const nextRoute: RouteConfig = { ...route }
      if (route.children) {
        nextRoute.children = filterRoutesByRole(route.children, roles, permissions)
      }
      return nextRoute
    })

const resourceActionPermissionCodes: any = {
  customers: {
    view: 'customers:view',
    create: 'customers:create',
    edit: 'customers:edit',
    delete: 'customers:delete'
  },
  staffMembers: {
    view: 'staffMembers:view',
    create: 'staffMembers:create',
    edit: 'staffMembers:edit',
    status: 'staffMembers:status',
    delete: 'staffMembers:delete'
  },
  serviceProjects: {
    view: 'serviceProjects:view',
    create: 'serviceProjects:create',
    edit: 'serviceProjects:edit',
    status: 'serviceProjects:status',
    delete: 'serviceProjects:delete'
  },
  inventorySkus: {
    view: 'inventorySkus:view',
    create: 'inventorySkus:create',
    edit: 'inventorySkus:edit',
    status: 'inventorySkus:status',
    delete: 'inventorySkus:delete'
  },
  serviceProjectInventories: {
    view: 'serviceProjectInventories:view',
    create: 'serviceProjectInventories:create',
    edit: 'serviceProjectInventories:edit',
    delete: 'serviceProjectInventories:delete'
  },
  inventoryStockLogs: {
    view: 'inventoryStockLogs:view',
    create: 'inventoryStockLogs:create'
  },
  appointments: {
    view: 'appointments:view',
    create: 'appointments:create',
    edit: 'appointments:edit',
    delete: 'appointments:delete',
    confirm: 'appointments:confirm',
    cancel: 'appointments:cancel',
    toOrder: 'appointments:toOrder'
  },
  appointmentItems: {
    view: 'appointmentItems:view',
    create: 'appointmentItems:create',
    edit: 'appointmentItems:edit',
    delete: 'appointmentItems:delete'
  },
  serviceOrders: {
    view: 'serviceOrders:view',
    create: 'serviceOrders:create',
    edit: 'serviceOrders:edit',
    delete: 'serviceOrders:delete',
    finish: 'serviceOrders:finish',
    cancel: 'serviceOrders:cancel'
  },
  paymentRecords: {
    view: 'paymentRecords:view',
    create: 'paymentRecords:create',
    void: 'paymentRecords:void'
  },
  users: {
    view: 'users:view',
    create: 'users:create',
    edit: 'users:edit',
    status: 'users:status',
    delete: 'users:delete',
    permissions: 'users:permissions',
    rolePermissions: 'roles:permissions',
    resetPassword: 'users:resetPassword'
  }
}

const resourcePermissions: any = {
  customers: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'FINANCE', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    edit: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    delete: ADMIN_ROLES
  },
  staffMembers: {
    view: ADMIN_ROLES,
    create: ADMIN_ROLES,
    edit: ADMIN_ROLES,
    status: ADMIN_ROLES,
    delete: ADMIN_ROLES
  },
  serviceProjects: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'READONLY'],
    create: ADMIN_ROLES,
    edit: ADMIN_ROLES,
    status: ADMIN_ROLES,
    delete: ADMIN_ROLES
  },
  inventorySkus: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN'],
    edit: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN'],
    status: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN'],
    delete: ADMIN_ROLES
  },
  serviceProjectInventories: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN'],
    edit: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN'],
    delete: ADMIN_ROLES
  },
  inventoryStockLogs: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'INVENTORY_ADMIN']
  },
  appointments: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    edit: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    delete: ADMIN_ROLES,
    confirm: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    cancel: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    toOrder: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF']
  },
  appointmentItems: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    edit: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    delete: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF']
  },
  serviceOrders: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF', 'FINANCE', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    edit: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    delete: ADMIN_ROLES,
    finish: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF'],
    cancel: ['SUPER_ADMIN', 'STORE_MANAGER', 'STAFF']
  },
  paymentRecords: {
    view: ['SUPER_ADMIN', 'STORE_MANAGER', 'FINANCE', 'READONLY'],
    create: ['SUPER_ADMIN', 'STORE_MANAGER', 'FINANCE'],
    void: ['SUPER_ADMIN', 'STORE_MANAGER', 'FINANCE']
  },
  users: {
    view: ADMIN_ROLES,
    create: ADMIN_ROLES,
    edit: ADMIN_ROLES,
    status: ADMIN_ROLES,
    delete: ADMIN_ROLES,
    permissions: ADMIN_ROLES,
    rolePermissions: ADMIN_ROLES,
    resetPassword: ADMIN_ROLES
  }
}

export const canUseResourceAction = (
  resource: string,
  action: string,
  roles: string[],
  permissions: string[] = []
) => {
  const permissionCodes = normalizePermissions(permissions)
  const permissionCode = (resourceActionPermissionCodes[resource] || {})[action]
  const config = resourcePermissions[resource]
  if (!config) {
    return false
  }
  const roleAllowed = hasAnyRole(roles, config[action] || [])
  const permissionAllowed = !permissionCode || permissionCodes.includes(permissionCode)
  return roleAllowed && permissionAllowed
}
