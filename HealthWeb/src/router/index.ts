import { createRouter, createWebHistory } from 'vue-router'

import { getToken } from '../utils/auth'
import AboutView from '../views/AboutView.vue'
import DepartmentView from '../views/DepartmentView.vue'
import DeviceView from '../views/DeviceView.vue'
import EmployeeView from '../views/EmployeeView.vue'
import HomeView from '../views/HomeView.vue'
import JobTypeView from '../views/JobTypeView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/departments',
      name: 'departments',
      component: DepartmentView,
      meta: { requiresAuth: true },
    },
    {
      path: '/job-types',
      name: 'job-types',
      component: JobTypeView,
      meta: { requiresAuth: true },
    },
    {
      path: '/employees',
      name: 'employees',
      component: EmployeeView,
      meta: { requiresAuth: true },
    },
    {
      path: '/devices',
      name: 'devices',
      component: DeviceView,
      meta: { requiresAuth: true },
    },
    {
      path: '/heart-rate',
      name: 'heart-rate',
      component: () => import('../views/HeartRateView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/blood-pressure',
      name: 'blood-pressure',
      component: () => import('../views/BloodPressureView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/blood-oxygen',
      name: 'blood-oxygen',
      component: () => import('../views/BloodOxygenView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/temperature',
      name: 'temperature',
      component: () => import('../views/TemperatureView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/pressure',
      name: 'pressure',
      component: () => import('../views/PressureView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/sleep',
      name: 'sleep',
      component: () => import('../views/SleepView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/alert-config',
      name: 'alert-config',
      component: () => import('../views/AlertConfigView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/about',
      name: 'about',
      component: AboutView,
    },
  ],
})

router.beforeEach((to) => {
  const loggedIn = Boolean(getToken())
  if (to.meta.requiresAuth && !loggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && loggedIn) {
    return { name: 'departments' }
  }
})

export default router
