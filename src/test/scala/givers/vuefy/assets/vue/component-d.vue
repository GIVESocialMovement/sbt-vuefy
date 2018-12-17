<template>
  <div class="beautiful-box">D<component-c></component-c></div>
</template>

<script>
  Vue.component('component-c', require('./dependencies/_component-c.vue').default);

  export default {}
</script>
