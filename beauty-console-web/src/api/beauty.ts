import request from '@/utils/request'

export const listRecords = (resource: string, params: any) =>
  request({
    url: `/admin/${resource}`,
    method: 'get',
    params
  })

export const getRecord = (resource: string, id: number | string) =>
  request({
    url: `/admin/${resource}/${id}`,
    method: 'get'
  })

export const createRecord = (resource: string, data: any) =>
  request({
    url: `/admin/${resource}`,
    method: 'post',
    data
  })

export const updateRecord = (resource: string, id: number | string, data: any) =>
  request({
    url: `/admin/${resource}/${id}`,
    method: 'put',
    data
  })

export const deleteRecord = (resource: string, id: number | string) =>
  request({
    url: `/admin/${resource}/${id}`,
    method: 'delete'
  })

export const patchAction = (resource: string, id: number | string, action: string, data?: any, params?: any) =>
  request({
    url: `/admin/${resource}/${id}/${action}`,
    method: 'patch',
    data,
    params
  })

export const updateStatus = (
  resource: string,
  id: number | string,
  status: number,
  mode: 'body' | 'query' = 'query'
) =>
  request({
    url: `/admin/${resource}/${id}/status`,
    method: 'patch',
    data: mode === 'body' ? { status } : undefined,
    params: mode === 'query' ? { status } : undefined
  })

export const createOrderFromAppointment = (appointmentId: number | string) =>
  request({
    url: `/admin/service-orders/from-appointment/${appointmentId}`,
    method: 'post'
  })

export const listPermissions = () =>
  request({
    url: '/admin/permissions',
    method: 'get'
  })

export const getUserPermissions = (id: number | string) =>
  request({
    url: `/admin/users/${id}/permissions`,
    method: 'get'
  })

export const updateUserPermissions = (
  id: number | string,
  permissionCodes: string[],
  useRoleDefault = false
) =>
  request({
    url: `/admin/users/${id}/permissions`,
    method: 'put',
    data: { permissionCodes, useRoleDefault }
  })
