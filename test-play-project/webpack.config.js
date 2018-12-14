"use strict";

const path = require('path');
const webpack = require('webpack');
const VueLoaderPlugin = require('vue-loader/lib/plugin');

// Required by sbt-vuefy.
const SbtVuefyPlugin = require('./sbt-vuefy-plugin.js');

module.exports = {
  output: {
    publicPath: '/assets', // Required by sbt-vuefy.
    library: '[camel-case-name]', // Required by sbt-vuefy.
    filename: '[name].js', // Required by sbt-vuefy.
  },
  plugins: [
    new SbtVuefyPlugin(), // Required by sbt-vuefy.
    new VueLoaderPlugin()
  ],
  cache: true,
  bail: true,
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
      },
      {
        test: /\.scss$/,
        exclude: /node_modules/,
        use: [
          'vue-style-loader',
          'css-loader',
          'sass-loader'
        ],
      },
      {
        test: /\.ts$/,
        loader: 'ts-loader',
        options: {
          appendTsSuffixTo: [/\.vue$/],
        }
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel-loader'
      }
    ]
  },
  externals: {
    // The below allows Typescript to `import Vue from 'vue'` without including Vue in the bundle.
    vue: 'Vue'
  },
  resolve: {
    extensions: ['.ts', '.js', '.vue']
  },
  performance: {
    hints: 'error',
    maxAssetSize: 1500000,
    maxEntrypointSize: 1500000,
    assetFilter: function(assetFilename) {
      return assetFilename.endsWith('.js');
    }
  },
  devtool: ''
};

if (process.env.NODE_ENV === 'production') {
  console.log('Webpack for production')
  module.exports.devtool = '';
  module.exports.performance.maxAssetSize = 250000;
  module.exports.performance.maxEntrypointSize = 250000;
  module.exports.plugins = (module.exports.plugins || []).concat([
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: '"production"'
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      sourceMap: false,
      cache: true,
      parallel: true,
      compress: {
        warnings: false
      }
    }),
    new webpack.LoaderOptionsPlugin({
      minimize: true
    })
  ])
} else {
  console.log('Webpack for development')
}
