<template>
  <div>B<component-c></component-c></div>
</template>

<script lang="ts">
  import Vue from 'vue'

  Vue.component('component-c', require('./component-c.vue').default);

  export default Vue.extend({});
</script>

<style scoped lang="scss">
</style>
