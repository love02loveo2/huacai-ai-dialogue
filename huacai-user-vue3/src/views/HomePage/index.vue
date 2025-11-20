<template>
  <div class="cat-girl-container">
    <!-- 顶部导航 -->
    <header class="header-container">
      <div class="cat-header">
        <!-- 左侧：Logo + 标题 -->
        <div class="header-left" @click="router.push('/index/home')">
          <img src="@/assets/logo/logo.png" alt="logo" class="logo-cat" />
          <div class="titles">💗 青紫猫娘小助手 💗</div>
        </div>

        <!-- 中间导航 -->
        <div class="header-center">
          <el-menu
              :default-active="route.path"
              mode="horizontal"
              :router="true"
              class="cat-menu"
          >
            <el-menu-item index="/index/home">
              <i class="icon-cat paw"></i> 喵窝首页
            </el-menu-item>
            <el-menu-item index="/index/profile">
              <i class="icon-cat heart"></i> 主人中心
            </el-menu-item>
          </el-menu>
        </div>

        <!-- 右侧：用户登录状态 -->
        <div class="header-right">
          <!-- 未登录 -->
          <div v-if="!nickName" class="auth-buttons">
            <el-button size="small" @click="router.push('/login')" round>
              🐾 登录
            </el-button>
            <el-button size="small" type="danger" @click="router.push('/register')" round>
              💖 注册
            </el-button>
          </div>

          <!-- 已登录 -->
          <div v-else class="user-info">
            <el-dropdown>
              <div class="header-dropdown">
                <img :src="avatar" alt="头像" class="avatar-img" />
                <div class="user-name">
                  <span>{{ nickName }}</span>
                  <i class="el-icon-arrow-down" style="margin-left: 5px"></i>
                </div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-button type="text" @click.native="logout">
                      确定要离开本喵吗？😿
                    </el-button>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 猫尾巴 -->
    <div class="cat-tail"></div>

<!--    &lt;!&ndash; 看板娘挂载点 &ndash;&gt;-->
<!--    <div id="pio-container"></div>-->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser } from '@/api/system/user'
import useUserStore from '@/store/modules/user.js'
import { ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const nickName = ref(null)
const avatar = computed(() => userStore.avatar)

// 加载用户信息
const getUserInfo = async () => {
  try {
    const res = await getUser(userStore.id)
    nickName.value = res.data.nickName
  } catch (e) {
    console.warn('获取用户信息失败', e)
  }
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('确定要离开本喵吗？本喵会想你的…(´;ω;｀)', '🐾 温馨提示', {
    confirmButtonText: '摸摸头再走',
    cancelButtonText: '取消',
    type: 'warning',
    center: true,
    customClass: 'cat-confirm-box'
  })
      .then(() => {
        userStore.logOut().then(() => {
          location.href = '/index'
        })
      })
      .catch((action) => {
        if (action === 'cancel') {
          ElMessageBox.alert('嘿嘿，就知道主人舍不得走～ (ฅ>ω<*ฅ)', '开心！', {
            type: 'success',
            center: true
          })
        }
      })
}

// 🟢 onMounted 中也只保留必要逻辑
onMounted(() => {
  getUserInfo()
  // 🔴 已删除看板娘相关初始化
})
</script>




<style scoped>
/* 全局柔和背景 */
.cat-girl-container {
  font-family: "Rounded Mplus 1c", "Comic Neue", "幼圆", "Comic Sans MS", cursive, sans-serif;
  background: radial-gradient(circle at top left, #fff5f9, #ffeaf3, #fff0f5);
  min-height: 100vh;
  color: #444;
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  border: 3px solid rgba(255, 182, 193, 0.3);
  box-shadow: inset 0 0 12px rgba(255, 192, 203, 0.3);
}

/* 粉雾毛边 */
.cat-girl-container::before {
  content: "";
  position: fixed;
  top: 0; right: 0;
  width: 200px; height: 200px;
  background: radial-gradient(circle at center, rgba(255,182,193,0.25), rgba(255,240,245,0.1));
  filter: blur(20px);
  border-radius: 50%;
  pointer-events: none;
  z-index: -1;
}

/* 头部风格 */
.cat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 32px;
  background: rgba(255, 220, 235, 0.8);
  backdrop-filter: blur(14px);
  border-bottom: 3px solid #ffc0cb;
  box-shadow: 0 4px 10px rgba(255, 105, 180, 0.2);
  border-radius: 0 0 20px 20px;
  position: sticky;
  top: 0;
  z-index: 1000;
}

/* Logo和标题 */
.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.logo-cat {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  border: 3px solid #ff8ec3;
  box-shadow: 0 0 10px rgba(255, 182, 193, 0.7);
  animation: bounce 2.5s infinite ease-in-out;
}

.titles {
  margin-left: 14px;
  font-size: 1.5rem;
  font-weight: 700;
  color: #d63384;
  text-shadow: 0 0 6px rgba(255, 182, 193, 0.7);
}

/* 导航菜单可爱风 */
.cat-menu {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #e91e63;
  --el-menu-hover-text-color: #ff69b4;
}

.cat-menu .el-menu-item {
  font-size: 1.05rem;
  border-radius: 20px;
  margin: 0 8px;
  transition: all 0.3s;
}

.cat-menu .el-menu-item:hover {
  background: rgba(255, 192, 203, 0.2);
  transform: scale(1.08);
  box-shadow: 0 0 10px rgba(255, 182, 193, 0.3);
}

.icon-cat {
  margin-right: 6px;
  font-style: normal;
  filter: drop-shadow(0 0 2px #ffb6c1);
}
.paw::after { content: "🐾"; }
.heart::after { content: "💖"; }

/* 登录区 */
.auth-buttons .el-button {
  margin: 0 8px;
  border-radius: 20px;
  font-family: inherit;
  font-weight: 600;
  transition: 0.3s;
}

.auth-buttons .el-button:hover {
  transform: scale(1.05);
  box-shadow: 0 0 8px rgba(255, 105, 180, 0.4);
}

/* 用户信息 */
.avatar-img {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid #ff69b4;
  box-shadow: 0 0 10px rgba(255, 105, 180, 0.5);
}

.user-name {
  margin-left: 8px;
  font-weight: bold;
  color: #d63384;
  text-shadow: 0 0 4px rgba(255, 182, 193, 0.4);
}

/* 尾巴动画 */
.cat-tail {
  position: fixed;
  bottom: 40px;
  right: 50px;
  width: 70px;
  height: 140px;
  background: linear-gradient(135deg, #ffb6c1, #ff69b4);
  border-radius: 50% 0 50% 50%;
  transform: rotate(-45deg);
  opacity: 0.5;
  animation: sway 4s ease-in-out infinite;
  pointer-events: none;
}

/* 动画 */
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

@keyframes sway {
  0%, 100% { transform: rotate(-45deg) skewX(5deg); }
  50% { transform: rotate(-45deg) skewX(-5deg); }
}

/* 弹窗猫娘化 */
.cat-confirm-box {
  font-family: "Comic Neue", "幼圆", cursive;
  border-radius: 20px;
  background: #fffafc;
  box-shadow: 0 0 15px rgba(255, 182, 193, 0.4);
}

.cat-confirm-box .el-message-box__title {
  color: #e91e63;
  font-weight: bold;
}

.cat-confirm-box .el-button--text {
  color: #ff69b4;
}

#pio-container {
  position: fixed;
  right: 30px;
  bottom: 60px;
  z-index: 9999;
}


</style>
