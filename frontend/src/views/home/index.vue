<template>
  <div class="__container_home_index">
    <!-- 顶部导航 -->
    <header class="app-header">
      <div class="logo">
        <span class="logo-icon">✈️</span>
        <div>
          <div class="logo-title">Funnair</div>
          <div class="logo-sub">智能机票助手</div>
        </div>
      </div>
      <div class="header-actions">
        <button class="theme-btn" @click="toggleTheme" :title="theme === 'dark' ? '切换浅色' : '切换暗色'">
          <span v-if="theme === 'dark'">☀️</span>
          <span v-else>🌙</span>
        </button>
        <a-button class="btn-ghost">历史记录</a-button>
        <a-button type="primary">新建对话</a-button>
      </div>
    </header>

    <!-- 主体两栏 -->
    <main class="app-body">
      <!-- 左侧：聊天 -->
      <section class="chat-panel">
        <div class="panel-head">
          <div class="panel-title">
            <span class="dot"></span>
            AI 助手
          </div>
          <span class="panel-tip">在线</span>
        </div>

        <div class="chat-scroll" ref="chatScrollRef">
          <MessageList :list="messageInfo.list":is-streaming="isStreaming" />
          <div id="chat-body-id" style="height: 1px;"></div>
        </div>

        <!-- 快捷提问按钮 -->
         <div class="quick-asks" v-if="messageInfo.list.length <= 1">
          <span
          v-for="(tip, i) in quickTips"
          :key="i"
          class="chip"
          @click="quickAsk(tip.text)"
        >
          {{ tip.label }}
          </span>
         </div>

        <div class="input-bar">
          <a-input
            v-model:value="question"
            @keydown.enter="forHelp"
            placeholder="描述你的行程，例如：帮我找下周一北京到上海最便宜的航班"
            size="large"
            :disabled="lock"
            class="chat-input"
          />
          <a-button
            type="primary"
            size="large"
            @click="forHelp"
            :disabled="lock"
            :loading="lock"
            class="send-btn"
          >
            发送
          </a-button>
        </div>
      </section>

      <!-- 右侧：预订信息 -->
      <section class="booking-panel">
        <div class="panel-head">
          <div class="panel-title">机票预订信息</div>
          <span class="badge">{{ bookingInfo.dataSource.length }} 条</span>
        </div>

        <!-- 空状态 -->
        <div v-if="bookingInfo.dataSource.length === 0" class="empty">
          <div class="empty-icon">🛫</div>
          <div class="empty-title">还没有预订信息</div>
          <div class="empty-desc">在左侧和 AI 助手对话，开始你的行程吧</div>
        </div>

        <a-table
          v-else
          :data-source="bookingInfo.dataSource"
          :columns="bookingInfo.columns"
          :pagination="false"
          row-key="bookingNumber"
          class="booking-table"
        >       
          <template #bodyCell="{ record, index, column, text }">
            
            <template v-if="column.dataIndex === 'bookingStatus'">
              <span class="status-tag" :class="text === 'CONFIRMED' ? 'confirmed' : 'cancelled'">
                <span class="status-dot"></span>
                {{ text === 'CONFIRMED' ? '已确认' : '已取消' }}
              </span>
            </template>

            <!-- 舱位列 -->
            <template v-else-if="column.dataIndex === 'bookingClass'">
              <span class="class-tag" :class="text.toLowerCase()">
                {{ classText(text) }}
              </span>
            </template>
          </template>
        </a-table>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from "vue";
import { getBookings } from "@/api/service/booking";
import MessageList from "@/views/home/MessageList.vue";
import type { MessageItem } from "@/types/message";
import { v4 as uuidv4 } from "uuid";
import { message } from "ant-design-vue";
import { useTheme } from "@/composables/useTheme";

const messageInfo: { cur: MessageItem | null; list: MessageItem[] } = reactive({
  cur: null,
  list: [
    { role: "assistant", content: "欢迎来到 Funnair! 请问有什么可以帮您的?" },
  ],
});

const bookingInfo = reactive({
  dataSource: [] as any[],
  columns: [
    { title: "#", dataIndex: "bookingNumber", key: "bookingNumber", width: 60 },
    { title: "乘客", dataIndex: "name", key: "name" },
    { title: "日期", dataIndex: "date", key: "date" },
    { title: "出发", dataIndex: "from", key: "from" },
    { title: "到达", dataIndex: "to", key: "to" },
    { title: "状态", dataIndex: "bookingStatus", key: "bookingStatus" },
    { title: "舱位", dataIndex: "bookingClass", key: "bookingClass" },
  ],
});

