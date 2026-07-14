<template>
  <div class="navbar">
    <div class="navbar-left">
      <hamburger
        id="hamburger-container"
        :is-active="sidebar.opened"
        class="hamburger-container"
        @toggleClick="toggleSideBar"
      />
      <div class="brand-title">
        <strong>美容门店后台</strong>
        <span>预约、订单、库存、收款统一管理</span>
      </div>
    </div>

    <div class="right-menu">
      <el-tag class="role-tag" size="medium">
        {{ roleName }}
      </el-tag>
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-chip">
          {{ userName }}<i class="el-icon-arrow-down el-icon--right" />
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="logout">
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { AppModule } from '@/store/modules/app'
import { UserModule } from '@/store/modules/user'
import Hamburger from '@/components/Hamburger/index.vue'

@Component({
  name: 'Navbar',
  components: {
    Hamburger
  }
})
export default class extends Vue {
  get sidebar() {
    return AppModule.sidebar
  }

  get userName() {
    const info = UserModule.userInfo as any
    return info.staffName || info.username || UserModule.name || '管理员'
  }

  get roleName() {
    const info = UserModule.userInfo as any
    return info.roleName || info.roleCode || (UserModule.roles && UserModule.roles[0]) || 'admin'
  }

  private toggleSideBar() {
    AppModule.ToggleSideBar(false)
  }

  private async handleCommand(command: string) {
    if (command === 'logout') {
      await UserModule.LogOut()
      this.$router.replace('/login')
    }
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 68px;
  padding: 0 24px 0 10px;
  background: #ffffff;
  border-bottom: 1px solid #edf0f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}

.navbar-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.hamburger-container {
  padding: 0 14px;
  cursor: pointer;
  color: #3d4251;
}

.brand-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;

  strong {
    font-size: 18px;
    color: #20242f;
    line-height: 22px;
  }

  span {
    font-size: 12px;
    color: #8a92a6;
    line-height: 16px;
  }
}

.right-menu {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-tag {
  border-radius: 999px;
  color: #7c4b00;
  background: #fff3d8;
  border-color: #ffe0a3;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  background: #f6f8fb;
  color: #303644;
  cursor: pointer;
  border: 1px solid #edf0f5;
}
</style>
