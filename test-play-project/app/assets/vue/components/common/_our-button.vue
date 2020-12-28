<template>
  <button class="our-button" @click="click">
    <slot></slot>
  </button>
</template>

<script lang="ts">
  import { defineComponent } from 'vue'

  export default defineComponent({
    emits: ['click'],
    methods: {
      click(): void {
        this.$emit('click');
      },
      testMessage(): void {
        console.log('our-button receives the message.');
      }
    }
  });
</script>

<style scoped lang="scss">
</style>
