import { get, post, del } from '../utils/request'

// 文档上传
export function upload(payload) {
  return post('/api/doc/upload', payload)
}

// 查询文档全部分片（不含 embedding）
export function listChunks(docId) {
  return get(`/api/doc/${docId}/chunks`)
}

// 删除文档及其全部分片
export function remove(docId) {
  return del(`/api/doc/${docId}`)
}
