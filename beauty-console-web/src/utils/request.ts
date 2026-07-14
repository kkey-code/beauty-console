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
    if (body.status === 401 || body.code === 401) {
      router.replace('/login')
      return response
    }
    if (body.code === 0) {
      Message.error(body.msg || '操作失败')
    }
    return response
  },
  (error: any) => {
    if (error && error.response && error.response.status === 401) {
      router.replace('/login')
    } else {
      Message.error(error.message || '网络请求失败')
    }
    return Promise.reject(error)
  }
)

export default service
