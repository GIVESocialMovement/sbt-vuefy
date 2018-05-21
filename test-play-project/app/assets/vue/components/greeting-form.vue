<template>
  <div class="greeting-form">
    <h1>{{ greeting }}</h1>
    <p>Value: {{ toggleText }}</p>
    <our-button @click="toggle">Toggle button</our-button>
  </div>
</template>

<script>
  import OurButton from './common/our-button.vue'

  Vue.component('our-button', OurButton);

  export default {
    props: {
      greeting: {
        type: String,
        required: true
      }
    },
    data: function() {
      return {
        toggleValue: false
      };
    },
    computed: {
      toggleText: function() {
        if (this.toggleValue) {
          return "on";
        } else {
          return "off";
        }
      }
    },
    methods: {
      toggle: function() {
        this.toggleValue = !this.toggleValue;
      }
    }
  };
</script>

<style scoped lang="scss">
  .greeting-form {
    display: block;
    text-align: center;
    padding: 25px 0px;
  }
</style>
