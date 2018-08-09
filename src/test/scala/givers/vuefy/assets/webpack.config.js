"use strict";

const path = require('path')
const webpack = require('webpack')

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
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: {
            'scss': [
              'vue-style-loader',
              'css-loader',
              'sass-loader'
            ]
          }
        }
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel-loader'
      }
    ]
  },
  performance: {
    hints: 'error',
  },
  stats: 'minimal',
  devtool: ''
}
