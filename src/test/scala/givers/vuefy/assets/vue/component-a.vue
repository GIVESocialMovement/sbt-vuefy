<template>
  <div class="beautiful-box">A<component-b></component-b></div>
</template>

<script lang="ts">
  import {defineComponent} from 'vue'
  import ComponentB from './dependencies/_component-b';

  export default defineComponent({
    components: {ComponentB}
  });
</script>

<style scoped lang="scss">
  @import './dependencies/style.scss';
</style>
