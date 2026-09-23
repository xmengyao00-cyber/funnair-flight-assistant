<script setup lang="ts">
import type { MessageItem } from "@/types/message";
import Markdown from "vue3-markdown-it";
import "highlight.js/styles/github.css";
import DOMPurify from "dompurify";
import { computed } from "vue";

const { role, content, time, streaming } = defineProps<
  MessageItem & {
    streaming?: boolean;
  }
>();

const safeContent = computed(() => DOMPurify.sanitize(content || ""));
</script>

<template>
  <div class="msg-row" :class="role">
    <!-- 头像 -->
    <div class="avatar" :class="role">
      <span v-if="role === 'assistant'">🛩️</span>
      <span v-else>🧑‍💻</span>
    </div>

    <!-- 消息主体 -->
    <div class="msg-body">
      <div class="name">
        {{ role === "assistant" ? "Funnair Assistant" : "You" }}
      </div>

      <div class="bubble-row" :class="role">
        <div class="bubble" :class="role">
          <div v-if="!content" class="typing">
            <span></span><span></span><span></span>
          </div>
          <div v-else class="md-wrap">
            <Markdown :source="safeContent" />
            <span v-if="streaming" class="cursor">▍</span>
          </div>
        </div>
        <span class="time" v-if="time">{{ time }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  animation: fadeInUp 0.3s ease;

  &.user {
    flex-direction: row-reverse;

    .msg-body {
      align-items: flex-end;
    }

    .bubble {
      background: linear-gradient(135deg, #6366F1 0%, #4F46E5 100%);
      color: #fff;
      border: none;
      box-shadow: 0 6px 16px rgba(99, 102, 241, 0.28);

      :deep(a) {
        color: #fff;
        text-decoration: underline;
      }
      :deep(code) {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
      }
      :deep(pre) {
        background: rgba(0, 0, 0, 0.25);
      }
    }
  }
}

/* ========== 头像 ========== */
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  flex-shrink: 0;
  background: var(--primary-light);
  box-shadow: 0 2px 6px rgba(99, 102, 241, 0.1);
  border: 1px solid var(--border);

  &.user {
    background: var(--bg-hover);
    border-color: var(--border);
  }
}

/* ========== 主体 ========== */
.msg-body {
  max-width: 74%;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.name {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-sub);
  padding: 0 6px;
  line-height: 1.2;
}

/* ========== 气泡行 ========== */
.bubble-row {
  display: flex;
  align-items: flex-end;
  gap: 6px;

  &.user {
    flex-direction: row-reverse;
  }
}

.bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.55;
  background: var(--bg-card);
  color: var(--text-main);
  border: 1px solid var(--border);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  word-break: break-word;
  transition: box-shadow 0.2s, transform 0.15s;
}

.time {
  font-size: 11px;
  color: var(--text-light);
  flex-shrink: 0;
  padding-bottom: 4px;
  white-space: nowrap;
}

/* ========== Markdown 内部样式 ========== */
.md-wrap {
  :deep(p) {
    margin: 0 0 6px;
  }
  :deep(p:last-child) {
    margin-bottom: 0;
  }
  :deep(p:last-child:empty) {
    display: none;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 20px;
    margin: 6px 0;
  }

  :deep(code) {
    background: var(--bg-hover);
    color: var(--text-main);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
    font-family: "JetBrains Mono", Consolas, monospace;
  }

  :deep(pre) {
    background: #1F2937;
    color: #F9FAFB;
    padding: 12px 14px;
    border-radius: 10px;
    overflow-x: auto;
    margin: 8px 0;

    code {
      background: transparent;
      color: inherit;
      padding: 0;
    }
  }

  :deep(a) {
    color: var(--primary);
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }

  :deep(table) {
    border-collapse: collapse;
    margin: 8px 0;
    font-size: 13px;

    th,
    td {
      border: 1px solid var(--border);
      padding: 6px 10px;
    }
    th {
      background: var(--bg-hover);
    }
  }

  :deep(blockquote) {
    border-left: 3px solid var(--primary);
    padding-left: 12px;
    color: var(--text-sub);
    margin: 8px 0;
  }
}

/* ========== 打字光标 ========== */
.cursor {
  display: inline-block;
  margin-left: 2px;
  color: var(--primary);
  font-weight: bold;
  animation: blink 1s steps(2) infinite;
}
@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

/* ========== 加载中 ========== */
.typing {
  display: flex;
  gap: 4px;
  padding: 4px 2px;

  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--text-light);
    animation: bounce 1.4s infinite ease-in-out both;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }
    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}
@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>