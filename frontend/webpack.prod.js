import { merge } from 'webpack-merge';
import common from './webpack.common.js';

export default (env, argv) =>
  merge(common(env, { ...argv, mode: 'production' }), {
    devtool: 'source-map',
  });