const question = ref("");
const lock = ref(false);
const chatScrollRef = ref<HTMLElement | null>(null);
const chatId = uuidv4();
const isStreaming = ref(false); 
const { theme, toggleTheme } = useTheme();

// 快捷提问数据
const quickTips = [
  { label: "🔍 查询张伟的订单", text: "帮我查询张伟的航班预订信息" },
  { label: "💼 找商务舱", text: "有哪些商务舱的航班" },
  { label: "📅 最近的航班", text: "最近一班航班是哪一天" },
  { label: "📋 退票规则", text: "退票的规则是什么" },
];

// 点击快捷提问
function quickAsk(text: string) {
  question.value = text;
  // 用 nextTick 确保 question 已更新后再发送
  import("vue").then(({ nextTick }) => {
    nextTick(() => forHelp());
  });
}

function scrollBottom() {
  nextTick(() => {
    chatScrollRef.value?.scrollTo({
      top: chatScrollRef.value.scrollHeight,
      behavior: "smooth",
    });
  });
}

function formatTime() {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function addMessage(role: "user" | "assistant", content: string) {
  const cur: any = { role, content, time: formatTime() };
  messageInfo.cur = cur;
  messageInfo.list.push(cur);
  scrollBottom();
}

function appendMessage(content: string) {
  if (messageInfo.cur) messageInfo.cur.content += content;
  scrollBottom();
}

function forHelp() {
  if (lock.value) { message.warn("助手正在生成, 请耐心等候"); return; }
  const userMessage = question.value.trim();
  if (!userMessage) return;

  addMessage("user", userMessage);
  question.value = "";

  const eventSource = new EventSource(
    `/api/assistant/chat?chatId=${chatId}&userMessage=${encodeURIComponent(userMessage)}`
  );
  eventSource.onopen = () => { lock.value = true; isStreaming.value = true; addMessage("assistant", ""); };
  eventSource.onmessage = (event) => { appendMessage(event.data); };
  eventSource.onerror = () => {
    eventSource.close();
    bookings();
    lock.value = false;
    isStreaming.value = false;
  };
}

function bookings() {
  getBookings({}).then((res) => { bookingInfo.dataSource = res; });
}

function classText(cls: string) {
  const map: Record<string, string> = {
    ECONOMY: "经济舱",
    PREMIUM_ECONOMY: "超级经济舱",
    BUSINESS: "商务舱",
    FIRST: "头等舱",
  };
  return map[cls] || cls;
}

onMounted(() => { bookings(); });
</script>

<style lang="less" scoped>
.__container_home_index {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
  overflow: hidden;
}

/* 顶部导航 */
.app-header {
  height: 64px;
  flex-shrink: 0;
  padding: 0 28px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logo { display: flex; align-items: center; gap: 12px; }
  .logo-icon { font-size: 26px; }
  .logo-title { font-size: 16px; font-weight: 700; line-height: 1.2; }
  .logo-sub { font-size: 12px; color: var(--text-sub); }
  .header-actions { display: flex; gap: 10px; }
}

/* 主体 */
.app-body {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1.35fr;
  gap: 20px;
  padding: 20px;
  overflow: hidden;
}

.chat-panel,
.booking-panel {
  background: var(--bg-card);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-head {
  height: 56px;
  flex-shrink: 0;
  padding: 0 20px;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.panel-tip {
  font-size: 12px;
  color: #16A34A;
  background: linear-gradient(135deg, #DCFCE7 0%, #BBF7D0 100%);
  padding: 3px 12px;
  border-radius: 999px;
  font-weight: 500;
}
/* ========== 暗色模式：在线徽章 ========== */
:root[data-theme="dark"] .panel-tip {
  background: linear-gradient(135deg, #064E3B 0%, #065F46 100%);
  color: #6EE7B7;
}

.badge {
  font-size: 12px;
  background: var(--primary-light);
  color: var(--primary);
  padding: 2px 10px;
  border-radius: 999px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34,197,94,.18);
  animation: pulse 1.6s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .55; }
}

.chat-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: var(--bg-chat);
}

/* 快捷提问 */
.quick-asks {
  flex-shrink: 0;
  padding: 12px 20px 0;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  background: var(--bg-card);
  border-top: 1px solid var(--border);
}

.chip {
  padding: 6px 14px;
  background: #F3F4F6;
  border-radius: 999px;
  font-size: 13px;
  color: #4B5563;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  white-space: nowrap;
}

.chip:hover {
  background: var(--primary-light);
  color: var(--primary);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(79, 70, 229, 0.15);
}

.chip:active {
  transform: translateY(0);
}
/* ========== 暗色模式：快捷提问胶囊 ========== */
:root[data-theme="dark"] .chip {
  background: var(--bg-hover);
  color: var(--text-sub);
  border-color: var(--border);
}
:root[data-theme="dark"] .chip:hover {
  background: var(--primary-light);
  color: #A5B4FC;
  border-color: var(--primary);
}

.input-bar {
  flex-shrink: 0;
  padding: 12px 20px 16px;
  /*border-top: 1px solid var(--border);*/
  background: var(--bg-card);
  display: flex;
  gap: 12px;
  align-items: center;

  .chat-input { flex: 1; border-radius: 10px; }
    .send-btn {
      border-radius: 10px;
      min-width: 88px;
      height: 40px;
      background: var(--primary-gradient) !important;
      border: none !important;
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25) !important;
      font-weight: 500;
  
      &:hover:not(:disabled) {
        box-shadow: 0 6px 18px rgba(79, 70, 229, 0.35) !important;
        transform: translateY(-1px);
      }
  
      &:disabled {
        background: #D1D5DB !important;
        box-shadow: none !important;
      }
    }
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  padding: 40px;
}
.empty-icon { font-size: 56px; margin-bottom: 16px; opacity: .75; }
.empty-title { font-size: 15px; font-weight: 500; color: var(--text-main); }
.empty-desc { font-size: 13px; margin-top: 6px; }

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  line-height: 1;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.status-tag.confirmed {
  background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%);
  color: #059669;
  border-color: #A7F3D0;
}
.status-tag.confirmed .status-dot {
  background: #10B981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

.status-tag.cancelled {
  background: linear-gradient(135deg, #FEF2F2 0%, #FEE2E2 100%);
  color: #DC2626;
  border-color: #FECACA;
}
.status-tag.cancelled .status-dot {
  background: #EF4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15);
}

.status-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.booking-table {
  padding: 0 20px 20px;
}
.booking-table :deep(.ant-table-thead > tr > th) {
  background: transparent !important;
  color: var(--text-sub) !important;
  font-weight: 500 !important;
  font-size: 13px !important;
  border-bottom: 1px solid var(--border) !important;
}
.booking-table :deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid var(--border) !important;
  font-size: 13px;
  padding: 14px 16px !important;
}
/* ========== 暗色模式：表格 ========== */
:root[data-theme="dark"] .booking-table :deep(.ant-table) {
  background: transparent;
  color: var(--text-main);
}
:root[data-theme="dark"] .booking-table :deep(.ant-table-thead > tr > th) {
  background: transparent !important;
  color: var(--text-sub) !important;
  border-bottom: 1px solid var(--border) !important;
}
:root[data-theme="dark"] .booking-table :deep(.ant-table-tbody > tr > td) {
  background: transparent !important;
  color: var(--text-main) !important;
  border-bottom: 1px solid var(--border) !important;
}
:root[data-theme="dark"] .booking-table :deep(.ant-table-tbody > tr:hover > td) {
  background: var(--bg-hover) !important;
}

.header-actions :deep(.ant-btn-primary) {
  background: var(--primary-gradient) !important;
  border: none !important;
}

.class-tag {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  border: 1px solid transparent;
}

.class-tag.economy {
  background: #F0F9FF;
  color: #0369A1;
  border-color: #BAE6FD;
}

.class-tag.premium_economy {
  background: #F5F3FF;
  color: #6D28D9;
  border-color: #DDD6FE;
}

.class-tag.business {
  background: #FEF3C7;
  color: #B45309;
  border-color: #FDE68A;
}

.class-tag.first {
  background: #FCE7F3;
  color: #BE185D;
  border-color: #FBCFE8;
}

.theme-btn {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: var(--bg-card);
  color: var(--text-main);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.2s ease;
  padding: 0;
  outline: none;
}
.theme-btn:hover {
  background: var(--primary-light);
  border-color: var(--primary);
  transform: rotate(15deg) scale(1.05);
}
.theme-btn:active {
  transform: rotate(15deg) scale(0.95);
}

@media (max-width: 960px) {
  .app-body { grid-template-columns: 1fr; grid-template-rows: 1fr 1fr; }
}
</style>