import { clampByWidth, clampByHeight } from '@/utils/responsive';

export const DESIGN_TOKENS = {
  // 타이포그래피
  typography: {
    h1: clampByWidth(24, 30),
    h2: clampByWidth(20, 24),
    h3: clampByWidth(18, 20),
    h4: clampByWidth(14, 16),
    paragraph: clampByWidth(14, 16),
    small: clampByWidth(12, 14),
  },

  // 카드
  card: {
    small: {
      width: clampByWidth(40, 48),
      height: clampByHeight(45, 61),
    },
    medium: {
      width: clampByWidth(54, 64),
      height: clampByHeight(60, 82),
    },
    large: {
      width: clampByWidth(80, 96),
      height: clampByHeight(95, 123),
    },
  },

  // 원형
  circle: {
    small: clampByWidth(29, 34),
    medium: clampByWidth(38, 46),
    large: clampByWidth(57, 69),
  },
} as const;
