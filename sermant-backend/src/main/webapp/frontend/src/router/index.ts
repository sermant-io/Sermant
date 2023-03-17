import { createRouter, createWebHashHistory } from 'vue-router'
import EventsView from '~/views/EventsView.vue'
import EventsConfigView from '~/views/EventsConfigView.vue'
import InstancesView from '~/views/InstancesView.vue'

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
    }
  ]
})

export default router
