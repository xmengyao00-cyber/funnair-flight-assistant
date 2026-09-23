<script setup lang="ts">
import Message from "@/views/home/Message.vue";
import type { MessageItem } from "@/types/message";

const props = defineProps<{
  list: MessageItem[];
  isStreaming?: boolean;          // 👈 新增 prop
}>();
</script>

<template>
  <div class="msg-list">
    <Message
      v-for="(msg, i) in props.list"
      :key="i"
      :role="msg.role"
      :content="msg.content"
      :time="(msg as any).time"
      :streaming="
        !!props.isStreaming &&                          
        i === props.list.length - 1 &&
        msg.role === 'assistant'
      "
    />
  </div>
</template>

<style scoped lang="less">
.msg-list {
  display: flex;
  flex-direction: column;
}
</style>