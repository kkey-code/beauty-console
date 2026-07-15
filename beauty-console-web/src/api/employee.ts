import request from '@/utils/request'

export const login = (data: any) =>
  request({
    url: '/admin/users/login',
    method: 'post',
    data
  })

export const userLogout = () => Promise.resolve({ data: { code: 200, message: '操作成功', data: null } })

export const getEmployeeList = (params: any) =>
  request({
    url: '/admin/staff-members',
    method: 'get',
    params
  })

export const enableOrDisableEmployee = (params: any) =>
  request({
    url: `/admin/staff-members/${params.id}/status`,
    method: 'patch',
    params: { status: params.status }
  })

export const addEmployee = (params: any) =>
  request({
    url: '/admin/staff-members',
    method: 'post',
    data: params
  })

export const editEmployee = (params: any) =>
  request({
    url: `/admin/staff-members/${params.id}`,
    method: 'put',
    data: params
  })

export const queryEmployeeById = (id: string | (string | null)[]) =>
  request({
    url: `/admin/staff-members/${id}`,
    method: 'get'
  })
