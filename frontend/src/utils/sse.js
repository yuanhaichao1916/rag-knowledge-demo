import { SSE } from 'sse.js'

/**
 * SSE 客户端封装
 * 统一处理后端事件类型：sources / message / done / error
 *
 * @param {string} url      SSE 地址（相对路径即可，走 Vite proxy）
 * @param {object} callbacks 事件回调
 * @param {Function} callbacks.onSources  sources 事件回调，参数为后端返回的 sources 数组
 * @param {Function} callbacks.onMessage  message 事件回调，参数为增量文本
 * @param {Function} callbacks.onDone    done 事件回调，无参数
 * @param {Function} callbacks.onError   error 事件回调，参数为错误信息
 * @param {object} options  可选配置：payload（POST 体）、reconnect（是否自动重连，默认 true）
 * @returns {object} emitter 含 close() 方法
 */
export function connectSSE(url, callbacks, options = {}) {
  const payload = options.payload || null
  const reconnect = options.reconnect !== false

  const sse = payload
    ? new SSE(url, {
        payload: JSON.stringify(payload),
        headers: { 'Content-Type': 'application/json' },
        reconnect
      })
    : new SSE(url, { reconnect })

  // 统一处理 4 种事件
  sse.addEventListener('sources', e => {
    try {
      const sources = JSON.parse(e.data)
      callbacks.onSources && callbacks.onSources(sources)
    } catch {
      callbacks.onSources && callbacks.onSources([])
    }
  })

  sse.addEventListener('message', e => {
    callbacks.onMessage && callbacks.onMessage(e.data)
  })

  sse.addEventListener('done', () => {
    callbacks.onDone && callbacks.onDone()
    sse.close()
  })

  sse.addEventListener('error', e => {
    callbacks.onError && callbacks.onError(e.data || 'SSE 异常')
    sse.close()
  })

  // 兜底：连接失败时触发 error
  sse.onerror = e => {
    if (e.type === 'error') {
      callbacks.onError && callbacks.onError('SSE 连接失败')
    }
  }

  return {
    close: () => sse.close()
  }
}

export default connectSSE
