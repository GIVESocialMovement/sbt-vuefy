<template>
  <div class="beautiful-box">D<component-c></component-c></div>
</template>

<script lang="ts">
  import {defineComponent} from 'vue'
  const ComponentC = require('./dependencies/_component-c').default;

  export default defineComponent({
    components: {ComponentC}
  });
</script>
