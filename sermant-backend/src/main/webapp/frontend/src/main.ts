import {createApp} from "vue";

import App from "./App.vue";
import router from "./router"
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import ElementPlus from 'element-plus'
import translations from './composables/translations';
import i18n from './composables/translations';


import "~/styles/index.scss";
import 'uno.css'

import "element-plus/theme-chalk/src/message.scss"

console.log("欢迎加入Sermant社区: https://sermant.io");

const app = createApp(App);

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
app.use(translations);
app.use(i18n)
app.use(ElementPlus);
app.use(router);

app.config.globalProperties.$t = i18n.global.t;

app.mount("#app");
