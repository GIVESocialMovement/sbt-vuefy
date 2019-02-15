"use strict";

const VueLoaderPlugin = require('vue-loader/lib/plugin');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = {
  plugins: [
    new VueLoaderPlugin()
  ],
  cache: true,
  bail: true,
  stats: 'minimal',
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

// If the arguments includes `-p`, it means we are doing the production build.
if (process.argv.includes('-p')) {
  console.log('Webpack for production');
  module.exports.devtool = '';
  module.exports.performance.maxAssetSize = 250000;
  module.exports.performance.maxEntrypointSize = 250000;
  module.exports.optimization = (module.exports.optimization || {});
  module.exports.optimization.minimizer = (module.exports.optimization.minimizer || []).concat([
    new TerserPlugin({
      sourceMap: false,
      cache: true,
      parallel: true
    }),
  ])
} else {
  console.log('Webpack for development')
}
