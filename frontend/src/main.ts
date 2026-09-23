import { createApp } from "vue";
import Antd from "ant-design-vue";

// 全局样式
import "./styles/main.css";
import "ant-design-vue/dist/reset.css";

// 代码高亮：主题 + 核心库
import "highlight.js/styles/github.css";
import hljs from "highlight.js";

// 其它插件
import Vue3ColorPicker from "vue3-colorpicker";
import "vue3-colorpicker/style.css";
import "nprogress/nprogress.css";
import Markdown from "vue3-markdown-it";

// 你自己的组件
import App from "./App.vue";

const app = createApp(App);

// 给 Markdown 插件传配置（hljs 高亮 + 安全）
app.use(Markdown, {
  html: false,      // 禁止原始 HTML，防 XSS
  linkify: true,    // 自动识别链接
  breaks: true,     // 换行渲染成 <br>
  typographer: true,// 智能标点
  highlight: (str: string, lang: string) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang }).value;
      } catch {
        // 忽略
      }
    }
    try {
      return hljs.highlightAuto(str).value;
    } catch {
      return "";
    }
  },
});

app.use(Antd).use(Vue3ColorPicker).mount("#app");