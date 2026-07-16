import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { Message } from 'element-ui'
import { Route } from 'vue-router'
import { UserModule } from '@/store/modules/user'
import Cookies from 'js-cookie'
import { canVisitRoute } from '@/utils/rolePermissions'

NProgress.configure({ 'showSpinner': false })

const firstAccessiblePath = () => {
  const layoutRoute: any = (((router as any).options || {}).routes || [])
    .find((route: any) => route.path === '/')
  const route = ((layoutRoute && layoutRoute.children) || [])
    .find((item: any) => canVisitRoute(item, UserModule.roles, UserModule.permissions))
  if (!route || !route.path) {
    return '/404'
  }
  return route.path.startsWith('/') ? route.path : `/${route.path}`
}

router.beforeEach(async (to: Route, _: Route, next: any) => {
  NProgress.start()
  if (Cookies.get('token')) {
    await UserModule.GetUserInfo()
    if (!canVisitRoute(to, UserModule.roles, UserModule.permissions)) {
      Message.warning('当前账号无权限访问该页面')
      const target = firstAccessiblePath()
      next(target === to.path ? '/404' : target)
      return
    }
    next()
  } else {
    if (!to.meta.notNeedAuth) {
      next('/login')
    } else {
      next()
    }
  }
})

router.afterEach((to: Route) => {
  NProgress.done()
  const pageTitle = to.meta && to.meta.title
  document.title = pageTitle && pageTitle !== '美容门店后台'
    ? `${pageTitle} - 美容门店后台`
    : '美容门店后台'
})
