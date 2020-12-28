<template>
  <div class="greeting-form">
    <h1>{{ greeting }}</h1>
    <p>Value: {{ toggleText }}</p>
    <our-button ref="button" @click="toggle">Toggle Typescript button</our-button>
    <our-js-button ref="jsButton" @click="toggle">Toggle Javascript button</our-js-button>
  </div>
</template>

<script lang="ts">
  import {defineComponent} from 'vue'
  import OurButton from './common/_our-button';
  const OurJsButton = require('./common/_our-js-button').default;

  export default defineComponent({
    components: {
      OurButton,
      OurJsButton,
    },
    props: {
      greeting: {
        type: String,
        required: true
      }
    },
    data() {
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
        this.$refs.button.testMessage();
        this.$refs.jsButton.testMessage();
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
