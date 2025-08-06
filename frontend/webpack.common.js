import SentryWebpackPlugin from '@sentry/webpack-plugin';
import dotenv from 'dotenv';
import { readFileSync } from 'fs';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import webpack from 'webpack';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const dotenvEnv = dotenv.config({ path: path.resolve(__dirname, '.env') }).parsed || {};
const mergedEnv = { ...process.env, ...dotenvEnv };

const packageJson = JSON.parse(readFileSync(path.resolve(__dirname, 'package.json'), 'utf8'));
const appVersion = packageJson.version;

const envKeys = Object.keys(mergedEnv).reduce(
  (acc, key) => {
    if (key.startsWith('REACT_APP_')) {
      acc[`process.env.${key}`] = JSON.stringify(mergedEnv[key]);
    }
    return acc;
  },
  {
    'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development'),
    'process.env.REACT_APP_VERSION': JSON.stringify(appVersion),
  }
);

export default {
  entry: './src/main.tsx',
  output: {
    publicPath: '/',
    path: path.resolve(__dirname, 'dist'),
    filename: '[name].[contenthash].js',
    clean: true,
  },
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './public/index.html',
    }),
    new webpack.DefinePlugin(envKeys),
    new SentryWebpackPlugin({
      authToken: process.env.SENTRY_AUTH_TOKEN,
      org: 'woowacourse-7th-fe',
      project: '2025-coffee-shout',
      release: appVersion,
      sourcemaps: {
        disable: process.env.NODE_ENV !== 'production',
      },
    }),
  ],
  devServer: {
    compress: true,
    port: 3000,
    hot: true,
    open: true,
    historyApiFallback: true,
  },
};
