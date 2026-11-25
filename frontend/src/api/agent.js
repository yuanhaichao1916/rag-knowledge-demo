import { post } from '../utils/request'

// Agent 问答（工具调用由大模型自主决策）
export function chat(payload) {
  return post('/api/agent/chat', payload)
}
