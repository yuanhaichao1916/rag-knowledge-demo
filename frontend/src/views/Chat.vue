<template>
  <div class="chat-page">
    <!-- 左侧聊天区 -->
    <div class="chat-main">
      <div class="chat-header">
        <span class="chat-title">💬 RAG 智能问答</span>
        <el-tag size="small" :type="ragEnabled ? 'success' : 'info'" effect="plain">
          {{ ragEnabled ? 'RAG 已启用' : 'RAG 已关闭（裸问 LLM）' }}
        </el-tag>
      </div>

      <div class="message-list" ref="listRef">
        <div v-if="messages.length === 0" class="empty-hint">
          <div>知识库问答助手</div>
          <div class="sub">上传文档后，在此提问即可获取基于知识的回答</div>
        </div>

        <div v-for="(msg, idx) in messages" :key="idx" class="message-row" :class="msg.role">
          <div class="avatar">{{ msg.role === 'user' ? '🙂' : '🤖' }}</div>
          <div class="bubble">
            <div v-if="msg.loading" class="loading-dots">
              <span></span><span></span><span></span>
            </div>
            <div v-else class="msg-text" v-html="formatText(msg.content)"></div>
            <div v-if="msg.sources && msg.sources.length" class="sources-preview" @click="toggleSources(idx)">
              📎 引用片段 × {{ msg.sources.length }}
            </div>
            <div v-if="msg.content?.includes('知识库未收录相关信息')" class="no-knowledge-tip">
              ⚠️ 当前知识库未收录相关内容
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-area">
        <div class="input-toolbar">
          <span class="toolbar-label">检索 topK：</span>
          <el-select v-model.number="topK" size="small" style="width: 90px">
            <el-option label="3" :value="3" />
            <el-option label="5" :value="5" />
            <el-option label="8" :value="8" />
            <el-option label="10" :value="10" />
          </el-select>
          <el-switch
            v-model="useStream"
            active-text="流式"
            inactive-text="普通"
            style="margin-left: 12px"
          />
          <el-button size="small" text @click="clearAll" style="margin-left: auto">
            清空对话
          </el-button>
        </div>
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          placeholder="输入问题，Ctrl+Enter 发送"
          resize="none"
          @keydown.ctrl.enter="send"
        />
        <div class="input-actions">
          <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          <el-button :disabled="!sending" @click="stopStream">停止</el-button>
        </div>
      </div>
    </div>

    <!-- 右侧引用片段面板 -->
    <el-aside width="360px" class="sources-panel">
      <div class="sources-header">📎 引用片段</div>
      <div class="sources-list">
        <div v-if="!currentSources.length" class="empty-hint small">
          暂无引用片段<br />普通问答结束后可见
        </div>
        <div v-for="(s, idx) in currentSources" :key="idx" class="source-item">
          <div class="source-title">
            <span>{{ s.title || '（未命名）' }}</span>
            <el-tag size="small" type="info" effect="plain" v-if="s.score">
              相似度 {{ s.score.toFixed(4) }}
            </el-tag>
          </div>
          <div class="source-content">{{ s.content }}</div>
          <div class="source-footer">
            <el-button size="small" link @click="copyText(s.content)">复制片段</el-button>
          </div>
        </div>
      </div>
    </el-aside>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as chatApi from '../api/chat'

// 运行时可在 application.yml 里关，但前端无法直接读到，先硬编码默认 true
const ragEnabled = ref(true)
const topK = ref(5)
const useStream = ref(true)
const input = ref('')
const sending = ref(false)
const messages = ref([])
const currentSources = ref([])
const listRef = ref(null)

let sseEmitter = null

onBeforeUnmount(() => {
  stopStream()
})

