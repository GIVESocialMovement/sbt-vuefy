<template>
  <div>B<component-c></component-c></div>
</template>

<script>
  import ComponentC from './component-c.vue'
  Vue.component('component-c', ComponentC);
  export default {}
</script>

<style scoped lang="scss">
</style>
