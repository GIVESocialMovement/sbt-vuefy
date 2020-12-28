"use strict";

const {VueLoaderPlugin} = require('vue-loader');
const TerserPlugin = require('terser-webpack-plugin');

const config = {
  mode: 'development',
  plugins: [
    new VueLoaderPlugin()
  ],
  cache: true,
  bail: true,
  stats: 'minimal',
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        loader: 'ts-loader',
        options: {
          appendTsSuffixTo: [/\.vue$/],
        }
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
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel-loader'
      },
      {
        test: /\.vue$/,
        loader: 'vue-loader',
      },
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
  devtool: 'eval-cheap-source-map',
};

module.exports = (env, argv) => {
  if (argv.mode === 'production') {
    console.log('Webpack for production');
    config.devtool = 'production';
    config.devtool = '';
    config.performance.maxAssetSize = 250000;
    config.performance.maxEntrypointSize = 250000;
    config.optimization = (config.optimization || {});
    config.optimization.minimizer = (config.optimization.minimizer || []).concat([
      new TerserPlugin({
        sourceMap: false,
        cache: true,
        parallel: true
      }),
    ])
  } else {
    console.log('Webpack for development')
  }

  return config;
};
