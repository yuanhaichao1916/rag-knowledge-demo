<template>
  <div class="agent-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>🧪 Agent 工具调试台</span>
          <el-tag size="small" effect="plain">Spring AI @Tool 自主调用</el-tag>
        </div>
      </template>

      <!-- 预置问题 -->
      <div class="preset-area">
        <div class="preset-label">预置问题（点击快速填入）：</div>
        <div class="preset-buttons">
          <el-button
            v-for="(q, idx) in presetQuestions"
            :key="idx"
            size="small"
            @click="input = q"
          >
            {{ q }}
          </el-button>
        </div>
      </div>

      <!-- 对话区 -->
      <div class="chat-area">
        <div class="message-list">
          <div v-if="messages.length === 0" class="empty-hint">
            <div>Agent 会自主判断是否调用后端 @Tool 工具</div>
            <div class="sub">例如：「现在几点？」触发 getCurrentTime，「xxx docId 有多少分片？」触发 countChunksByDocId</div>
          </div>
          <div v-for="(m, idx) in messages" :key="idx" class="msg-row" :class="m.role">
            <div class="avatar">{{ m.role === 'user' ? '🙂' : '🤖' }}</div>
            <div class="bubble">
              <div v-if="m.loading" class="loading-dots">
                <span></span><span></span><span></span>
              </div>
              <div v-else>{{ m.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入 -->
      <div class="input-area">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          placeholder="输入问题，Ctrl+Enter 发送"
          resize="none"
          @keydown.ctrl.enter="send"
        />
        <div class="actions">
          <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          <el-button @click="clearAll">清空</el-button>
        </div>
      </div>
    </el-card>

    <!-- 当前注册的工具列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>🛠️ 后端注册的 Agent 工具</span>
          <el-tag size="small" type="info" effect="plain">Spring AI M6 工具调用轨迹后端未暴露，仅展示最终回答</el-tag>
        </div>
      </template>
      <el-table :data="toolList" border size="small">
        <el-table-column prop="name" label="工具名" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="params" label="参数" width="200" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as agentApi from '../api/agent'

const presetQuestions = [
  '现在几点？',
  '请列出当前可用的工具',
  '你会回答哪些问题？'
]

const toolList = [
  {
    name: 'getCurrentTime',
    description: '获取当前服务器时间（yyyy-MM-dd HH:mm:ss）',
    params: '无'
  },
  {
    name: 'countChunksByDocId',
    description: '根据文档 ID 查询分片总数',
    params: 'docId: String'
  }
]

const input = ref('')
const sending = ref(false)
const messages = ref([])

async function send() {
  const q = input.value.trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }
  if (sending.value) return
  sending.value = true
  messages.value.push({ role: 'user', content: q })
  messages.value.push({ role: 'assistant', content: '', loading: true })
  input.value = ''
  try {
    const answer = await agentApi.chat({ question: q })
    const last = messages.value[messages.value.length - 1]
    last.content = answer || '（无内容）'
    last.loading = false
  } catch {
    const last = messages.value[messages.value.length - 1]
    last.content = '[调用失败，请检查后端]'
    last.loading = false
  } finally {
    sending.value = false
  }
}

function clearAll() {
  messages.value = []
}
</script>

<style scoped>
.agent-page {
  max-width: 960px;
  margin: 0 auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.preset-area {
  margin-bottom: 16px;
}
.preset-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
.preset-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chat-area {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  height: 360px;
  overflow-y: auto;
  padding: 16px;
  margin-bottom: 16px;
  background: #fafbfc;
}
.empty-hint {
  text-align: center;
  color: #909399;
  margin-top: 40px;
  font-size: 14px;
}
.empty-hint .sub {
  margin-top: 8px;
  font-size: 12px;
  color: #c0c4cc;
}
.msg-row {
  display: flex;
  margin-bottom: 12px;
}
.msg-row.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #fff;
  border: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  flex-shrink: 0;
}
.bubble {
  max-width: 70%;
  margin: 0 10px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.msg-row.user .bubble {
  background: #409eff;
  color: #fff;
}
.msg-row.assistant .bubble {
  background: #fff;
  border: 1px solid #ebeef5;
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
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dot-bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}
.input-area .actions {
  margin-top: 8px;
  text-align: right;
}
</style>
