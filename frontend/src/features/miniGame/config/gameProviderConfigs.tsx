import CardGameProvider from '@/contexts/CardGame/CardGameProvider';
import RacingGameProvider from '@/contexts/RacingGame/RacingGameProvider';
import { MiniGameType } from '@/types/miniGame/common';
import { ComponentType, PropsWithChildren } from 'react';

type ProviderComponent = ComponentType<PropsWithChildren>;

export const GAME_PROVIDER: Partial<Record<MiniGameType, ProviderComponent>> = {
  CARD_GAME: CardGameProvider,
  RACING_GAME: RacingGameProvider,
} as const;
