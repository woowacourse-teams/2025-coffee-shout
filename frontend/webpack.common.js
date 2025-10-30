import webpack from 'webpack';
import CopyWebpackPlugin from 'copy-webpack-plugin';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import dotenv from 'dotenv';
import { readFileSync } from 'fs';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import { sentryWebpackPlugin } from '@sentry/webpack-plugin';
import WebpackBundleAnalyzer from 'webpack-bundle-analyzer';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const packageJson = JSON.parse(readFileSync(path.resolve(__dirname, 'package.json'), 'utf8'));
const appVersion = packageJson.version;

export default (_, argv) => {
  const mode = argv.mode || 'development';

  const dotenvEnv = dotenv.config({ path: path.resolve(process.cwd(), `.env.${mode}`) });
  const mergedEnv = { ...process.env, ...dotenvEnv };

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
        {
          test: /\.(png|svg|jpg|jpeg|gif|webp)$/i,
          type: 'asset/resource',
          generator: {
            filename: (pathData) => {
              // assets/logo 폴더의 이미지들은 해시값 없이 원본 이름 유지
              if (pathData.filename.includes('assets/logo/')) {
                return 'logo/[name][ext]';
              }
              // 다른 이미지들은 기존처럼 해시값 포함
              return '[name].[contenthash][ext]';
            },
          },
        },
        { test: /\.css$/i, use: ['style-loader', 'css-loader'] },
      ],
    },
    resolve: {
      extensions: ['.tsx', '.ts', '.js'],
      alias: { '@': path.resolve(__dirname, 'src') },
      conditionNames: ['import', 'module', 'browser', 'default'],
    },
    plugins: [
      new HtmlWebpackPlugin({
        template: './public/index.html',
        favicon: './public/favicon.ico',
        inject: 'head',
        templateParameters: {
          DEV_SNIPPET: argv.mode === 'development' ? `<script src="/dev-snippet.js"></script>` : '',
        },
      }),
      new CopyWebpackPlugin({
        patterns: [
          {
            from: 'public/fonts',
            to: 'fonts',
          },
          {
            from: 'public/robots.txt',
            to: 'robots.txt',
          },
          {
            from: 'public/sitemap.xml',
            to: 'sitemap.xml',
          },
          { from: 'src/devtools/dev-snippet.ts', to: 'dev-snippet.js' },
        ],
      }),
      new webpack.DefinePlugin(envKeys),
      sentryWebpackPlugin({
        authToken: process.env.SENTRY_AUTH_TOKEN,
        org: 'woowacourse-7th-fe',
        project: '2025-coffee-shout',
        release: appVersion,
        sourcemaps: { disable: mode !== 'production' },
      }),
      new WebpackBundleAnalyzer.BundleAnalyzerPlugin({
        analyzerMode: 'static',
        openAnalyzer: false,
        reportFilename: 'bundle-report.html',
      }),
    ],
    devServer: {
      static: {
        directory: path.resolve(__dirname, 'dist'),
      },
      compress: true,
      port: 3000,
      hot: true,
      open: true,
      historyApiFallback: true,
    },

    optimization: {
      usedExports: true,
      sideEffects: false,

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
