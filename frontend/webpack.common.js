import { sentryWebpackPlugin } from '@sentry/webpack-plugin';
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

// 👇 config를 함수로 만들어서 argv.mode를 활용
export default (env, argv) => {
  const mode = argv.mode || 'development';

  const envKeys = Object.keys(mergedEnv).reduce(
    (acc, key) => {
      acc[`process.env.${key}`] = JSON.stringify(mergedEnv[key]);
      return acc;
    },
    {
      // 👇 mode 값으로 자동 주입
      'process.env.NODE_ENV': JSON.stringify(mode),
      'process.env.VERSION': JSON.stringify(appVersion),
    }
  );

  return {
    mode,
    entry: './src/main.tsx',
    output: {
      publicPath: '/',
      path: path.resolve(__dirname, 'dist'),
      filename: '[name].[contenthash].js',
      chunkFilename: '[name].[contenthash].chunk.js',
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
        favicon: './public/favicon.ico',
      }),
      new webpack.DefinePlugin(envKeys),
      sentryWebpackPlugin({
        authToken: process.env.SENTRY_AUTH_TOKEN,
        org: 'woowacourse-7th-fe',
        project: '2025-coffee-shout',
        release: appVersion,
        sourcemaps: {
          disable: mode !== 'production',
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
    optimization: {
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          vendor: {
            test: /[\\/]node_modules[\\/]/,
            name: 'vendors',
            chunks: 'all',
          },
        },
      },
    },
  };
};
