sbt-vuefy
==========

[![CircleCI](https://circleci.com/gh/GIVESocialMovement/sbt-vuefy/tree/master.svg?style=shield)](https://circleci.com/gh/GIVESocialMovement/sbt-vuefy/tree/master)
[![codecov](https://codecov.io/gh/GIVESocialMovement/sbt-vuefy/branch/master/graph/badge.svg)](https://codecov.io/gh/GIVESocialMovement/sbt-vuefy)
[![Gitter chat](https://badges.gitter.im/GIVE-asia/gitter.png)](https://gitter.im/GIVE-asia/Lobby)

sbt-vuefy integrates Vue's single components into Playframework. It hot-reloads the changes of Vue components while running Playframework with `sbt run`. It also works with `sbt stage`, which triggers the production build.

Please see the example project in the folder `test-play-project`. Also, see our blog post for some more detail: https://give.engineering/2018/06/05/vue-js-with-playframework.html

This plugin is currently used at [GIVE.asia](https://give.asia).


Requirements
-------------

* __[Webpack](https://webpack.js.org/) and [vue-loader](https://github.com/vuejs/vue-loader):__ you'll need to specify the webpack binary location and webpack's configuration localtion. This enables you to choose your own version of Webpack and your own Webpack's configuration. You can see an example in the folder `test-play-project`.
* __Playframework 2.6:__ This is because GIVE.asia uses Playframework 2.6. Anecdotally, I have been told that [it doesn't work with Playframework 2.5](https://github.com/GIVESocialMovement/sbt-vuefy/issues/10)


How to use
-----------

### 1. Install the plugin

Add the below line to `project/plugins.sbt`:

```
resolvers += Resolver.bintrayRepo("givers", "maven")

addSbtPlugin("givers.vuefy" % "sbt-vuefy" % "1.3.0")
```


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
lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, SbtVuefy) // Enable the plugin

// The commands that triggers production build (as in `webpack -p`)
Assets / VueKeys.vuefy / VueKeys.prodCommands := Set("stage")

// The location of the webpack binary. For windows, it might be `webpack.cmd`.
Assets / VueKeys.vuefy / VueKeys.webpackBinary := "./node_modules/.bin/webpack"

// The location of the webpack configuration.
Assets / VueKeys.vuefy / VueKeys.webpackConfig := "./webpack.config.js"
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

Please feel free to open an issue to ask questions. Let us know how you want to use the plugin. We want to help you use the plugin successfully.


Contributing
---------------

The project welcomes any contribution. Here are the steps for testing when developing locally:

1. Run `yarn install` in order to install packages needed for the integration tests.
2. Run `sbt test` to run all tests.
3. To test the plugin on an actual Playframework project, go to `test-play-project`, run `yarn install`, and run `sbt run`


Future improvement
--------------------

* Currently, the plugin doesn't track CSS dependencies (e.g. using `@import`) because webpack/vue-loader doesn't track these dependencies. We need to find a way. See the ongoing issue: https://github.com/GIVESocialMovement/sbt-vuefy/issues/20
* `VueKeys.prodCommands` is hacky. I use this approach because I don't have good understanding in SBT's scoping. There must be a better way of implementing the production build setting.
