import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'
import { UserModule } from '@/store/modules/user'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 600000
})

service.interceptors.request.use(
  (config: any) => {
    if (UserModule.token) {
      config.headers.token = UserModule.token
    }
    return config
  },
  (error: any) => Promise.reject(error)
)

service.interceptors.response.use(
  (response: any) => {
    const body = response.data || {}
    const code = Number(body.code)
    if (response.status === 401 || code === 401) {
      router.replace('/login')
      return response
    }
    if (body.code !== undefined && code !== 200) {
      Message.error(body.message || '操作失败')
    }
    return response
  },
  (error: any) => {
    const response = error && error.response
    const body = (response && response.data) || {}
    if (response && response.status === 401) {
      router.replace('/login')
    }
    Message.error(body.message || error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default service
