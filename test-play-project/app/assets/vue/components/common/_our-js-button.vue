<template>
  <button class="our-js-button" @click="click">
    <slot></slot>
  </button>
</template>

<script>
  export default {
    methods: {
      click() {
        this.$emit('click');
      },
      testMessage() {
        console.log('our-js-button receives the message.');
      }
    }
  };
</script>

<style scoped lang="scss">
</style>
