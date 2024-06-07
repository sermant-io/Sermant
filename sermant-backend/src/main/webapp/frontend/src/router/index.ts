import {createRouter, createWebHashHistory} from 'vue-router'
import EventsView from '~/views/EventsView.vue'
import EventsConfigView from '~/views/EventsConfigView.vue'
import InstancesView from '~/views/InstancesView.vue'
import ConfigView from "~/views/ConfigView.vue";
import configInfo from "~/views/ConfigInfo.vue";

const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: InstancesView
    },
    {
      path: '/instances',
      name: 'instances',
      component: InstancesView
    },
    {
      path: '/events',
      name: 'events',
      component: EventsView,
      children: [
        {
          path: 'config',
          component: EventsConfigView,
        }
      ]
    },
    {
      path: '/events-config',
      name: 'events-config',
      component: EventsConfigView
    },
    {
      path: '/config',
      name: 'config',
      component: ConfigView
    },
    {
      path: '/configInfo',
      name: 'configInfo',
      component: configInfo
    },
  ]
})

export default router
