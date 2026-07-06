<script setup lang="ts">
</script>

<template>
  <footer class="footer">
    <div class="footer-inner">
      <div class="footer-brand">
        <span class="brand-accent">Mh</span><span class="brand-text">平台</span>
      </div>
      <p class="footer-desc">精选好物，品质生活从这里开始</p>
      <div class="footer-divider"></div>
      <p class="footer-copy">© 2026 Mh平台 · 诚信经营，用心服务</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #0f172a;
  padding: 40px 0;
  margin-top: auto;
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  text-align: center;
}

.footer-brand {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.3px;
  margin-bottom: 10px;
}

.brand-accent {
  color: #a5b4fc;
}

.brand-text {
  color: rgba(255, 255, 255, 0.85);
}

.footer-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.45);
  margin: 0 0 24px;
}

.footer-divider {
  width: 48px;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0 auto 20px;
}

.footer-copy {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.35);
  margin: 0;
}
</style>
