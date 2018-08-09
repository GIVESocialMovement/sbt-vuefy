<template>
  <button class="our-button" @click="click">
    <slot></slot>
  </button>
</template>

<script>
  export default {
    methods: {
      click: function() {
        this.$emit('click');
      }
    }
  };
</script>

<style scoped lang="scss">
</style>
