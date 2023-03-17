import { createApp } from "vue";

import App from "./App.vue";
import router from "./router"
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'


import "~/styles/index.scss";
import 'uno.css'

import "element-plus/theme-chalk/src/message.scss"

const app = createApp(App);

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

app.use(ElementPlus, {
    locale: zhCn,
})
app.use(router);
app.mount("#app");
