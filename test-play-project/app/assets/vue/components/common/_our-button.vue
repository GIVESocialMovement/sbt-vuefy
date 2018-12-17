<template>
  <button class="our-button" @click="click">
    <slot></slot>
  </button>
</template>

<script lang="ts">
  import Vue from 'vue';

  export interface Type extends Vue {
    click(): void
    testMessage(): void
  }

  export default Vue.extend({
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
