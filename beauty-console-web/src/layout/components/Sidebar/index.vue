<template>
  <div class="sidebar-shell">
    <div class="logo">
      <div class="logo-mark">
        B
      </div>
      <div v-if="!isCollapse" class="logo-text">
        <strong>Beauty Console</strong>
        <span>门店经营系统</span>
      </div>
    </div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="defAct"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :active-text-color="variables.menuActiveText"
        :unique-opened="false"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
          :is-collapse="isCollapse"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { AppModule } from '@/store/modules/app'
import { UserModule } from '@/store/modules/user'
import SidebarItem from './SidebarItem.vue'
import variables from '@/styles/_variables.scss'
import { filterRoutesByRole } from '@/utils/rolePermissions'

@Component({
  name: 'SideBar',
  components: {
    SidebarItem
  }
})
export default class extends Vue {
  get defAct() {
    return this.$route.path
  }

  get sidebar() {
    return AppModule.sidebar
  }

  get routes() {
    const routes = JSON.parse(JSON.stringify([...(this.$router as any).options.routes]))
    const menu = routes.find((item: any) => item.path === '/')
    return menu && menu.children ? filterRoutesByRole(menu.children, UserModule.roles, UserModule.permissions) : []
  }

  get variables() {
    return variables
  }

  get isCollapse() {
    return !this.sidebar.opened
  }
}
</script>

<style lang="scss" scoped>
.sidebar-shell {
  height: 100%;
  background: #242936;
  padding: 14px 12px;
}

.logo {
  height: 58px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 8px;
  color: #ffffff;
}

.logo-mark {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f8c14a 0%, #f28f6b 100%);
  color: #242936;
  font-size: 22px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  display: flex;
  flex-direction: column;
  min-width: 0;

  strong {
    font-size: 15px;
    line-height: 20px;
  }

  span {
    color: #aeb7ca;
    font-size: 12px;
    line-height: 16px;
  }
}

.el-scrollbar {
  height: calc(100% - 58px);
}

.el-menu {
  border: none;
  width: 100% !important;
  padding-top: 12px;
}
</style>
