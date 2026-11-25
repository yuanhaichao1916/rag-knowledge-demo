import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一请求封装
// baseURL 为空时走 Vite proxy，开发模式把 /api 转发到后端 8080
const instance = axios.create({
  baseURL: '',
  timeout: 60000
})

// 请求拦截器
instance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('userToken')
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一解构后端 { code, msg, data }
instance.interceptors.response.use(
  response => {
    const res = response.data
    // 如果后端直接返回数组或字符串（非标准 Result 结构），原样返回
    if (res == null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (res.code === 200) {
      return res.data
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    const msg = error?.response?.data?.msg || error?.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export const get = (url, params, config = {}) =>
  instance.get(url, { params, ...config })

export const post = (url, data, config = {}) =>
  instance.post(url, data, config)

export const del = (url, config = {}) => instance.delete(url, config)

export default instance
