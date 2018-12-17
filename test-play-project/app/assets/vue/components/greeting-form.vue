<template>
  <div class="greeting-form">
    <h1>{{ greeting }}</h1>
    <p>Value: {{ toggleText }}</p>
    <our-button ref="button" @click="toggle">Toggle button</our-button>
    <our-js-button ref="jsButton" @click="toggle">Toggle JS button</our-js-button>
  </div>
</template>

<script lang="ts">
  import Vue from 'vue';

  import { Type as OurButton } from './common/_our-button.vue';

  Vue.component('our-button', require('./common/_our-button.vue').default);
  Vue.component('our-js-button', require('./common/_our-js-button.vue').default);

  // Since a JS component doesn't have type, we need to provide the type in order for Typescript to compile.
  interface OurJsButton extends Vue {
    testMessage(): void
  }

  export default Vue.extend({
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
      toggleText(): string {
        if (this.toggleValue) {
          return "on";
        } else {
          return "off";
        }
      }
    },
    methods: {
      toggle(): void {
        this.toggleValue = !this.toggleValue;
        (<OurButton>(this.$refs.button)).testMessage();
        (<OurJsButton>(this.$refs.jsButton)).testMessage();
      }
    }
  });
</script>

<style scoped lang="scss">
  .greeting-form {
    display: block;
    text-align: center;
    padding: 25px 0px;
  }
</style>
