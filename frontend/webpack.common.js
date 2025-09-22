import { sentryWebpackPlugin } from '@sentry/webpack-plugin';
import dotenv from 'dotenv';
import { readFileSync } from 'fs';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import webpack from 'webpack';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const packageJson = JSON.parse(readFileSync(path.resolve(__dirname, 'package.json'), 'utf8'));
const appVersion = packageJson.version;

export default (env, argv) => {
  const mode = argv.mode || 'development';

  dotenv.config({ path: path.resolve(process.cwd(), `.env.${mode}`) });

  const mergedEnv = { ...process.env };

  const envKeys = {
    'process.env.NODE_ENV': JSON.stringify(mode),
    'process.env.VERSION': JSON.stringify(appVersion),
    ...Object.fromEntries(
      Object.entries(mergedEnv).map(([k, v]) => [`process.env.${k}`, JSON.stringify(v)])
    ),
  };

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
        { test: /\.tsx?$/, use: 'ts-loader', exclude: /node_modules/ },
        { test: /\.(png|svg|jpg|jpeg|gif|webp)$/i, type: 'asset/resource' },
        { test: /\.css$/i, use: ['style-loader', 'css-loader'] },
      ],
    },
    resolve: {
      extensions: ['.tsx', '.ts', '.js'],
      alias: { '@': path.resolve(__dirname, 'src') },
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
        sourcemaps: { disable: mode !== 'production' },
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
};
