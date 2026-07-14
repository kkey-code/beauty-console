import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { Message } from 'element-ui'
import { Route } from 'vue-router'
import { UserModule } from '@/store/modules/user'
import Cookies from 'js-cookie'
import { canVisitRoute } from '@/utils/rolePermissions'

NProgress.configure({ 'showSpinner': false })

router.beforeEach(async (to: Route, _: Route, next: any) => {
  NProgress.start()
  if (Cookies.get('token')) {
    await UserModule.GetUserInfo()
    if (!canVisitRoute(to, UserModule.roles, UserModule.permissions)) {
      Message.warning('当前账号无权限访问该页面')
      next('/dashboard')
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
  document.title = to.meta.title
})
