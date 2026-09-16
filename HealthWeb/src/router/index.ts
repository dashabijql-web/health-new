import { createRouter, createWebHistory } from 'vue-router'

import AboutView from '../views/AboutView.vue'
import DepartmentView from '../views/DepartmentView.vue'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/departments',
      name: 'departments',
      component: DepartmentView,
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
    },
  ],
})

export default router
