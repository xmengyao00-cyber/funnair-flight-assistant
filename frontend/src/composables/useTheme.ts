import { ref, watch } from "vue";

type Theme = "light" | "dark";

const savedTheme = (localStorage.getItem("theme") as Theme) || "light";
const theme = ref<Theme>(savedTheme);

function applyTheme(t: Theme) {
  document.documentElement.setAttribute("data-theme", t);
}

// 初始化
applyTheme(theme.value);

// 监听变化
watch(theme, (newTheme) => {
  applyTheme(newTheme);
  localStorage.setItem("theme", newTheme);
});

export function useTheme() {
  function toggleTheme() {
    theme.value = theme.value === "light" ? "dark" : "light";
    console.log("切换主题：", theme.value);   // ← 加日志看是否触发
  }
  return { theme, toggleTheme };
}