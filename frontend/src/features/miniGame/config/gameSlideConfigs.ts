import CardGameDescription1 from '@/assets/card_game_desc1.svg';
import CardGameDescription2 from '@/assets/card_game_desc2.svg';
import { MiniGameType } from '@/types/miniGame/common';

type SlideConfig = {
  textLines: readonly string[];
  imageSrc: string;
  className: string;
};

const CARD_GAME_SLIDES: readonly SlideConfig[] = [
  {
    textLines: ['각 라운드마다', '카드 1장을 선택하세요'],
    imageSrc: CardGameDescription1,
    className: 'slide-first',
  },
  {
    textLines: ['합산된 값으로', '등수가 결정됩니다'],
    imageSrc: CardGameDescription2,
    className: 'slide-second',
  },
] as const;

export const GAME_SLIDE_CONFIGS: Record<MiniGameType, readonly SlideConfig[]> = {
  CARD_GAME: CARD_GAME_SLIDES,
} as const;

export const getGameSlideConfig = (gameType: MiniGameType): readonly SlideConfig[] => {
  return GAME_SLIDE_CONFIGS[gameType];
};
