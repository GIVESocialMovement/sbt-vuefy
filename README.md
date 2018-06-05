sbt-vuefy
==========

[![CircleCI](https://circleci.com/gh/GIVESocialMovement/sbt-vuefy/tree/master.svg?style=shield)](https://circleci.com/gh/GIVESocialMovement/sbt-vuefy/tree/master)
[![codecov](https://codecov.io/gh/GIVESocialMovement/sbt-vuefy/branch/master/graph/badge.svg)](https://codecov.io/gh/GIVESocialMovement/sbt-vuefy)

sbt-vuefy integrates Vue's single components into Playframework. It hot-reloads the changes of Vue components while running Playframework with `sbt run`. It also works with `sbt stage`, which triggers the production build.

Please see the example project in the folder `test-play-project`.

Requirements
-------------

You'll need to install Webpack and vue-loader and give the webpack binary location to sbt-vuefy.


How to use
-----------

### 1. Install the plugin

Add the below line to `project/plugins.sbt`:

```
lazy val sbtVuefy = RootProject(uri("git://github.com/GIVESocialMovement/sbt-vuefy.git#master"))
```

You may change `master` to a specific commit.


### 2. Configure Webpack config file.

Create `webpack.config.js` with the below specifications:

```
...

// Required by sbt-vuefy.
const SbtVuefyPlugin = require('./sbt-vuefy-plugin.js')

module.exports = {
  output: {
    publicPath: '/assets', // Required by sbt-vuefy.
    library: '[camel-case-name]', // Required by sbt-vuefy.
    filename: '[name].js', // Required by sbt-vuefy.
  },
  plugins: [
    new SbtVuefyPlugin() // Required by sbt-vuefy.
  ],
  ...
}

...
```

This config file will be copied and used by sbt-vuefy when compiling Vue components.


### 3. Configure `build.sbt`

Specifying necessary configurations:

```
Assets / VueKeys.vuefy / VueKeys.prodCommands := Set("stage")  // the command that triggers production build (webpack -p).
Assets / VueKeys.vuefy / VueKeys.webpackBinary := "./node_modules/.bin/webpack"  // The location of the webpack binary.
Assets / VueKeys.vuefy / VueKeys.webpackConfig := "./webpack.config.js"  // the location of the webpack configuration.
```

### 4. Find out where the output JS file is and how to use it

The plugin compiles `*.vue` within `app/assets`.

For the path `app/assets/vue/components/some-component.vue`, the output JS should be at `http://.../assets/vue/components/some-component.js`.
It should also work with `@routes.Assets.versioned("vue/components/some-component.js")`.

The exported module name is the camel case of the file name. In the above example, the module name is `SomeComponent`.

Therefore, we can use the component as shown below:

```
<script src='@routes.Assets.versioned("vue/components/some-component.js")'></script>

<div id="app"></div>
<script>
  var app = new Vue({
    el: '#app',
    render: function(html) {
      return html(SomeComponent.default, {
        props: {
          someData: "data"
        }
      });
    }
  })
</script>
```

Please see the folder `test-play-project` for a complete example.


Interested in using the plugin?
--------------------------------

Please feel free to open an issue to ask questions. Let us know how you want to use the plugin.


Contributing
---------------

The project welcomes any contribution. Here are the steps for testing when developing locally:

1. Run `yarn install` in order to install packages needed for the integration tests.
2. Run `sbt test` to run all tests.
3. To test the plugin on an actual Playframework project, go to `test-play-project`, run `yarn install`, and run `sbt run`


Future improvement
--------------------

* `VueKeys.prodCommands` is hacky. There must be a better way of implementing the production build setting.
* Currently, the plugin doesn't track CSS dependencies (e.g. using `@import`) because webpack/vue-loader doesn't track these dependencies. We need to find a way.
