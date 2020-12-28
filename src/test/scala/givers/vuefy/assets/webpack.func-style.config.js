"use strict";

const {VueLoaderPlugin} = require('vue-loader');

module.exports = (env, argv) => {
  return {
    plugins: [new VueLoaderPlugin()],
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
            'style-loader',
            'css-loader',
            'sass-loader'
          ],
        },
        {
          test: /\.ts$/,
          loader: 'ts-loader',
          options: {
            appendTsSuffixTo: [/\.vue$/]
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
      vue: 'Vue'
    },
    resolve: {
      extensions: ['.ts', '.js', '.vue']
    },
    performance: {
      hints: 'error',
    },
    stats: 'minimal'
  };
};
