import Vue from 'vue'
import Router from 'vue-router'
import PluginsInfo from '../components/PluginsInfo'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'PluginsInfo',
      component: PluginsInfo
    }
  ]
})
