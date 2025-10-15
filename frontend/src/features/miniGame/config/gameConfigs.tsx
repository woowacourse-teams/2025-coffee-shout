import CardGameDescription1 from '@/assets/card_game_desc1.svg';
import CardGameDescription2 from '@/assets/card_game_desc2.svg';
import RacingGameDescription1 from '@/assets/racing_game_desc1.svg';
import RacingGameDescription2 from '@/assets/racing_game_desc2.svg';
import CardGameProvider from '@/contexts/CardGame/CardGameProvider';
import RacingGameProvider from '@/contexts/RacingGame/RacingGameProvider';
import { MiniGameType } from '@/types/miniGame/common';
import { ComponentType, PropsWithChildren } from 'react';
import CardGameReadyPage from '../cardGame/pages/CardGameReadyPage';
import RacingGameReadyPage from '../racingGame/pages/RacingGameReadyPage';
import CardGamePlayPage from '../cardGame/pages/CardGamePlayPage';
import RacingGamePlayPage from '../racingGame/pages/RacingGamePlayPage';

export type SlideConfig = {
  textLines: string[];
  imageSrc: string;
  className: string;
};

export type GameConfig = {
  Provider: ComponentType<PropsWithChildren>;
  ReadyPage: ComponentType;
  slides: SlideConfig[];
  PlayPage: ComponentType;
};

export const GAME_CONFIGS: Record<MiniGameType, GameConfig> = {
  CARD_GAME: {
    Provider: CardGameProvider,
    ReadyPage: CardGameReadyPage,
    slides: [
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
    ],
    PlayPage: CardGamePlayPage,
  },
  RACING_GAME: {
    Provider: RacingGameProvider,
    ReadyPage: RacingGameReadyPage,
    slides: [
      {
        textLines: ['빠르게 터치하세요!'],
        imageSrc: RacingGameDescription1,
        className: 'slide-first',
      },
      {
        textLines: ['먼저 도착한 순으로', '등수가 결정됩니다'],
        imageSrc: RacingGameDescription2,
        className: 'slide-second',
      },
    ],
    PlayPage: RacingGamePlayPage,
  },
};
