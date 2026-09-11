<template>
  <div class="shell appt-shell">
    <div class="page-body">
      <van-nav-bar title="体检预约" left-arrow @click-left="$router.back()" />

      <!-- 头部 -->
      <div class="hero fade-up">
        <div class="hero-deco deco-1"></div>
        <div class="hero-deco deco-2"></div>
        <div class="hero-inner">
          <div class="hero-icon"><van-icon name="shield-o" /></div>
          <div class="hero-txt">
            <p class="hero-title">专业健康体检</p>
            <p class="hero-desc">积分兑换 · 全流程贴心服务</p>
          </div>
          <button class="hero-mine press" type="button" @click="$router.push('/appointment/mine')">
            <van-icon name="clock-o" />
            <span>我的预约</span>
          </button>
        </div>
      </div>

      <!-- 套餐列表 -->
      <template v-if="list.length">
        <div class="sec-head fade-up d1">
          <div class="sec-title">体检套餐</div>
          <span class="sec-more">{{ list.length }} 个可选</span>
        </div>
        <div
          v-for="(pkg, i) in list"
          :key="pkg.id"
          class="app-card pkg-card press fade-up"
          :class="'d' + Math.min(i + 1, 5)"
          @click="$router.push(`/appointment/${pkg.id}`)"
        >
          <div class="pkg-head">
            <p class="pkg-name">{{ pkg.name }}</p>
            <div class="pkg-price">
              <em>{{ pkg.price }}</em>
              <span>积分</span>
            </div>
          </div>
          <p class="pkg-desc">{{ pkg.description }}</p>
          <div class="pkg-foot">
            <div class="pkg-tags">
              <span v-for="(item, k) in (pkg.items || []).slice(0, 3)" :key="k" class="mini-tag">{{ item }}</span>
              <span v-if="(pkg.items || []).length > 3" class="mini-tag more">+{{ pkg.items.length - 3 }}</span>
            </div>
            <span class="pkg-go">立即预约 <van-icon name="arrow" /></span>
          </div>
        </div>
      </template>
      <van-empty v-else description="暂无可用套餐" class="fade-up d1" />
    </div>
    <TabBar />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import TabBar from '../components/TabBar.vue'
import { listPackages } from '../api/appointment'

const list = ref([])

onMounted(async () => {
  try {
    const data = await listPackages({ pageNum: 1, pageSize: 20 })
    list.value = data?.list || []
  } catch (e) { /* 忽略 */ }
})
</script>

<style scoped>
.appt-shell {
  background:
    radial-gradient(90% 22% at 50% 0%, rgba(63, 124, 255, 0.08), transparent 70%),
    var(--bg);
}

/* 头部 */
.hero {
  position: relative;
  margin: 8px 14px 6px;
  border-radius: var(--r-lg);
  background: linear-gradient(140deg, #2f6cf6 0%, #3f7cff 50%, #6fa5ff 100%);
  padding: 18px 16px;
  overflow: hidden;
  box-shadow: 0 12px 28px rgba(63, 124, 255, 0.3);
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.deco-1 { width: 130px; height: 130px; top: -55px; right: -35px; }
.deco-2 { width: 56px; height: 56px; bottom: -22px; left: 24%; opacity: 0.7; }

.hero-inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 13px;
  color: #fff;
}

.hero-icon {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 25px;
  flex-shrink: 0;
}

.hero-txt {
  flex: 1;
}

.hero-title {
  margin: 0;
  font-size: 19px;
  font-weight: 800;
  letter-spacing: 1px;
}

.hero-desc {
  margin: 5px 0 0;
  font-size: 13px;
  opacity: 0.85;
}

.hero-mine {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  border: none;
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
  font-size: 11px;
  padding: 9px 12px;
  border-radius: 13px;
  cursor: pointer;
  flex-shrink: 0;
}

.hero-mine .van-icon {
  font-size: 17px;
}

/* 套餐卡 */
.pkg-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.pkg-name {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--ink);
}

.pkg-price {
  flex-shrink: 0;
  display: flex;
  align-items: baseline;
  gap: 2px;
  color: var(--sky);
}

.pkg-price em {
  font-size: 26px;
  font-weight: 800;
  font-style: normal;
}

.pkg-price span {
  font-size: 13px;
  color: var(--muted);
}

.pkg-desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--ink-2);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pkg-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.pkg-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.mini-tag {
  font-size: 11px;
  color: var(--sky);
  background: rgba(63, 124, 255, 0.09);
  padding: 3px 9px;
  border-radius: 999px;
}

.mini-tag.more {
  color: var(--muted);
  background: var(--bg);
}

.pkg-go {
  font-size: 13px;
  font-weight: 700;
  color: var(--sky);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
</style>
