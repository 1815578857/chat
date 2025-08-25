<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// Vue 3 Composition API
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  width: 100vw;
  margin: 0;
  padding: 0;
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  position: relative;
}

body {
  margin: 0;
  padding: 0;
  overflow: hidden; /* 隐藏页面的滚动条 */
}

* {
  box-sizing: border-box;
}
</style>
