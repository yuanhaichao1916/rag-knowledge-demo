import { post } from '../utils/request'
import { connectSSE } from '../utils/sse'

// 普通 RAG 问答
export function chat(payload) {
  return post('/api/chat', payload)
}

/**
 * SSE 流式 RAG 问答
 * 后端事件类型：sources / message / done / error
 *
 * @param {object} payload  { question: string, topK?: number }
 * @param {object} callbacks 事件回调：onSources / onMessage / onDone / onError
 * @returns {object} emitter 含 close() 方法
 */
export function chatStream(payload, callbacks) {
  const params = new URLSearchParams()
  params.append('question', payload.question)
  if (payload.topK) params.append('topK', String(payload.topK))
  const url = `/api/chat/stream?${params.toString()}`
  return connectSSE(url, callbacks)
}
