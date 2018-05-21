<template>
  <div>A<component-b></component-b></div>
</template>

<script>
  import ComponentB from './dependencies/component-b.vue'
  Vue.component('component-b', ComponentB);

  export default {}
</script>

<style scoped lang="scss">
</style>