function scrollToBottom() {
  nextTick(() => {
    const el = listRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function addMsg(msg) {
  messages.value.push(msg)
  scrollToBottom()
}

function updateLastAssistant(content) {
  const last = [...messages.value].reverse().find(m => m.role === 'assistant')
  if (last) last.content = (last.content || '') + content
  scrollToBottom()
}

async function send() {
  const question = input.value.trim()
  if (!question) {
    ElMessage.warning('请输入问题')
    return
  }
  if (sending.value) return
  sending.value = true

  addMsg({ role: 'user', content: question })
  input.value = ''

  if (useStream.value) {
    // SSE 流式
    addMsg({ role: 'assistant', content: '', loading: true, sources: [] })
    const assistantMsg = messages.value[messages.value.length - 1]
    sseEmitter = chatApi.chatStream(
      { question, topK: topK.value },
      {
        onSources(list) {
          currentSources.value = list || []
          assistantMsg.sources = list || []
        },
        onMessage(delta) {
          assistantMsg.loading = false
          assistantMsg.content += delta
          scrollToBottom()
        },
        onDone() {
          assistantMsg.loading = false
          sending.value = false
          sseEmitter = null
        },
        onError(err) {
          assistantMsg.loading = false
          assistantMsg.content += `\n\n**[错误]** ${err}`
          sending.value = false
          sseEmitter = null
        }
      }
    )
  } else {
    // 普通问答
    try {
      const res = await chatApi.chat({ question, topK: topK.value })
      currentSources.value = res.sources || []
      addMsg({
        role: 'assistant',
        content: res.answer || '',
        sources: res.sources || []
      })
    } catch {
      addMsg({ role: 'assistant', content: '[问答失败，请检查后端或网络]' })
    } finally {
      sending.value = false
    }
  }
}

function stopStream() {
  if (sseEmitter) {
    sseEmitter.close()
    sseEmitter = null
    sending.value = false
    const last = [...messages.value].reverse().find(m => m.role === 'assistant')
    if (last) last.loading = false
  }
}

function clearAll() {
  stopStream()
  messages.value = []
  currentSources.value = []
}

function toggleSources(idx) {
  const msg = messages.value[idx]
  if (msg.sources && msg.sources.length) {
    currentSources.value = msg.sources
  }
}

function copyText(text) {
  navigator.clipboard.writeText(text).then(
    () => ElMessage.success('已复制到剪贴板'),
    () => ElMessage.error('复制失败')
  )
}

// 简易文本格式化：**粗体**、代码块保持原样
function formatText(text) {
  if (!text) return ''
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
}
</script>

<style scoped>
.chat-page {
  display: flex;
  height: calc(100vh - 120px);
  gap: 16px;
}
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fafbfc;
}
.chat-title {
  font-weight: 600;
  font-size: 15px;
}
.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f7fa;
}
.empty-hint {
  text-align: center;
  color: #909399;
  margin-top: 60px;
  font-size: 15px;
}
.empty-hint.small {
  margin-top: 40px;
  font-size: 13px;
}
.empty-hint .sub {
  margin-top: 8px;
  font-size: 13px;
  color: #c0c4cc;
}
.message-row {
  display: flex;
  margin-bottom: 16px;
}
.message-row.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.bubble {
  max-width: 70%;
  margin: 0 12px;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}
.message-row.user .bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.message-row.assistant .bubble {
  background: #fff;
  border: 1px solid #ebeef5;
  border-bottom-left-radius: 4px;
}
.loading-dots {
  display: inline-flex;
  gap: 4px;
}
.loading-dots span {
  width: 6px;
  height: 6px;
  background: #909399;
  border-radius: 50%;
  animation: dot-bounce 1.2s infinite;
}
.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}
.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}
.sources-preview {
  margin-top: 8px;
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
}
.no-knowledge-tip {
  margin-top: 8px;
  padding: 6px 10px;
  background: #fdf6ec;
  border-radius: 4px;
  font-size: 12px;
  color: #e6a23c;
}
.input-area {
  padding: 12px 20px 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}
.input-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}
.toolbar-label {
  margin-right: 4px;
}
.input-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 右侧引用片段面板 */
.sources-panel {
  background: #fff;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}
.sources-header {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  font-size: 14px;
  background: #fafbfc;
}
.sources-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}
.source-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 10px;
  background: #fafbfc;
}
.source-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 6px;
  color: #303133;
}
.source-content {
  font-size: 12px;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 260px;
  overflow-y: auto;
  line-height: 1.5;
}
.source-footer {
  margin-top: 6px;
  text-align: right;
}
</style>
