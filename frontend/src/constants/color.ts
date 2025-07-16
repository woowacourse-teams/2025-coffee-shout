import { theme } from '@/styles/theme';
import { makeColorMap } from '@/utils/makeColorMap';

export const COLOR_MAP = {
  ...makeColorMap('gray', theme.color.gray),
  ...makeColorMap('point', theme.color.point),
  white: theme.color.white,
  black: theme.color.black,
} as const;

export type ColorKey = keyof typeof COLOR_MAP;
