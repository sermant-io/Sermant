import Vue from 'vue'
import Router from 'vue-router'
import PluginsInfo from '../components/PluginsInfo'
import Consanguinity from '../views/consanguinity.vue'
import Contract from "../views/contract.vue";

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'PluginsInfo',
      component: PluginsInfo
    },
    {
      path: '/contract',
      name: 'Contract',
      component: Contract,
      meta: {
        title: '契约信息'
      }
    },
    {
      path: '/consanguinity',
      name: 'Consanguinity',
      component: Consanguinity,
      meta: {
        title: '血缘关系'
      }
    }
  ]
})
