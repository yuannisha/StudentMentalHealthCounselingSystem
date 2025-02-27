import { createApp } from 'vue';
import { createPinia } from 'pinia';
import Antd from 'ant-design-vue';
import App from './App.vue';
import router from './router';
import 'ant-design-vue/dist/reset.css';
import './style.css';

const app = createApp(App);
const pinia = createPinia();

// 按顺序安装插件
app.use(pinia)
   .use(router)
   .use(Antd)
   .mount('#app'); 