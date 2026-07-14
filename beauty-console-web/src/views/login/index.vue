<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-visual">
        <div class="visual-content">
          <div class="logo-mark">
            B
          </div>
          <h1>美容门店后台</h1>
          <p>连接预约、订单、耗材库存和收款流水，让门店经营数据更清楚。</p>
        </div>
      </div>
      <div class="login-form">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-position="top">
          <div class="form-title">
            <span>欢迎回来</span>
            <strong>登录工作台</strong>
          </div>
          <el-form-item label="账号" prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="请输入账号"
              prefix-icon="el-icon-user"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              @keyup.enter.native="handleLogin"
            />
          </el-form-item>
          <el-button
            :loading="loading"
            class="login-btn"
            type="primary"
            @click.native.prevent="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { Form as ElForm } from 'element-ui'
import { UserModule } from '@/store/modules/user'

@Component({
  name: 'Login'
})
export default class extends Vue {
  private loginForm = {
    username: 'admin',
    password: '123456'
  }

  private loginRules = {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }

  private loading = false

  private handleLogin() {
    const loginForm = this.$refs.loginForm as ElForm
    loginForm.validate(async (valid: boolean) => {
      if (!valid) {
        return false
      }
      this.loading = true
      try {
        const res = await UserModule.Login(this.loginForm)
        if (String(res.code) === '1') {
          this.$router.push('/')
        }
      } finally {
        this.loading = false
      }
    })
  }
}
</script>

<style lang="scss">
.login-page {
  height: 100%;
  min-height: 680px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background:
    linear-gradient(135deg, rgba(36, 41, 54, 0.82), rgba(48, 55, 70, 0.68)),
    url('~@/assets/img_denglu_bj.jpg') center/cover no-repeat;
}

.login-card {
  width: 980px;
  min-height: 560px;
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  border-radius: 28px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 28px 80px rgba(20, 25, 35, 0.28);
}

.login-visual {
  position: relative;
  padding: 44px;
  color: #ffffff;
  background:
    linear-gradient(150deg, rgba(217, 151, 50, 0.92), rgba(225, 108, 122, 0.84)),
    url('~@/assets/login/login-l.png') center/cover no-repeat;
}

.visual-content {
  position: absolute;
  left: 44px;
  right: 44px;
  bottom: 44px;

  .logo-mark {
    width: 54px;
    height: 54px;
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.88);
    color: #8d4f09;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    font-weight: 800;
    margin-bottom: 22px;
  }

  h1 {
    margin: 0 0 12px;
    font-size: 34px;
    line-height: 42px;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    max-width: 360px;
    font-size: 15px;
    line-height: 26px;
    color: rgba(255, 255, 255, 0.9);
  }
}

.login-form {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px;

  .el-form {
    width: 100%;
    max-width: 320px;
  }

  .form-title {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 34px;

    span {
      color: #9aa3b5;
      font-size: 14px;
    }

    strong {
      color: #20242f;
      font-size: 28px;
      line-height: 34px;
    }
  }

  .el-input__inner {
    height: 44px;
    line-height: 44px;
    border-radius: 14px;
    background: #f7f8fb;
    border-color: #edf0f5;
  }

  .el-form-item {
    margin-bottom: 22px;
  }
}

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 16px;
  font-size: 15px;
  margin-top: 10px;
  box-shadow: 0 12px 24px rgba(217, 151, 50, 0.28);
}
</style>
