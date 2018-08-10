<template>
  <div class="beautiful-box">A<component-b></component-b></div>
</template>

<script>
  import ComponentB from './dependencies/component-b.vue'
  Vue.component('component-b', ComponentB);

  export default {}
</script>

<style scoped lang="scss">
  @import './dependencies/style.scss';
</style>
