import { VuexModule, Module, Action, Mutation, getModule } from 'vuex-module-decorators'
import { login, userLogout } from '@/api/employee'
import { getToken, setToken, removeToken, setUserInfo, getUserInfo, removeUserInfo } from '@/utils/cookies'
import store from '@/store'
import Cookies from 'js-cookie'
import { Message } from 'element-ui'

export interface IUserState {
  token: string
  name: string
  avatar: string
  storeId: string
  introduction: string
  userInfo: any
  roles: string[]
  permissions: string[]
  username: string
}

const parseUserInfo = () => {
  const value = getUserInfo() || Cookies.get('user_info')
  if (!value) {
    return {}
  }
  try {
    return JSON.parse(value)
  } catch (error) {
    return {}
  }
}

const roleOf = (userInfo: any) => userInfo.roleCode || userInfo.roleName || 'admin'
const nameOf = (userInfo: any) => userInfo.staffName || userInfo.username || '管理员'
const permissionsOf = (userInfo: any) => Array.isArray(userInfo.permissions) ? userInfo.permissions : []

@Module({ dynamic: true, store, name: 'user' })
class User extends VuexModule implements IUserState {
  public token = getToken() || ''
  public name = nameOf(parseUserInfo())
  public avatar = ''
  public storeId = ''
  public introduction = ''
  public userInfo = parseUserInfo()
  public roles: string[] = [roleOf(parseUserInfo())]
  public permissions: string[] = permissionsOf(parseUserInfo())
  public username = Cookies.get('username') || ''

  @Mutation
  private SET_TOKEN(token: string) {
    this.token = token
  }

  @Mutation
  private SET_NAME(name: string) {
    this.name = name
  }

  @Mutation
  private SET_USERINFO(userInfo: any) {
    this.userInfo = { ...userInfo }
  }

  @Mutation
  private SET_ROLES(roles: string[]) {
    this.roles = roles
  }

  @Mutation
  private SET_PERMISSIONS(permissions: string[]) {
    this.permissions = permissions || []
  }

  @Mutation
  private SET_USERNAME(username: string) {
    this.username = username
  }

  @Action
  public async Login(userInfo: { username: string; password: string }) {
    const username = userInfo.username.trim()
    this.SET_USERNAME(username)
    Cookies.set('username', username)

    const { data } = await login({ username, password: userInfo.password })
    if (String(data.code) === '1') {
      const loginUser = data.data || {}
      this.SET_TOKEN(loginUser.token)
      this.SET_USERINFO(loginUser)
      this.SET_NAME(nameOf(loginUser))
      this.SET_ROLES([roleOf(loginUser)])
      this.SET_PERMISSIONS(permissionsOf(loginUser))
      setToken(loginUser.token)
      setUserInfo(loginUser)
      Cookies.set('user_info', JSON.stringify(loginUser))
      return data
    }

    Message.error(data.msg || '登录失败')
    return data
  }

  @Action
  public ResetToken() {
    removeToken()
    removeUserInfo()
    Cookies.remove('username')
    Cookies.remove('user_info')
    this.SET_TOKEN('')
    this.SET_USERINFO({})
    this.SET_ROLES([])
    this.SET_PERMISSIONS([])
  }

  @Action
  public async GetUserInfo() {
    if (!this.token) {
      throw Error('GetUserInfo: token is undefined')
    }
    const userInfo = Object.keys(this.userInfo || {}).length ? this.userInfo : parseUserInfo()
    this.SET_USERINFO(userInfo)
    this.SET_NAME(nameOf(userInfo))
    this.SET_ROLES([roleOf(userInfo)])
    this.SET_PERMISSIONS(permissionsOf(userInfo))
    return userInfo
  }

  @Action
  public async LogOut() {
    await userLogout()
    removeToken()
    removeUserInfo()
    Cookies.remove('username')
    Cookies.remove('user_info')
    this.SET_TOKEN('')
    this.SET_USERINFO({})
    this.SET_ROLES([])
    this.SET_PERMISSIONS([])
  }
}

export const UserModule = getModule(User)
