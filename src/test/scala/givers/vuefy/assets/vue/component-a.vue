<template>
  <div class="beautiful-box">A<component-b></component-b></div>
</template>

<script>
  Vue.component('component-b', require('./dependencies/component-b.vue').default);

  export default {}
</script>

<style scoped lang="scss">
  @import './dependencies/style.scss';
</style>
