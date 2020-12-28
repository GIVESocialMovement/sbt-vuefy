<template>
  <div>B<component-c></component-c></div>
</template>

<script lang="ts">
  import {defineComponent} from 'vue'

  const ComponentC = require('./_component-c').default;

  export default defineComponent({
    components: {ComponentC}
  });
</script>

<style scoped lang="scss">
</style>
