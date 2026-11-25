<template>
  <div class="doc-manage">
    <!-- 上传区 -->
    <el-card class="upload-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>📄 上传文档</span>
        </div>
      </template>

      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="（可选）文档标题" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="元数据">
          <el-input
            v-model="form.meta"
            placeholder="（可选）JSON 字符串，例如 {&quot;source&quot;:&quot;manual&quot;}"
          />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="粘贴纯文本或 Markdown 内容..."
            resize="vertical"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="uploading" @click="doUpload">
            {{ uploading ? '上传中...' : '上传并入库' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 文档列表（localStorage 管理） -->
    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>📚 本地上传历史</span>
          <el-tag size="small" effect="plain">仅前端本地记录，后端无 listAll 接口</el-tag>
        </div>
      </template>

      <el-table
        :data="localList"
        stripe
        style="width: 100%"
        empty-text="暂无上传记录"
        v-loading="loading"
      >
        <el-table-column prop="title" label="标题" min-width="160">
          <template #default="{ row }">
            {{ row.title || '（未命名）' }}
          </template>
        </el-table-column>
        <el-table-column prop="docId" label="文档 ID" min-width="240" show-overflow-tooltip />
        <el-table-column prop="time" label="上传时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="primary" @click="viewChunks(row.docId)">查看分片</el-button>
            <el-button size="small" link type="danger" @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分片查看弹窗 -->
    <el-dialog v-model="chunkDialog.visible" :title="'文档分片（' + chunkDialog.docId + '）'" width="80%" top="5vh">
      <el-table
        :data="chunkDialog.list"
        stripe
        height="55vh"
        empty-text="加载中..."
        v-loading="chunkDialog.loading"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="400">
          <template #default="{ row }">
            <div class="chunk-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as docApi from '../api/doc'

const STORAGE_KEY = 'rag_doc_history'

const form = reactive({
  title: '',
  content: '',
  meta: ''
})
const uploading = ref(false)
const loading = ref(false)
const localList = ref([])

const chunkDialog = reactive({
  visible: false,
  docId: '',
  list: [],
  loading: false
})

// 载入本地历史
onMounted(() => {
  loadLocalList()
})

function loadLocalList() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    localList.value = raw ? JSON.parse(raw) : []
  } catch {
    localList.value = []
  }
}

function saveLocalList(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

function resetForm() {
  form.title = ''
  form.content = ''
  form.meta = ''
}

async function doUpload() {
  if (!form.content || !form.content.trim()) {
    ElMessage.warning('文档内容不能为空')
    return
  }
  uploading.value = true
  try {
    const docId = await docApi.upload({
      title: form.title || null,
      content: form.content,
      meta: form.meta || null
    })
    ElMessage.success('上传成功，docId=' + docId)
    localList.value.unshift({
      docId,
      title: form.title || '',
      time: formatTime(new Date())
    })
    saveLocalList(localList.value)
    resetForm()
  } catch {
    // 错误已在 axios 拦截器中提示
  } finally {
    uploading.value = false
  }
}

function viewChunks(docId) {
  chunkDialog.docId = docId
  chunkDialog.list = []
  chunkDialog.visible = true
  chunkDialog.loading = true
  docApi
    .listChunks(docId)
    .then(data => {
      chunkDialog.list = data || []
    })
    .catch(() => {
      chunkDialog.list = []
    })
    .finally(() => {
      chunkDialog.loading = false
    })
}

function confirmDelete(row) {
  ElMessageBox.confirm(
    `确定删除文档「${row.title || row.docId}」及其所有分片？`,
    '删除确认',
    { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
  )
    .then(async () => {
      try {
        const count = await docApi.remove(row.docId)
        ElMessage.success(`已删除 ${count} 个分片`)
        localList.value = localList.value.filter(x => x.docId !== row.docId)
        saveLocalList(localList.value)
      } catch {
        // 错误已提示
      }
    })
    .catch(() => {})
}

function formatTime(d) {
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>

<style scoped>
.doc-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.chunk-content {
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  font-size: 13px;
  line-height: 1.5;
}
</style>
